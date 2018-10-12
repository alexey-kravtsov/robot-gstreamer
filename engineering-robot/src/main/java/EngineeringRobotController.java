import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.NetworkMessageReceiverService;
import service.SerialCommunicationService;

import java.net.SocketException;

public class EngineeringRobotController {
    private static final Logger logger = LogManager.getLogger(EngineeringRobotController.class);

    public static void main(String[] args) throws SocketException {
        Injector injector = Guice.createInjector(new RobotBootstrapModule());

        SerialCommunicationService serialCommunicationService =
                injector.getInstance(SerialCommunicationService.class);
        serialCommunicationService.start();

        NetworkMessageReceiverService networkMessageReceiverService =
                injector.getInstance(NetworkMessageReceiverService.class);
        networkMessageReceiverService.start();

        logger.info("Started");
    }
}