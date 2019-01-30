package com.robot.network.message.video;

import com.robot.network.message.NetworkMessagePayload;

public class VideoNetworkMessagePayload extends NetworkMessagePayload {

    private final VideoNetworkMessageType messageType;
    private final VideoNetworkMessageParameters parameters;

    public VideoNetworkMessagePayload(
            VideoNetworkMessageType messageType,
            VideoNetworkMessageParameters parameters) {
        this.messageType = messageType;
        this.parameters = parameters;
    }

    public VideoNetworkMessageType getMessageType() {
        return messageType;
    }

    public VideoNetworkMessageParameters getParameters() {
        return parameters;
    }
}
