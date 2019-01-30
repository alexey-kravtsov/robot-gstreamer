package com.robot.engineeringrobot;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.robot.network.service.NetworkCommunicationService;
import com.robot.serial.service.SerialCommunicationService;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.SocketException;

public class EngineeringRobotController {
    private static final Logger logger = LogManager.getLogger(EngineeringRobotController.class);

    public static void main(String[] args) throws SocketException, ConfigurationException {
        Configurations configs = new Configurations();
        Configuration config = configs.properties(new File("config.properties"));

        Injector injector = Guice.createInjector(new RobotBootstrapModule());

        SerialCommunicationService serialCommunicationService =
                injector.getInstance(SerialCommunicationService.class);
        serialCommunicationService.start();

        NetworkCommunicationService networkCommunicationService =
                injector.getInstance(NetworkCommunicationService.class);
        networkCommunicationService.start(config.getInt("udp.message.receiver.listen.port"));

        logger.info("Started");
    }
}