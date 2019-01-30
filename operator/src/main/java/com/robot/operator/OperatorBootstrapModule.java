package com.robot.operator;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.robot.common.BootstrapModule;
import com.robot.common.service.ParallelExecutionService;
import com.robot.network.message.NetworkMessageType;
import com.robot.network.serialization.movement.MovementNetworkMessagePayloadSerializer;
import com.robot.network.serialization.video.VideoNetworkMessagePayloadSerializer;
import com.robot.network.service.NetworkCommunicationService;

public class OperatorBootstrapModule extends BootstrapModule {

    @Override
    protected void configureModules() {
        bindSingleton(ParallelExecutionService.class);
        bindSingleton(DashboardModel.class);
        bindSingleton(DashboardController.class);
        bindSingleton(DashboardView.class);
    }

    @Provides
    @Singleton
    NetworkCommunicationService getNetworkMessageCommunicationService(
            ParallelExecutionService parallelExecutionService) {
        NetworkCommunicationService service = new NetworkCommunicationService(parallelExecutionService);

        service.registerConsumer(
                NetworkMessageType.MOVEMENT,
                new MovementNetworkMessagePayloadSerializer(),
                null);

        service.registerConsumer(
                NetworkMessageType.VIDEO,
                new VideoNetworkMessagePayloadSerializer(),
                null
        );

        return service;
    }
}
