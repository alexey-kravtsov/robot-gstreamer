package com.robot.network.serialization.movement;

import com.robot.network.message.movement.*;
import com.robot.network.serialization.NetworkMessagePayloadSerializer;
import com.robot.common.serialization.EnumEncoder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.robot.network.message.movement.MovementNetworkMessageType.STOP;

public class MovementNetworkMessagePayloadSerializer
        implements NetworkMessagePayloadSerializer<MovementNetworkMessagePayload> {

    @Override
    public void writePayload(MovementNetworkMessagePayload payload, DataOutputStream stream)
            throws IOException {

        MovementNetworkMessageType movementType = payload.getMovementType();

        stream.writeByte(EnumEncoder.getValue(movementType));

        if (movementType == STOP) {
            return;
        }

        MovementNetworkMessageParameters movementParameters = payload.getMovementParameters();
        if (movementParameters == null) {
            throw new IllegalArgumentException("Missing movement parameters");
        }

        stream.writeByte(EnumEncoder.getValue(movementParameters.getDirection()));
        stream.writeByte(EnumEncoder.getValue(movementParameters.getRotation()));
        stream.writeByte(movementParameters.getSpeed());
    }

    @Override
    public MovementNetworkMessagePayload readPayload(DataInputStream stream) throws IOException {
        MovementNetworkMessageType movementType = EnumEncoder.getType(MovementNetworkMessageType.class, stream.readByte());
        if (movementType == STOP) {
            return new MovementNetworkMessagePayload(movementType, null);
        }

        MovementNetworkMessageDirection movementDirection = EnumEncoder.getType(
                MovementNetworkMessageDirection.class, stream.readByte());
        MovementNetworkMessageRotation movementRotation = EnumEncoder.getType(
                MovementNetworkMessageRotation.class, stream.readByte());
        byte speed = stream.readByte();

        return new MovementNetworkMessagePayload(
                movementType,
                new MovementNetworkMessageParameters(movementDirection, movementRotation, speed)
        );
    }
}
