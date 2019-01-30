package com.robot.serial.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;

class SerialCommunicator {

    private static final Logger logger = LogManager.getLogger(SerialCommunicator.class);

    private final String portName;
    private final Integer baudRate;
    private final Integer portOpenTimeoutMs;

    private SerialPort serialPort;

    public SerialCommunicator(String portName, Integer baudRate, Integer portOpenTimeoutMs) {
        this.portName = portName;
        this.baudRate = baudRate;
        this.portOpenTimeoutMs = portOpenTimeoutMs;
    }


    public void connect() throws SerialPortException {
        serialPort = new SerialPort(portName);

        serialPort.openPort();

        serialPort.setParams(
                baudRate,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        serialPort.setFlowControlMode(
                SerialPort.FLOWCONTROL_RTSCTS_IN |
                SerialPort.FLOWCONTROL_RTSCTS_OUT);

        try {
            Thread.sleep(portOpenTimeoutMs);
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    public void disconnect() throws SerialPortException {
        if (serialPort != null) {
            serialPort.closePort();
        }
    }

    public void write(byte[] data) throws IOException {
        if (serialPort == null) {
            throw new IOException("Port not opened");
        }

        try {
            serialPort.writeBytes(data);
        } catch (SerialPortException e) {
            throw new IOException(e);
        }
    }

    public byte[] read() {
        throw new NotImplementedException();
    }
}
