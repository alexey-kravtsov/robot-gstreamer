package com.robot.operator;

import com.google.common.collect.Sets;
import com.robot.network.message.movement.*;
import com.robot.network.message.video.VideoNetworkMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Pipeline;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetSocketAddress;
import java.util.Set;

import static com.robot.network.message.video.VideoNetworkMessageCodec.H264;
import static com.robot.network.message.video.VideoNetworkMessageCodec.MPEG4;

public class DashboardController {
    private static final Logger logger = LogManager.getLogger(DashboardController.class);

    private final Set<Integer> movementKeys;
    private final Set<Integer> keysHold;

    private final DashboardView view;
    private final DashboardModel model;

    private Pipeline pipe;

    @Inject
    public DashboardController(
            DashboardView view,
            DashboardModel model) {
        movementKeys = Sets.newHashSet(
                KeyEvent.VK_W,
                KeyEvent.VK_A,
                KeyEvent.VK_S,
                KeyEvent.VK_D,
                KeyEvent.VK_SPACE
        );

        keysHold = Sets.newHashSet();

        this.view = view;
        this.model = model;

        this.view.startVideoButton.addActionListener(e -> {
            VideoNetworkMessageCodec codec = getCodec();
            String binConfg = null;

            if (codec == H264) {
                binConfg = "udpsrc port=5200 caps = \"application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264, payload=(int)96\" ! queue ! rtph264depay ! queue ! h264parse ! queue !  avdec_h264 ! queue ! videoconvert ! queue";
            }

            if (codec == MPEG4) {
                binConfg = "udpsrc port=5200 caps = \"application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)MP4V-ES, payload=(int)96\" ! rtpmp4vdepay ! queue ! avdec_mpeg4 ! queue ! videoconvert ! queue";
            }

            Bin bin = Bin.launch(binConfg, true);

            pipe = new Pipeline();
            pipe.addMany(bin, view.videoComponent.getElement());
            Pipeline.linkMany(bin, view.videoComponent.getElement());
            pipe.play();

            this.handleStartButtonPressed();
        });

        MovementKeyListener movementKeyListener = new MovementKeyListener();
        this.view.rootPanel.addKeyListener(movementKeyListener);
        this.view.videoComponentPanel.addKeyListener(movementKeyListener);
    }

    public DashboardView getView() {
        return view;
    }

    public DashboardModel getModel() {
        return model;
    }

    public Integer getHostCommandPort() {
        return Integer.parseInt(view.hostCommandPortText.getText());
    }

    public InetSocketAddress getVideoReceiverAddress() {
        return new InetSocketAddress(
                view.hostAddressText.getText(),
                Integer.parseInt(view.hostVideoPortText.getText()));
    }

    public VideoNetworkMessageCodec getCodec() {
        if (view.h264CodecRadioBtn.isSelected()) {
            return H264;
        }

        if (view.mpeg4CodecRadioBtn.isSelected()) {
            return MPEG4;
        }

        return null;
    }

    public Short getVideoHorizontalResolution() {
        if (view.res640x480RadioBtn.isSelected()) {
            return (short)640;
        }

        if (view.res320x240RadioBtn.isSelected()) {
            return (short)320;
        }

        return null;
    }

    public Short getVideoVerticalResolution() {
        if (view.res640x480RadioBtn.isSelected()) {
            return (short)480;
        }

        if (view.res320x240RadioBtn.isSelected()) {
            return (short)240;
        }

        return null;
    }

    private byte getSpeed() {
        return 1;
    }

    public InetSocketAddress getRobotCommandAddress() {
        return new InetSocketAddress(
                view.robotAddressText.getText(),
                Integer.parseInt(view.robotCommandPortText.getText()));
    }

    public void handleKeyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (!movementKeys.contains(keyCode) || keysHold.contains(keyCode)) {
            return;
        }

        try {
            InetSocketAddress robotCommandAddress = getRobotCommandAddress();
            if (keyCode == KeyEvent.VK_SPACE) {
                keysHold.clear();
                model.stopMove(robotCommandAddress);
                return;
            }

            boolean firstCommand = keysHold.isEmpty();
            keysHold.add(keyCode);

            MovementNetworkMessageDirection direction = getDirection(keysHold);
            MovementNetworkMessageRotation rotation = getRotation(keysHold);
            byte speed = getSpeed();

            if (firstCommand) {
                model.startMove(robotCommandAddress, direction, rotation, speed);
            } else {
                model.continueMove(robotCommandAddress, direction, rotation, speed);
            }
        } catch (Exception ex) {
            logger.error("Unable to send message", ex);
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (!movementKeys.contains(e.getKeyCode()) || keyCode == KeyEvent.VK_SPACE) {
            return;
        }

        try {
            InetSocketAddress robotCommandAddress = getRobotCommandAddress();
            keysHold.remove(keyCode);

            if (keysHold.isEmpty()) {
                model.stopMove(robotCommandAddress);
                return;
            }

            MovementNetworkMessageDirection direction = getDirection(keysHold);
            MovementNetworkMessageRotation rotation = getRotation(keysHold);
            byte speed = getSpeed();
            model.continueMove(robotCommandAddress, direction, rotation, speed);
        } catch (Exception ex) {
            logger.error("Unable to send message", ex);
        }
    }

    public void handleStartButtonPressed() {
        try {
            Integer hostCommandPort = getHostCommandPort();
            InetSocketAddress robotCommandAddress = getRobotCommandAddress();
            InetSocketAddress videoReceiverAddress = getVideoReceiverAddress();
            short videoHorizontalResolution = getVideoHorizontalResolution();
            short videoVerticalResolution = getVideoVerticalResolution();
            VideoNetworkMessageCodec codec = getCodec();

            model.startRobot(
                    hostCommandPort,
                    videoReceiverAddress,
                    videoHorizontalResolution,
                    videoVerticalResolution,
                    codec,
                    robotCommandAddress);
        } catch (Exception e) {
            logger.error("Unable to start robot", e);
        }
    }

    private static MovementNetworkMessageDirection getDirection(Set<Integer> keysHold) {
        if (keysHold.contains(KeyEvent.VK_W)) {
            return MovementNetworkMessageDirection.FORWARD;
        } else if (keysHold.contains(KeyEvent.VK_S)) {
            return MovementNetworkMessageDirection.BACKWARD;
        } else {
            return MovementNetworkMessageDirection.NEUTRAL;
        }
    }

    private static MovementNetworkMessageRotation getRotation(Set<Integer> keysHold) {
        if (keysHold.contains(KeyEvent.VK_A)) {
            return MovementNetworkMessageRotation.LEFT;
        } else if (keysHold.contains(KeyEvent.VK_D)) {
            return MovementNetworkMessageRotation.RIGHT;
        } else {
            return MovementNetworkMessageRotation.NEUTRAL;
        }
    }

    private class MovementKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            handleKeyPressed(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            handleKeyReleased(e);
        }
    }
}
