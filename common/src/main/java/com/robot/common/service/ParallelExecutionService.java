package com.robot.common.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.concurrent.*;

public class ParallelExecutionService {
    private final ExecutorService pooledExecutorService;
    private final ExecutorService cachedExecutorService;
    private final ScheduledExecutorService scheduledExecutorService;

    @Inject
    public ParallelExecutionService(
            @Named("executor.threads.count") Integer executorThreadCount,
            @Named("scheduler.threads.count") Integer schedulerThreadsCount) {
        pooledExecutorService = Executors.newFixedThreadPool(executorThreadCount);
        cachedExecutorService = Executors.newCachedThreadPool();
        scheduledExecutorService = Executors.newScheduledThreadPool(schedulerThreadsCount);
    }

    public void submit(Runnable task) {
        pooledExecutorService.submit(task);
    }

    public void submitLongRunning(Runnable task) {
        cachedExecutorService.submit(task);
    }

    public ScheduledFuture submitScheduled(
            Runnable task,
            long initialDelay,
            long delay,
            TimeUnit timeUnit) {
        return scheduledExecutorService.scheduleWithFixedDelay(task, initialDelay, delay, timeUnit);
    }
}
