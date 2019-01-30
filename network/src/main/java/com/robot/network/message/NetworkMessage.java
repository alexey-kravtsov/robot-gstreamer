package com.robot.network.message;

public class NetworkMessage<T extends NetworkMessagePayload> {
    private final NetworkMessageType messageType;
    private final T payload;

    public NetworkMessage(NetworkMessageType messageType, T payload) {
        this.messageType = messageType;
        this.payload = payload;
    }

    public NetworkMessageType getType() {
        return messageType;
    }

    public T getPayload() {
        return payload;
    }
}
