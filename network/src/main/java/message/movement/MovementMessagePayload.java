package message.movement;

import message.NetworkMessagePayload;

public class MovementMessagePayload extends NetworkMessagePayload {

    private final MovementType movementType;
    private final MovementParameters movementParameters;

    public MovementMessagePayload(MovementType movementType, MovementParameters movementParameters) {
        this.movementType = movementType;
        this.movementParameters = movementParameters;
    }


    public MovementType getMovementType() {
        return movementType;
    }

    public MovementParameters getMovementParameters() {
        return movementParameters;
    }
}
