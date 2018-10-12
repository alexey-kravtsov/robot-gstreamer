package serialization.movement;

import message.movement.*;
import serialization.EnumEncoder;
import serialization.NetworkMessagePayloadSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MovementPayloadSerializer
        implements NetworkMessagePayloadSerializer<MovementMessagePayload> {

    @Override
    public void writePayload(MovementMessagePayload payload, DataOutputStream stream)
            throws IOException {

        MovementType movementType = payload.getMovementType();

        stream.writeByte(EnumEncoder.getValue(movementType));

        if (movementType == MovementType.STOP) {
            return;
        }

        MovementParameters movementParameters = payload.getMovementParameters();
        if (movementParameters == null) {
            throw new IllegalArgumentException("Missing movement parameters");
        }

        stream.writeByte(EnumEncoder.getValue(movementParameters.getDirection()));
        stream.writeByte(EnumEncoder.getValue(movementParameters.getRotation()));
        stream.writeByte(movementParameters.getSpeed());
    }

    @Override
    public MovementMessagePayload readPayload(DataInputStream stream) throws IOException {
        MovementType movementType = EnumEncoder.getType(MovementType.class, stream.readByte());
        if (movementType == MovementType.STOP) {
            return new MovementMessagePayload(movementType, null);
        }

        MovementDirection movementDirection = EnumEncoder.getType(
                MovementDirection.class, stream.readByte());
        MovementRotation movementRotation = EnumEncoder.getType(
                MovementRotation.class, stream.readByte());
        byte speed = stream.readByte();

        return new MovementMessagePayload(
                movementType,
                new MovementParameters(movementDirection, movementRotation, speed)
        );
    }
}
