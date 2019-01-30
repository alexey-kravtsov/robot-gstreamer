package com.robot.engineeringrobot;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.robot.common.BootstrapModule;
import com.robot.common.service.ParallelExecutionService;
import com.robot.engineeringrobot.service.MovementService;
import com.robot.engineeringrobot.service.VideoService;
import com.robot.network.message.NetworkMessageType;
import com.robot.network.serialization.video.VideoNetworkMessagePayloadSerializer;
import com.robot.network.service.NetworkCommunicationService;
import com.robot.network.serialization.movement.MovementNetworkMessagePayloadSerializer;
import com.robot.serial.message.SerialMessageType;
import com.robot.serial.serialization.movement.MovementSerialMessagePayloadSerializer;
import com.robot.serial.service.SerialCommunicationService;
import com.robot.serial.service.SerialCommunicationServiceImpl;
import com.robot.serial.service.SerialCommunicationServiceTestImpl;

public class RobotBootstrapModule extends BootstrapModule {

    @Override
    protected void configureModules() {
        bindSingleton(ParallelExecutionService.class);
        bindSingleton(MovementService.class);
        bindSingleton(VideoService.class);
    }

    @Provides
    @Singleton
    NetworkCommunicationService provideNetworkCommunicationService(
            ParallelExecutionService parallelExecutionService,
            MovementService movementService,
            VideoService videoService) {
        NetworkCommunicationService service = new NetworkCommunicationService(parallelExecutionService);

        service.registerConsumer(
                NetworkMessageType.MOVEMENT,
                new MovementNetworkMessagePayloadSerializer(),
                movementService
        );

        service.registerConsumer(
                NetworkMessageType.VIDEO,
                new VideoNetworkMessagePayloadSerializer(),
                videoService
        );

        return service;
    }

    @Provides
    @Singleton
    SerialCommunicationService provideSerialCommunicationService(
            @Named("serial.port.name") String portName,
            @Named("serial.port.baud.rate") Integer baudRate,
            @Named("serial.port.open.timeout") Integer portOpenTimeoutMs,
            @Named("serial.test.mode") boolean testMode,
            ParallelExecutionService parallelExecutionService) {
        if (testMode) {
            return new SerialCommunicationServiceTestImpl();
        }

        SerialCommunicationServiceImpl service = new SerialCommunicationServiceImpl(
                portName, baudRate, portOpenTimeoutMs, parallelExecutionService);

        service.registerSerializer(
                SerialMessageType.MOVEMENT,
                new MovementSerialMessagePayloadSerializer()
        );

        return service;
    }
}
