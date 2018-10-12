package service;

import com.google.inject.Inject;
import message.NetworkMessage;
import message.movement.*;

import java.nio.charset.StandardCharsets;

public class MovementService implements NetworkMessageConsumer<MovementMessagePayload> {

    private final SerialCommunicationService serialCommunicationService;

    @Inject
    public MovementService(SerialCommunicationService serialCommunicationService) {
        this.serialCommunicationService = serialCommunicationService;
    }

    @Override
    public void consumeMessage(NetworkMessage<MovementMessagePayload> message) {
        byte[] encodedMessage = encodeMessage(message.getPayload());
        serialCommunicationService.sendAsync(encodedMessage);
    }

    private static byte[] encodeMessage(MovementMessagePayload payload) {
        StringBuilder builder = new StringBuilder();
        builder.append('m');

        MovementType movementType = payload.getMovementType();
        builder.append(encodeMovementType(movementType));
        if (movementType == MovementType.STOP) {
            return builder.toString().getBytes(StandardCharsets.US_ASCII);
        }

        MovementParameters movementParameters = payload.getMovementParameters();
        builder.append(encodeMovementDirection(movementParameters.getDirection()));
        builder.append(encodeMovementRotation(movementParameters.getRotation()));
        builder.append(encodeSpeed(movementParameters.getSpeed()));

        return builder.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static char encodeMovementType(MovementType movementType) {
        switch (movementType) {
            case START:
                return 's';
            case CONTINUE:
                return 'c';
            case STOP:
                return 'p';
            default:
                throw new IllegalArgumentException(movementType.name());
        }
    }

    private static char encodeMovementDirection(MovementDirection movementDirection) {
        switch (movementDirection) {
            case NEUTRAL:
                return 'n';
            case FORWARD:
                return 'f';
            case BACKWARD:
                return 'b';
            default:
                throw new IllegalArgumentException(movementDirection.name());
        }
    }

    private static char encodeMovementRotation(MovementRotation movementRotation) {
        switch (movementRotation) {
            case NEUTRAL:
                return 'n';
            case RIGHT:
                return 'r';
            case LEFT:
                return 'l';
            default:
                throw new IllegalArgumentException(movementRotation.name());
        }
    }

    private static char encodeSpeed(byte speed) {
        if (speed < 0 || speed > 9) {
            throw new IllegalArgumentException("Speed is not digit");
        }

        return String.valueOf(speed).charAt(0);
    }
}
