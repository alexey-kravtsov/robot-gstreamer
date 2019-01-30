package com.robot.serial.message.movement;

import com.robot.serial.message.SerialMessagePayload;

public class MovementSerialMessagePayload extends SerialMessagePayload {
    private final byte movementType;
    private final Byte movementDirection;
    private final Byte movementRotation;
    private final Byte speed;

    public MovementSerialMessagePayload(
            byte movementType,
            Byte movementDirection,
            Byte movementRotation,
            Byte speed) {
        this.movementType = movementType;
        this.movementDirection = movementDirection;
        this.movementRotation = movementRotation;
        this.speed = speed;
    }

    public byte getMovementType() {
        return movementType;
    }

    public Byte getMovementDirection() {
        return movementDirection;
    }

    public Byte getMovementRotation() {
        return movementRotation;
    }

    public Byte getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return String.format("type: %s, direction: %s, rotation: %s, speed: %s",
                movementType, movementDirection, movementRotation, speed);
    }
}
