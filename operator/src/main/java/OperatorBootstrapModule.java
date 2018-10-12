import com.google.inject.Provides;
import message.NetworkMessageType;
import serialization.movement.MovementPayloadSerializer;
import service.NetworkMessageSenderService;
import service.NetworkMessageTypeRegistry;
import service.ParallelExecutionService;

public class OperatorBootstrapModule extends BootstrapModule {

    @Override
    protected void configureModules() {
        bindSingleton(ParallelExecutionService.class);
        bindSingleton(NetworkMessageSenderService.class);
        bindSingleton(DashboardFrame.class);
    }

    @Provides
    NetworkMessageTypeRegistry getNetworkMessageTypeRegistry() {
        NetworkMessageTypeRegistry registry = new NetworkMessageTypeRegistry();

        registry.registerMessageType(
                NetworkMessageType.MOVEMENT,
                new MovementPayloadSerializer(),
                null
        );

        return registry;
    }
}
