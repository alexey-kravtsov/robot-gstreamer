package com.robot.serial.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.robot.serial.SerialTestBootstrapModule;
import com.robot.serial.message.SerialMessage;
import com.robot.serial.message.movement.MovementSerialMessagePayload;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.robot.serial.message.SerialMessageType.MOVEMENT;
import static com.robot.serial.message.movement.MovementSerialMessagePayloadCodes.*;

public class SerialCommunicationServiceTest {
    private static SerialCommunicationService communicationService;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new SerialTestBootstrapModule());
        communicationService = injector.getInstance(SerialCommunicationService.class);

        communicationService.start();
    }

//    @AfterClass
//    public static void shutdown() {
//        communicationService.stop();
//    }

    @Test
    public void testSendMovementMessage() {
        MovementSerialMessagePayload serialMessagePayload = new MovementSerialMessagePayload(
                MOVEMENT_START, DIRECTION_FORWARD, ROTATION_RIGHT, (byte)0);
        SerialMessage<MovementSerialMessagePayload> message = new SerialMessage<>(
                MOVEMENT, serialMessagePayload);

        communicationService.sendAsync(message);
    }
}
