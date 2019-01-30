package com.robot.network.service;

import com.robot.common.service.ParallelExecutionService;
import com.robot.network.message.NetworkMessage;
import com.robot.network.message.NetworkMessagePayload;
import com.robot.network.message.NetworkMessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.robot.common.serialization.EnumEncoder;
import com.robot.network.serialization.NetworkMessagePayloadSerializer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static com.robot.network.service.NetworkServiceConstants.PROTOCOL_VERSION;

class NetworkMessageSender {
    private static final int MAX_QUEUE_LENGTH = 50;
    private static final Logger logger = LogManager.getLogger(NetworkMessageSender.class);

    private final ParallelExecutionService parallelExecutionService;
    private final Map<NetworkMessageType, NetworkMessagePayloadSerializer> serializers;
    private final LinkedBlockingQueue<SendTask> messagesQueue;
    private final Checksum checksum;
    private final AtomicBoolean started;

    private DatagramSocket socket;

    NetworkMessageSender(
            ParallelExecutionService parallelExecutionService,
            Map<NetworkMessageType, NetworkMessagePayloadSerializer> serializers) {
        this.parallelExecutionService = parallelExecutionService;
        this.serializers = serializers;
        this.messagesQueue = new LinkedBlockingQueue<>();
        this.checksum = new CRC32();
        this.started = new AtomicBoolean(false);
    }

    public void start() throws SocketException {
        if (started.get()) {
            logger.warn("Trying to start working service");
            return;
        }
        socket = new DatagramSocket();
        socket.setSoTimeout(NetworkServiceConstants.SO_SEND_TIMEOUT_MS);
        started.set(true);
        parallelExecutionService.submitLongRunning(this::awaitMessages);
    }

    public void stop() {
        if (!started.get()) {
            logger.warn("Trying to stop non-working service");
            return;
        }

        if (socket != null) {
            socket.close();
        }
        started.set(false);
    }

    public void sendAsync(
            NetworkMessageType messageType,
            NetworkMessagePayload payload,
            InetSocketAddress address) {
        if (messagesQueue.size() >= MAX_QUEUE_LENGTH) {
            try {
                logger.warn("Network message queue overflow");
                messagesQueue.poll(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Unable to remove old message", e);
                return;
            }
        }
        NetworkMessage message = new NetworkMessage(messageType, payload);
        messagesQueue.add(new SendTask(message, address));
    }

    private void awaitMessages() {
        while (started.get()) {
            try {
                SendTask task = messagesQueue.poll(3, TimeUnit.SECONDS);
                if (task == null) {
                    continue;
                }
                sendMessage(task.message, task.address);
            } catch (InterruptedException ignored) {

            } catch (Exception e) {
                logger.error("Unable to send message", e);
            }
        }
    }

    private void sendMessage(NetworkMessage message, InetSocketAddress address) {
        NetworkMessagePayloadSerializer serializer = serializers.get(message.getType());
        if (serializer == null) {
            logger.error("Serializer not registered");
            return;
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteStream);
        try {
            //reserve space for CRC32
            stream.writeInt(0);
            stream.writeByte(PROTOCOL_VERSION);
            stream.writeByte(EnumEncoder.getValue(message.getType()));

            serializer.writePayload(message.getPayload(), stream);
            stream.flush();
            byteStream.flush();
            byte[] messageBytes = byteStream.toByteArray();

            if (messageBytes.length > NetworkServiceConstants.MTU_SAFE_MESSAGE_SIZE_BYTES) {
                logger.error(
                        "Message size {} larger than safe MTU size {}",
                        messageBytes.length,
                        NetworkServiceConstants.MTU_SAFE_MESSAGE_SIZE_BYTES);
                return;
            }
            addCRC32(messageBytes);
            sendBytes(messageBytes, address);
        } catch (Exception e) {
            logger.error("Error while sending message", e);
        } finally {
            try {
                stream.close();
                byteStream.close();
            } catch (IOException e) {
                logger.error("Error while closing message stream", e);
            }
        }
    }

    private void sendBytes(byte[] data, InetSocketAddress address) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, address);
        socket.send(packet);
    }

    private void addCRC32(byte[] message) {
        checksum.reset();
        checksum.update(message, NetworkServiceConstants.CRC32_SIZE_BYTES, message.length - NetworkServiceConstants.CRC32_SIZE_BYTES);
        long crc32 = checksum.getValue();

        for (int i = NetworkServiceConstants.CRC32_SIZE_BYTES - 1; i >= 0; i--) {
            message[i] = (byte)(crc32 & 0xFF);
            crc32 >>>= 8;
        }
    }

    private class SendTask {
        private final NetworkMessage message;
        private final InetSocketAddress address;

        private SendTask(NetworkMessage message, InetSocketAddress address) {
            this.message = message;
            this.address = address;
        }
    }
}
