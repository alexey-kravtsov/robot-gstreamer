package com.robot.serial.service;

import com.robot.serial.message.SerialMessage;
import com.robot.serial.message.SerialMessageType;
import com.robot.serial.serialization.SerialMessagePayloadSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class SerialCommunicationServiceTestImpl implements SerialCommunicationService {
    private static final Logger logger = LogManager.getLogger(SerialCommunicationService.class);

    @Override
    public void registerSerializer(SerialMessageType messageType, SerialMessagePayloadSerializer serializer) {
        logger.debug("Attempt to register serializer");
    }

    @Override
    public void start() {
        logger.info("Test serial service started");
    }

    @Override
    public void stop() {
        logger.info("Test serial service stopped");
    }

    @Override
    public void sendAsync(SerialMessage message) {
        sendAsync(message, null);
    }

    @Override
    public void sendAsync(SerialMessage message, Consumer<SerialMessage> callback) {
        logger.debug(String.format("Serial message type: %s, payload: [%s]",
                message.getType(),
                message.getPayload()));
    }
}
