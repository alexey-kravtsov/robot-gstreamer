package com.robot.engineeringrobot.service;

import com.google.inject.Inject;
import com.robot.network.message.movement.*;
import com.robot.serial.message.SerialMessage;
import com.robot.serial.message.SerialMessageType;
import com.robot.serial.message.movement.MovementSerialMessagePayload;
import com.robot.serial.service.SerialCommunicationService;
import com.robot.serial.service.SerialCommunicationServiceImpl;
import com.robot.network.service.NetworkMessageConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.robot.serial.message.movement.MovementSerialMessagePayloadCodes.*;

public class MovementService implements NetworkMessageConsumer<MovementNetworkMessagePayload> {
    private static final Logger logger = LogManager.getLogger(MovementService.class);

    private final SerialCommunicationService serialCommunicationService;

    @Inject
    public MovementService(SerialCommunicationService serialCommunicationService) {
        this.serialCommunicationService = serialCommunicationService;
    }

    @Override
    public void consumeMessage(MovementNetworkMessagePayload payload) {
        if (payload == null) {
            logger.error("Empty payload");
            return;
        }

        byte movementType = encodeMovementType(payload.getMovementType());
        if (payload.getMovementType() == MovementNetworkMessageType.STOP) {
            MovementSerialMessagePayload serialPayload =
                    new MovementSerialMessagePayload(
                            movementType, null, null, null);
            SerialMessage<MovementSerialMessagePayload> serialMessage =
                    new SerialMessage<>(SerialMessageType.MOVEMENT, serialPayload);

            serialCommunicationService.sendAsync(serialMessage);
            return;
        }

        MovementNetworkMessageParameters movementParameters = payload.getMovementParameters();
        if (movementParameters == null) {
            throw new IllegalArgumentException("Missing movement parameters");
        }

        byte movementDirection = encodeMovementDirection(movementParameters.getDirection());
        byte movementRotation = encodeMovementRotation(movementParameters.getRotation());
        byte movementSpeed = movementParameters.getSpeed();

        MovementSerialMessagePayload messagePayload = new MovementSerialMessagePayload(
                movementType, movementDirection, movementRotation, movementSpeed);
        SerialMessage<MovementSerialMessagePayload> serialMessage = new SerialMessage<>(
                SerialMessageType.MOVEMENT, messagePayload);
        serialCommunicationService.sendAsync(serialMessage);
    }

    private static byte encodeMovementType(MovementNetworkMessageType movementType) {
        switch (movementType) {
            case START:
                return MOVEMENT_START;
            case CONTINUE:
                return MOVEMENT_CONTINUE;
            case STOP:
                return MOVEMENT_STOP;
            default:
                throw new IllegalArgumentException(movementType.name());
        }
    }

    private static byte encodeMovementDirection(MovementNetworkMessageDirection movementDirection) {
        switch (movementDirection) {
            case NEUTRAL:
                return DIRECTION_NEUTRAL;
            case FORWARD:
                return DIRECTION_FORWARD;
            case BACKWARD:
                return DIRECTION_BACKWARD;
            default:
                throw new IllegalArgumentException(movementDirection.name());
        }
    }

    private static byte encodeMovementRotation(MovementNetworkMessageRotation movementRotation) {
        switch (movementRotation) {
            case NEUTRAL:
                return ROTATION_NEUTRAL;
            case RIGHT:
                return ROTATION_RIGHT;
            case LEFT:
                return ROTATION_LEFT;
            default:
                throw new IllegalArgumentException(movementRotation.name());
        }
    }
}
