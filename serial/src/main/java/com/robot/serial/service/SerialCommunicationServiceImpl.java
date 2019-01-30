package com.robot.serial.service;

import com.robot.common.service.ParallelExecutionService;
import com.robot.serial.message.SerialMessage;
import com.robot.serial.message.SerialMessagePayload;
import com.robot.serial.message.SerialMessageType;
import com.robot.serial.serialization.SerialMessagePayloadSerializer;
import jssc.SerialPortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.robot.serial.service.SerialCommunicationServiceConstants.MAX_SERIAL_MESSAGE_LENGTH_BYTES;
import static com.robot.serial.service.SerialCommunicationServiceConstants.MESSAGE_MARKER;
import static com.robot.serial.service.SerialCommunicationServiceConstants.PROTOCOL_VERSION;

public class SerialCommunicationServiceImpl implements SerialCommunicationService {
    private static final int MAX_QUEUE_LENGTH = 50;
    private static final Logger logger = LogManager.getLogger(SerialCommunicationServiceImpl.class);

    private final LinkedBlockingQueue<SerialTask> messagesQueue;
    private final ParallelExecutionService parallelExecutionService;
    private final Map<SerialMessageType, SerialMessagePayloadSerializer> serializers;
    private final SerialCommunicator serialCommunicator;
    private final AtomicBoolean started;

    public SerialCommunicationServiceImpl(
            String portName,
            Integer baudRate,
            Integer portOpenTimeoutMs,
            ParallelExecutionService parallelExecutionService) {
        this.parallelExecutionService = parallelExecutionService;
        this.serialCommunicator = new SerialCommunicator(portName, baudRate, portOpenTimeoutMs);
        this.serializers = new HashMap<>();

        messagesQueue = new LinkedBlockingQueue<>();
        started = new AtomicBoolean(false);
    }

    @Override
    public void registerSerializer(SerialMessageType messageType, SerialMessagePayloadSerializer serializer) {
        serializers.put(messageType, serializer);
    }

    @Override
    public void start() {
        if (started.get()) {
            logger.warn("Trying to start working service");
            return;
        }

        try {
            serialCommunicator.connect();
        } catch (SerialPortException e) {
            logger.error("Unable to connect serial port", e);
            return;
        }

        started.set(true);
        parallelExecutionService.submitLongRunning(this::awaitMessages);
    }

    @Override
    public void stop() {
        if (!started.get()) {
            logger.warn("Trying to stop non-working service");
            return;
        }

        try {
            serialCommunicator.disconnect();
        } catch (SerialPortException e) {
            logger.error("Error closing serial port", e);
        }
        started.set(false);
    }

    @Override
    public void sendAsync(SerialMessage message) {
        sendAsync(message, null);
    }

    @Override
    public void sendAsync(SerialMessage message, Consumer<SerialMessage> callback) {
        if (!started.get()) {
            logger.error("Communicator not started");
            return;
        }

        SerialMessagePayloadSerializer serializer = serializers.get(message.getType());
        if (serializer == null) {
            logger.error("Serializer not registered");
            return;
        }

        if (messagesQueue.size() >= MAX_QUEUE_LENGTH) {
            try {
                logger.warn("Serial message queue overflow");
                messagesQueue.poll(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Unable to remove old message", e);
                return;
            }
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteStream);
        try {
            stream.writeByte(MESSAGE_MARKER);
            // reserve space for length
            stream.writeByte(0);
            stream.writeByte(PROTOCOL_VERSION);
            stream.writeByte(message.getType().getMessageCode());

            serializer.writePayload(message.getPayload(), stream);

            stream.flush();
            stream.close();

            byteStream.flush();
            byteStream.close();

            byte[] encodedMessage = byteStream.toByteArray();
            if (encodedMessage.length > MAX_SERIAL_MESSAGE_LENGTH_BYTES + 1) {
                logger.error("Serial message size exceeds maximum size");
                return;
            }
            encodedMessage[1] = (byte)encodedMessage.length;
            messagesQueue.add(new SerialTask(encodedMessage, callback));
        } catch (Exception e) {
            logger.error("Unable to serialize message", e);
        }
    }

    private void awaitMessages() {
        while (started.get()) {
            try {
                SerialTask task = messagesQueue.poll(3, TimeUnit.SECONDS);
                if (task == null) {
                    continue;
                }
                serialCommunicator.write(task.message);

                if (task.callback == null) {
                    continue;
                }

                byte[] response = serialCommunicator.read();
                parallelExecutionService.submit(() -> acceptCallback(response, task.callback));
            } catch (InterruptedException ignored) {

            } catch (Exception e) {
                logger.error("Error processing serial message", e);
            }
        }
    }

    private void acceptCallback(byte[] encodedResponse, Consumer<SerialMessage> callback) {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(encodedResponse);
        DataInputStream stream = new DataInputStream(byteStream);
        try {
            byte protocolVersion = stream.readByte();
            if (protocolVersion != PROTOCOL_VERSION) {
                logger.error("Protocol version mismatch");
                return;
            }

            SerialMessageType messageType = SerialMessageType.getMessageType(stream.readByte());
            if (messageType == null) {
                logger.error("Unknown message type");
                return;
            }

            SerialMessagePayloadSerializer payloadSerializer = serializers.get(messageType);
            if (payloadSerializer == null) {
                logger.error("Serializer not registered");
                return;
            }
            SerialMessagePayload messagePayload = payloadSerializer.readPayload(stream);
            SerialMessage message = new SerialMessage(messageType, messagePayload);

            callback.accept(message);
        } catch (Exception e) {
            logger.error("Error while processing serial message", e);
        } finally {
            try {
                stream.close();
                byteStream.close();
            } catch (IOException e) {
                logger.error("Error while closing serial message stream", e);
            }
        }
    }

    private static class SerialTask {
        private final byte[] message;
        private final Consumer<SerialMessage> callback;

        private SerialTask(byte[] message, Consumer<SerialMessage> callback) {
            this.message = message;
            this.callback = callback;
        }
    }
}
