package message.movement;

public class MovementParameters {
    private final MovementDirection direction;
    private final MovementRotation rotation;
    private final Byte speed;

    public MovementParameters(MovementDirection direction, MovementRotation rotation, Byte speed) {
        this.direction = direction;
        this.rotation = rotation;
        this.speed = speed;
    }

    public MovementDirection getDirection() {
        return direction;
    }

    public MovementRotation getRotation() {
        return rotation;
    }

    public Byte getSpeed() {
        return speed;
    }
}
