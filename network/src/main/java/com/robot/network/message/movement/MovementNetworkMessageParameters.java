package com.robot.network.message.movement;

public class MovementNetworkMessageParameters {
    private final MovementNetworkMessageDirection direction;
    private final MovementNetworkMessageRotation rotation;
    private final Byte speed;

    public MovementNetworkMessageParameters(MovementNetworkMessageDirection direction, MovementNetworkMessageRotation rotation, Byte speed) {
        this.direction = direction;
        this.rotation = rotation;
        this.speed = speed;
    }

    public MovementNetworkMessageDirection getDirection() {
        return direction;
    }

    public MovementNetworkMessageRotation getRotation() {
        return rotation;
    }

    public Byte getSpeed() {
        return speed;
    }
}
