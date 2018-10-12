package service;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SerialCommunicationService {
    private static final int MAX_QUEUE_LENGTH = 50;
    private static final Logger logger = LogManager.getLogger(SerialCommunicationService.class);

    private final LinkedBlockingQueue<SerialTask> messagesQueue;
    private final ParallelExecutionService parallelExecutionService;
    private final AtomicBoolean started;

    @Inject
    public SerialCommunicationService(ParallelExecutionService parallelExecutionService) {
        this.parallelExecutionService = parallelExecutionService;
        messagesQueue = new LinkedBlockingQueue<>();
        started = new AtomicBoolean(false);
    }

    public void start() {
        if (started.get()) {
            logger.warn("Trying to start working service");
            return;
        }
        started.set(true);
        parallelExecutionService.submitLongRunning(this::awaitMessages);
    }

    public void stop() {
        if (!started.get()) {
            logger.warn("Trying to stop non-working service");
            return;
        }
        started.set(false);
    }

    public void sendAsync(byte[] message) {
        sendAsync(message, null);
    }

    public void sendAsync(byte[] message, Consumer<byte[]> callback) {
        if (messagesQueue.size() >= MAX_QUEUE_LENGTH) {
            try {
                logger.warn("Serial message queue overflow");
                messagesQueue.poll(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Unable to remove old message", e);
                return;
            }
        }
        messagesQueue.add(new SerialTask(message, callback));
    }

    private void awaitMessages() {
        while (started.get()) {
            try {
                SerialTask task = messagesQueue.poll(3, TimeUnit.SECONDS);
                if (task == null) {
                    continue;
                }
                writeSerial(task.message);

                if (task.callback != null) {
                    byte[] response = readSerial();
                    parallelExecutionService.submit(() -> task.callback.accept(response));
                }
            } catch (InterruptedException ignored) {

            } catch (Exception e) {
                logger.error("Unable to send message", e);
            }
        }
    }

    private void writeSerial(byte[] message) {
        //TODO write serial message
        throw new NotImplementedException();
    }

    private byte[] readSerial() {
        //TODO read serial message
        throw new NotImplementedException();
    }

    private static class SerialTask {
        private final byte[] message;
        private final Consumer<byte[]> callback;

        private SerialTask(byte[] message, Consumer<byte[]> callback) {
            this.message = message;
            this.callback = callback;
        }
    }
}
