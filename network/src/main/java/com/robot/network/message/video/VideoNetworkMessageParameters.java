package com.robot.network.message.video;

import java.net.InetAddress;

public class VideoNetworkMessageParameters {

    private final InetAddress receiverAddress;
    private final int receiverPort;
    private final short horizontalResolution;
    private final short verticalResolution;
    private final VideoNetworkMessageCodec codec;

    public VideoNetworkMessageParameters(
            InetAddress receiverAddress,
            int receiverPort,
            short horizontalResolution,
            short verticalResolution,
            VideoNetworkMessageCodec codec) {
        this.receiverAddress = receiverAddress;
        this.receiverPort = receiverPort;
        this.horizontalResolution = horizontalResolution;
        this.verticalResolution = verticalResolution;
        this.codec = codec;
    }

    public InetAddress getReceiverAddress() {
        return receiverAddress;
    }

    public int getReceiverPort() {
        return receiverPort;
    }

    public short getHorizontalResolution() {
        return horizontalResolution;
    }

    public short getVerticalResolution() {
        return verticalResolution;
    }

    public VideoNetworkMessageCodec getCodec() {
        return codec;
    }
}
