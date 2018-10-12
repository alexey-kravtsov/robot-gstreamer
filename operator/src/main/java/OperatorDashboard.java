import com.google.inject.Guice;
import com.google.inject.Injector;
import message.NetworkMessage;
import message.NetworkMessageType;
import message.movement.*;
import service.NetworkMessageSenderService;

import javax.swing.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class OperatorDashboard {

    public static void main(String[] args) throws SocketException {
        Injector injector = Guice.createInjector(new OperatorBootstrapModule());

        NetworkMessageSenderService networkMessageSenderService =
                injector.getInstance(NetworkMessageSenderService.class);
        networkMessageSenderService.start();

        SwingUtilities.invokeLater(() -> {
            DashboardFrame dashboardFrame = injector.getInstance(DashboardFrame.class);
            dashboardFrame.setSize(640, 480);
            dashboardFrame.setVisible(true);
        });
    }


//    public static void main(String[] args) throws SocketException {
//        Injector injector = Guice.createInjector(new OperatorBootstrapModule());
//        NetworkMessageSenderService networkMessageSenderService =
//                injector.getInstance(NetworkMessageSenderService.class);
//
//        networkMessageSenderService.start();
//
//        MovementMessagePayload messagePayload = new MovementMessagePayload(
//                MovementType.START,
//                new MovementParameters(
//                        MovementDirection.FORWARD,
//                        MovementRotation.NEUTRAL,
//                        (byte)1
//                )
//        );
//        NetworkMessage<MovementMessagePayload> message = new NetworkMessage(
//                NetworkMessageType.MOVEMENT, messagePayload
//        );
//
//        Scanner scanner = new Scanner(System.in);
//        while (!scanner.next().equals("stop")) {
//            System.out.println(System.nanoTime());
//            networkMessageSenderService.sendMessageAsync(message,
//                    InetAddress.getLoopbackAddress(), 10500);
//        }
//    }
}
