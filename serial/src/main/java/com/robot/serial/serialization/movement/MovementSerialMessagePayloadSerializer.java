package com.robot.serial.serialization.movement;

import com.robot.serial.message.movement.*;
import com.robot.serial.serialization.SerialMessagePayloadSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.robot.serial.message.movement.MovementSerialMessagePayloadCodes.MOVEMENT_STOP;

public class MovementSerialMessagePayloadSerializer
        implements SerialMessagePayloadSerializer<MovementSerialMessagePayload> {

    @Override
    public void writePayload(MovementSerialMessagePayload payload, DataOutputStream stream)
            throws IOException {

        byte movementType = payload.getMovementType();
        stream.writeByte(movementType);

        if (movementType == MOVEMENT_STOP) {
            return;
        }

        Byte direction = payload.getMovementDirection();
        if (direction == null) {
            throw new IllegalArgumentException("Missing movement direction");
        }
        stream.writeByte(direction);

        Byte rotation = payload.getMovementRotation();
        if (rotation == null) {
            throw new IllegalArgumentException("Missing movement rotation");
        }
        stream.writeByte(rotation);

        Byte speed = payload.getSpeed();
        if (speed == null) {
            throw new IllegalArgumentException("Missing movement speed");
        }
        stream.writeByte(speed);
    }

    @Override
    public MovementSerialMessagePayload readPayload(DataInputStream stream) throws IOException {
        byte movementType = stream.readByte();
        if (movementType == MOVEMENT_STOP) {
            return new MovementSerialMessagePayload(movementType,null, null, null);
        }

        byte movementDirection = stream.readByte();
        byte movementRotation = stream.readByte();
        byte speed = stream.readByte();

        return new MovementSerialMessagePayload(movementType, movementDirection, movementRotation, speed);
    }
}
