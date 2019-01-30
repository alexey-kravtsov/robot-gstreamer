package com.robot.operator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.robot.network.service.NetworkCommunicationService;

import javax.swing.*;
import java.net.SocketException;

public class OperatorDashboard {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new OperatorBootstrapModule());

        SwingUtilities.invokeLater(() -> {
            DashboardController dashboardController = injector.getInstance(DashboardController.class);
            DashboardView dashboardView = dashboardController.getView();
            dashboardView.setSize(640, 530);
            dashboardView.setVisible(true);
        });
    }
}
