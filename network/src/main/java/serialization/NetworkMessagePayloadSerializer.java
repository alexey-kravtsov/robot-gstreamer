package serialization;

import message.NetworkMessagePayload;

import java.io.*;

public interface NetworkMessagePayloadSerializer<T extends NetworkMessagePayload> {

    void writePayload(T payload, DataOutputStream stream) throws IOException;

    T readPayload(DataInputStream stream) throws IOException;
}
