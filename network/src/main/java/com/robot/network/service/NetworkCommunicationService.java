package com.robot.network.service;

import com.robot.common.service.ParallelExecutionService;
import com.robot.network.message.NetworkMessagePayload;
import com.robot.network.message.NetworkMessageType;
import com.robot.network.serialization.NetworkMessagePayloadSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkCommunicationService {
    private static final Logger logger = LogManager.getLogger(NetworkCommunicationService.class);


    private final NetworkMessageReceiver receiver;
    private final NetworkMessageSender sender;
    private AtomicBoolean started;
    private final Map<NetworkMessageType, NetworkMessagePayloadSerializer> serializers;
    private final Map<NetworkMessageType, NetworkMessageConsumer> consumers;

    @Inject
    public NetworkCommunicationService(ParallelExecutionService parallelExecutionService) {
        this.serializers = new HashMap<>();
        this.consumers = new HashMap<>();

        this.receiver = new NetworkMessageReceiver(
                parallelExecutionService,
                serializers,
                consumers);

        this.sender = new NetworkMessageSender(parallelExecutionService, serializers);
        this.started = new AtomicBoolean(false);
    }

    public void registerConsumer(
            NetworkMessageType messageType,
            NetworkMessagePayloadSerializer serializer,
            NetworkMessageConsumer consumer) {
        serializers.put(messageType, serializer);
        consumers.put(messageType, consumer);
    }

    public void start(Integer listenPort) throws SocketException {
        receiver.start(listenPort);
        sender.start();
        started.set(true);
    }

    public void stop() {
        started.set(false);
        receiver.stop();
        sender.stop();
    }

    public void sendAsync(
            NetworkMessageType messageType,
            NetworkMessagePayload payload,
            InetSocketAddress address) {
        if (!started.get()) {
            logger.error("Network communication service not started");
            return;
        }

        sender.sendAsync(messageType, payload, address);
    }
}
