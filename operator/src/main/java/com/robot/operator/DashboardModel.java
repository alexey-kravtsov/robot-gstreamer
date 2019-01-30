package com.robot.operator;

import com.robot.common.service.ParallelExecutionService;
import com.robot.network.message.NetworkMessagePayload;
import com.robot.network.message.NetworkMessageType;
import com.robot.network.message.movement.*;
import com.robot.network.message.video.VideoNetworkMessageCodec;
import com.robot.network.message.video.VideoNetworkMessageParameters;
import com.robot.network.message.video.VideoNetworkMessagePayload;
import com.robot.network.message.video.VideoNetworkMessageType;
import com.robot.network.service.NetworkCommunicationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DashboardModel {
    private static final Logger logger = LogManager.getLogger(DashboardModel.class);
    private static final long MOVEMENT_PING_PERIOD_MS = 100;

    private final ParallelExecutionService executionService;
    private final NetworkCommunicationService networkCommunicationService;

    private MovementNetworkMessageParameters currentParameters = null;

    private ScheduledFuture movementPingTask;

    @Inject
    public DashboardModel(ParallelExecutionService executionService,
                          NetworkCommunicationService networkCommunicationService) {
        this.executionService = executionService;
        this.networkCommunicationService = networkCommunicationService;
    }

    public void startRobot(
            Integer hostCommandPort,
            InetSocketAddress videoReceiverAddress,
            short horizontalResolution,
            short verticalResolution,
            VideoNetworkMessageCodec codec,
            InetSocketAddress robotAddress) {
        try {
            networkCommunicationService.start(hostCommandPort);
        } catch (SocketException e) {
            logger.error("Unable to start network communication service", e);
            return;
        }

        VideoNetworkMessagePayload payload = new VideoNetworkMessagePayload(
                VideoNetworkMessageType.START,
                new VideoNetworkMessageParameters(
                        videoReceiverAddress.getAddress(),
                        videoReceiverAddress.getPort(),
                        horizontalResolution,
                        verticalResolution,
                        codec)
        );

        networkCommunicationService.sendAsync(
                NetworkMessageType.VIDEO,
                payload,
                robotAddress);
    }

    public synchronized void startMove(
            InetSocketAddress robotAddress,
            MovementNetworkMessageDirection direction,
            MovementNetworkMessageRotation rotation,
            byte speed) {
        currentParameters = new MovementNetworkMessageParameters(direction, rotation, speed);
        MovementNetworkMessagePayload messagePayload = new MovementNetworkMessagePayload(
                MovementNetworkMessageType.START, currentParameters);
        sendMovementMessage(robotAddress, messagePayload);

        startMovementPing(robotAddress);
    }

    public synchronized void continueMove(
            InetSocketAddress robotAddress,
            MovementNetworkMessageDirection direction,
            MovementNetworkMessageRotation rotation,
            byte speed) {
        currentParameters = new MovementNetworkMessageParameters(direction, rotation, speed);
        MovementNetworkMessagePayload messagePayload = new MovementNetworkMessagePayload(
                MovementNetworkMessageType.CONTINUE, currentParameters);
        sendMovementMessage(robotAddress, messagePayload);
    }

    public synchronized void stopMove(InetSocketAddress robotAddress) {
        stopMovementPing();

        MovementNetworkMessagePayload messagePayload = new MovementNetworkMessagePayload(
                MovementNetworkMessageType.STOP,
                null);

        sendMovementMessage(robotAddress, messagePayload);
    }

    private void sendMovementMessage(
            InetSocketAddress robotAddress,
            NetworkMessagePayload messagePayload) {
        networkCommunicationService.sendAsync(
                NetworkMessageType.MOVEMENT,
                messagePayload,
                robotAddress);
    }

    private void startMovementPing(InetSocketAddress robotAddress) {
        movementPingTask = executionService.submitScheduled(
                () -> sendMovementPing(robotAddress),
                MOVEMENT_PING_PERIOD_MS,
                MOVEMENT_PING_PERIOD_MS,
                TimeUnit.MILLISECONDS);
    }

    private void stopMovementPing() {
        if (movementPingTask != null && !movementPingTask.isDone()) {
            movementPingTask.cancel(false);
        }
    }

    private synchronized void sendMovementPing(
            InetSocketAddress robotAddress) {
        if (currentParameters == null) {
            logger.warn("No movement parameters provided");
        }
        try {
            MovementNetworkMessagePayload messagePayload =
                    new MovementNetworkMessagePayload(MovementNetworkMessageType.CONTINUE, currentParameters);
            sendMovementMessage(robotAddress, messagePayload);
        } catch (Exception e) {
            logger.error("Unable to send message", e);
        }
    }
}
