import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import message.NetworkMessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serialization.movement.MovementPayloadSerializer;
import service.*;

public class RobotBootstrapModule extends BootstrapModule {

    @Override
    protected void configureModules() {
        bindSingleton(ParallelExecutionService.class);
        bindSingleton(SerialCommunicationService.class);
        bindSingleton(MovementService.class);
        bindSingleton(NetworkMessageReceiverService.class);
    }

    @Provides
    NetworkMessageTypeRegistry provideNetworkMessageTypeRegistry(
            MovementService movementService
    ) {
        NetworkMessageTypeRegistry registry = new NetworkMessageTypeRegistry();

        registry.registerMessageType(
                NetworkMessageType.MOVEMENT,
                new MovementPayloadSerializer(),
                movementService
        );

        return registry;
    }
}
