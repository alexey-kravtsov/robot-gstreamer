package com.robot.serial.service;

import com.robot.serial.message.SerialMessage;
import com.robot.serial.message.SerialMessageType;
import com.robot.serial.serialization.SerialMessagePayloadSerializer;

import java.util.function.Consumer;

public interface SerialCommunicationService {

    void registerSerializer(SerialMessageType messageType, SerialMessagePayloadSerializer serializer);

    void start();

    void stop();

    void sendAsync(SerialMessage message);

    void sendAsync(SerialMessage message, Consumer<SerialMessage> callback);
}
