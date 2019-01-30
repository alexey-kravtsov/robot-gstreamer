package com.robot.serial.message.movement;

public class MovementSerialMessagePayloadCodes {
    public static final byte MOVEMENT_START = 's';
    public static final byte MOVEMENT_CONTINUE = 'c';
    public static final byte MOVEMENT_STOP = 'p';

    public static final byte DIRECTION_FORWARD = 'f';
    public static final byte DIRECTION_BACKWARD = 'b';
    public static final byte DIRECTION_NEUTRAL = 'n';

    public static final byte ROTATION_RIGHT = 'r';
    public static final byte ROTATION_LEFT = 'l';
    public static final byte ROTATION_NEUTRAL = 'n';
}
