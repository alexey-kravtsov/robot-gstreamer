package service;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import message.NetworkMessage;
import message.NetworkMessagePayload;
import message.NetworkMessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serialization.EnumEncoder;
import serialization.NetworkMessagePayloadSerializer;

import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static service.NetworkServiceConstants.*;

public class NetworkMessageReceiverService {
    private static final Logger logger = LogManager.getLogger(NetworkMessageReceiverService.class);

    private final NetworkMessageTypeRegistry registry;
    private final ParallelExecutionService parallelExecutionService;
    private final Checksum checksum;
    private final AtomicBoolean started;
    private final byte[] buffer;

    private int listenPort;
    private DatagramSocket socket;

    @Inject
    public NetworkMessageReceiverService(
            @Named("udp.message.receiver.listen.port") Integer listenPort,
            NetworkMessageTypeRegistry registry,
            ParallelExecutionService parallelExecutionService) {
        this.listenPort = listenPort;
        this.registry = registry;
        this.parallelExecutionService = parallelExecutionService;
        checksum = new CRC32();
        started = new AtomicBoolean(false);
        buffer = new byte[MTU_SAFE_MESSAGE_SIZE_BYTES];
    }

    public void start() throws SocketException {
        if (started.get()) {
            logger.warn("Trying to start working service");
            return;
        }
        socket = new DatagramSocket(listenPort);
        socket.setSoTimeout(SO_RECEIVE_TIMEOUT_MS);
        started.set(true);
        parallelExecutionService.submitLongRunning(this::listen);
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

    private void listen() {
        while (started.get()) {
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(receivePacket);
                decodeMessage(buffer, receivePacket.getLength());
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                logger.error("Socket error", e);
            }
        }
    }

    private void decodeMessage(byte[] encodedMessage, int messageLength) {
        if (!checkCRC32(encodedMessage, messageLength)) {
            logger.warn("Incorrect CRC32 checksum");
            return;
        }

        ByteArrayInputStream byteStream = new ByteArrayInputStream(
                encodedMessage,
                CRC32_SIZE_BYTES,
                messageLength - CRC32_SIZE_BYTES);
        DataInputStream stream = new DataInputStream(byteStream);
        try {
            processMessage(stream);
        } catch (Exception e) {
            logger.error("Error while processing message", e);
        } finally {
            try {
                stream.close();
                byteStream.close();
            } catch (IOException e) {
                logger.error("Error while closing message stream", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processMessage(DataInputStream stream) throws IOException {
        byte protocolVersion = stream.readByte();
        if (protocolVersion != PROTOCOL_VERSION) {
            logger.error("Protocol version mismatch");
            return;
        }

        NetworkMessageType messageType = EnumEncoder.getType(NetworkMessageType.class, stream.readByte());
        if (messageType == null) {
            logger.error("Unknown message type");
            return;
        }

        NetworkMessagePayloadSerializer payloadSerializer = registry.getSerializer(messageType);
        if (payloadSerializer == null) {
            logger.error("Serializer not registered");
            return;
        }
        NetworkMessagePayload messagePayload = payloadSerializer.readPayload(stream);

        NetworkMessageConsumer messageConsumer = registry.getConsumer(messageType);
        if (messageConsumer == null) {
            logger.error("Consumer not registered");
            return;
        }

        parallelExecutionService.submit(() -> {
            try {
                messageConsumer.consumeMessage(new NetworkMessage(messageType, messagePayload));
            } catch (Exception e) {
                logger.error("Error during processing message", e);
            }
        });

    }

    private boolean checkCRC32(byte[] message, int messageLength) {
        if (messageLength < CRC32_SIZE_BYTES) {
            return false;
        }

        long crc32 = 0;
        for (int i = 0; i < CRC32_SIZE_BYTES; i++) {
            crc32 <<= 8;
            crc32 |= (message[i] & 0xFF);
        }

        checksum.reset();
        checksum.update(message, CRC32_SIZE_BYTES, messageLength - CRC32_SIZE_BYTES);
        return crc32 == checksum.getValue();
    }
}
