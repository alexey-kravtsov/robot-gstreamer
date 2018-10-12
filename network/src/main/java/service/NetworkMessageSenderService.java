package service;

import com.google.inject.Inject;
import message.NetworkMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serialization.EnumEncoder;
import serialization.NetworkMessagePayloadSerializer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static service.NetworkServiceConstants.*;

public class NetworkMessageSenderService {
    private static final int MAX_QUEUE_LENGTH = 50;
    private static final Logger logger = LogManager.getLogger(NetworkMessageSenderService.class);

    private final NetworkMessageTypeRegistry registry;
    private final ParallelExecutionService parallelExecutionService;
    private final LinkedBlockingQueue<SendTask> messagesQueue;
    private final Checksum checksum;
    private final AtomicBoolean started;

    private DatagramSocket socket;

    @Inject
    public NetworkMessageSenderService(
            NetworkMessageTypeRegistry registry,
            ParallelExecutionService parallelExecutionService) {
        this.registry = registry;
        this.parallelExecutionService = parallelExecutionService;
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
        socket.setSoTimeout(SO_SEND_TIMEOUT_MS);
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

    public void sendMessageAsync(NetworkMessage message, InetAddress address, int port) {
        if (messagesQueue.size() >= MAX_QUEUE_LENGTH) {
            try {
                logger.warn("Network message queue overflow");
                messagesQueue.poll(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Unable to remove old message", e);
                return;
            }
        }
        messagesQueue.add(new SendTask(message, address, port));
    }

    private void awaitMessages() {
        while (started.get()) {
            try {
                SendTask task = messagesQueue.poll(3, TimeUnit.SECONDS);
                if (task == null) {
                    continue;
                }
                sendMessage(task.message, task.address, task.port);
            } catch (InterruptedException ignored) {

            } catch (Exception e) {
                logger.error("Unable to send message", e);
            }
        }
    }

    private void sendMessage(NetworkMessage message, InetAddress address, int port) {
        NetworkMessagePayloadSerializer serializer = registry.getSerializer(message.getType());
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

            if (messageBytes.length > MTU_SAFE_MESSAGE_SIZE_BYTES) {
                logger.error(
                        "Message size {} larger than safe MTU size {}",
                        messageBytes.length,
                        MTU_SAFE_MESSAGE_SIZE_BYTES);
                return;
            }
            addCRC32(messageBytes);
            sendBytes(messageBytes, address, port);
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

    private void sendBytes(byte[] data, InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }

    private void addCRC32(byte[] message) {
        checksum.reset();
        checksum.update(message, CRC32_SIZE_BYTES, message.length - CRC32_SIZE_BYTES);
        long crc32 = checksum.getValue();

        for (int i = CRC32_SIZE_BYTES - 1; i >= 0; i--) {
            message[i] = (byte)(crc32 & 0xFF);
            crc32 >>>= 8;
        }
    }

    private class SendTask {
        private final NetworkMessage message;
        private final InetAddress address;
        private final int port;

        private SendTask(NetworkMessage message, InetAddress address, int port) {
            this.message = message;
            this.address = address;
            this.port = port;
        }
    }
}
