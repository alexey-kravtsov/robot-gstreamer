package com.robot.serial.serialization;

import com.robot.serial.message.SerialMessagePayload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface SerialMessagePayloadSerializer<T extends SerialMessagePayload> {

    void writePayload(T payload, DataOutputStream stream) throws IOException;

    T readPayload(DataInputStream stream) throws IOException;
}
