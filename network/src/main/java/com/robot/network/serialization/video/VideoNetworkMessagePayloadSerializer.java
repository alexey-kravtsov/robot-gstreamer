package com.robot.network.serialization.video;

import com.robot.common.serialization.EnumEncoder;
import com.robot.network.message.video.VideoNetworkMessageCodec;
import com.robot.network.message.video.VideoNetworkMessageParameters;
import com.robot.network.message.video.VideoNetworkMessagePayload;
import com.robot.network.message.video.VideoNetworkMessageType;
import com.robot.network.serialization.NetworkMessagePayloadSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import static com.robot.network.message.video.VideoNetworkMessageType.STOP;

public class VideoNetworkMessagePayloadSerializer
        implements NetworkMessagePayloadSerializer<VideoNetworkMessagePayload> {
    @Override
    public void writePayload(VideoNetworkMessagePayload payload, DataOutputStream stream) throws IOException {
        VideoNetworkMessageType messageType = payload.getMessageType();
        stream.writeByte(EnumEncoder.getValue(messageType));

        if (messageType == STOP) {
            return;
        }

        VideoNetworkMessageParameters videoParameters = payload.getParameters();
        if (videoParameters == null) {
            throw new IllegalArgumentException("Missing video parameters");
        }

        byte[] addressBytes = videoParameters.getReceiverAddress().getAddress();
        stream.write(addressBytes);
        stream.writeInt(videoParameters.getReceiverPort());
        stream.writeShort(videoParameters.getHorizontalResolution());
        stream.writeShort(videoParameters.getVerticalResolution());
        stream.writeByte(EnumEncoder.getValue(videoParameters.getCodec()));
    }

    @Override
    public VideoNetworkMessagePayload readPayload(DataInputStream stream) throws IOException {
        VideoNetworkMessageType messageType = EnumEncoder.getType(VideoNetworkMessageType.class, stream.readByte());
        if (messageType == STOP) {
            return new VideoNetworkMessagePayload(messageType, null);
        }

        byte[] addressBytes = new byte[4];
        stream.read(addressBytes, 0, 4);
        InetAddress address = InetAddress.getByAddress(addressBytes);
        int port = stream.readInt();
        short horizontalResolution = stream.readShort();
        short verticalResoluton = stream.readShort();
        VideoNetworkMessageCodec codec = EnumEncoder.getType(VideoNetworkMessageCodec.class, stream.readByte());

        VideoNetworkMessageParameters parameters = new VideoNetworkMessageParameters(
                address, port, horizontalResolution, verticalResoluton, codec);

        return new VideoNetworkMessagePayload(messageType, parameters);
    }
}
