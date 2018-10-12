package service;

import message.NetworkMessage;
import message.NetworkMessagePayload;

public interface NetworkMessageConsumer<T extends NetworkMessagePayload> {

    void consumeMessage(NetworkMessage<T> message);
}
