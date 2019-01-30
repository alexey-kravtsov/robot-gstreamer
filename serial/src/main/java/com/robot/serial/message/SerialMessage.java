package com.robot.serial.message;

public class SerialMessage<T extends SerialMessagePayload> {
    private final SerialMessageType messageType;
    private final T payload;

    public SerialMessage(SerialMessageType messageType, T payload) {
        this.messageType = messageType;
        this.payload = payload;
    }

    public SerialMessageType getType() {
        return messageType;
    }

    public T getPayload() {
        return payload;
    }
}
