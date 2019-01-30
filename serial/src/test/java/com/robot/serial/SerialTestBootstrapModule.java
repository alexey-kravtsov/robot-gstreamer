package com.robot.serial;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.robot.common.BootstrapModule;
import com.robot.common.service.ParallelExecutionService;
import com.robot.serial.message.SerialMessageType;
import com.robot.serial.serialization.movement.MovementSerialMessagePayloadSerializer;
import com.robot.serial.service.SerialCommunicationService;
import com.robot.serial.service.SerialCommunicationServiceImpl;
import com.robot.serial.service.SerialCommunicationServiceTestImpl;

public class SerialTestBootstrapModule extends BootstrapModule {
    @Override
    protected void configureModules() {
        bindSingleton(ParallelExecutionService.class);
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
