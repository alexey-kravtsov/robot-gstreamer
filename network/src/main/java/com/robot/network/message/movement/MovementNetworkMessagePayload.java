package com.robot.network.message.movement;

import com.robot.network.message.NetworkMessagePayload;

public class MovementNetworkMessagePayload extends NetworkMessagePayload {

    private final MovementNetworkMessageType movementType;
    private final MovementNetworkMessageParameters movementParameters;

    public MovementNetworkMessagePayload(MovementNetworkMessageType movementType, MovementNetworkMessageParameters movementParameters) {
        this.movementType = movementType;
        this.movementParameters = movementParameters;
    }

    public MovementNetworkMessageType getMovementType() {
        return movementType;
    }

    public MovementNetworkMessageParameters getMovementParameters() {
        return movementParameters;
    }
}
