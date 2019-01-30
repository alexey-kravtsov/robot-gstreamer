package com.robot.serial.message;

public enum SerialMessageType {
    MOVEMENT((byte)'m');

    private final byte messageCode;

    SerialMessageType(byte code) {
        this.messageCode = code;
    }

    public byte getMessageCode() {
        return messageCode;
    }

    public static SerialMessageType getMessageType(byte code) {
        for (SerialMessageType messageType : SerialMessageType.values()) {
            if (messageType.messageCode == code) {
                return messageType;
            }
        }

        return null;
    }
}
