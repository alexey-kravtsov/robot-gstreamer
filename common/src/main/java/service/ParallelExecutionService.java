package service;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelExecutionService {
    private final ExecutorService pooledExecutorService;
    private final ExecutorService cachedExecutorService;

    @Inject
    public ParallelExecutionService(
            @Named("threads.count") Integer threadCount) {
        pooledExecutorService = Executors.newFixedThreadPool(threadCount);
        cachedExecutorService = Executors.newCachedThreadPool();
    }

    public void submit(Runnable task) {
        pooledExecutorService.submit(task);
    }

    public void submitLongRunning(Runnable task) {
        cachedExecutorService.submit(task);
    }
}
