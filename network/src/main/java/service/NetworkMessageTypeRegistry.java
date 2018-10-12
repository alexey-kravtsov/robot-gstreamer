package service;

import message.NetworkMessageType;
import serialization.NetworkMessagePayloadSerializer;

import java.util.HashMap;
import java.util.Map;

public class NetworkMessageTypeRegistry {
    private final Map<NetworkMessageType, MessageProcessors> messageProcessors = new HashMap<>();

    public void registerMessageType(
            NetworkMessageType messageType,
            NetworkMessagePayloadSerializer serializer,
            NetworkMessageConsumer consumer) {
        messageProcessors.put(messageType, new MessageProcessors(serializer, consumer));
    }

    public NetworkMessagePayloadSerializer getSerializer(NetworkMessageType messageType) {
        MessageProcessors processors = messageProcessors.get(messageType);
        if (processors == null) {
            return null;
        }

        return processors.serializer;
    }

    public NetworkMessageConsumer getConsumer(NetworkMessageType messageType) {
        MessageProcessors processors = messageProcessors.get(messageType);
        if (processors == null) {
            return null;
        }

        return processors.consumer;
    }

    private class MessageProcessors {
        private final NetworkMessagePayloadSerializer serializer;
        private final NetworkMessageConsumer consumer;

        private MessageProcessors(
                NetworkMessagePayloadSerializer serializer,
                NetworkMessageConsumer consumer) {
            this.serializer = serializer;
            this.consumer = consumer;
        }
    }
}
