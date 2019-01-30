package com.robot.network.service;

import com.robot.network.message.NetworkMessagePayload;

public interface NetworkMessageConsumer<T extends NetworkMessagePayload> {

    void consumeMessage(T payload);
}
