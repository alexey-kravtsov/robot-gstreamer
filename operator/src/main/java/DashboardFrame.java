import com.google.common.collect.Sets;
import com.google.inject.Inject;
import message.NetworkMessage;
import message.movement.MovementMessagePayload;
import service.NetworkMessageSenderService;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;

public class DashboardFrame extends JFrame implements KeyListener {

    private final Set<Integer> movementKeys;
    private final Set<Integer> keysHold;
    private final NetworkMessageSenderService senderService;

    @Inject
    public DashboardFrame(NetworkMessageSenderService networkSenderService) {
        super("Operator Dashboard");
        JPanel p = new JPanel();
        add(p);
        addKeyListener(this);

        this.senderService = networkSenderService;

        movementKeys = Sets.newConcurrentHashSet();
        movementKeys.add(KeyEvent.VK_W);
        movementKeys.add(KeyEvent.VK_A);
        movementKeys.add(KeyEvent.VK_S);
        movementKeys.add(KeyEvent.VK_D);
        movementKeys.add(KeyEvent.VK_SPACE);

        keysHold = Sets.newConcurrentHashSet();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (!movementKeys.contains(e.getKeyCode())) {
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            keysHold.clear();
            keysHold.add(e.getKeyCode());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private synchronized static NetworkMessage<MovementMessagePayload> getMessage(
            Set<Integer> keysHold) {
        MovementMessagePayload message;

        return null;
    }
}
