package org.fogbowcloud.app.utils;

import org.apache.log4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ManagerTimer {

    private static final Logger LOGGER = Logger.getLogger(ManagerTimer.class);
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> future;

    public ManagerTimer(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    public void scheduleAtFixedRate(final Runnable task, long delay, long period) {
        this.future =
                executor.scheduleWithFixedDelay(
                        () -> {
                            try {
                                task.run();
                            } catch (Throwable e) {
                                LOGGER.error(
                                        "Failed while executing timer task: " + e.getMessage(), e);
                            }
                        },
                        delay,
                        period,
                        TimeUnit.MILLISECONDS);
    }

    public void cancel() {
        if (future != null) {
            future.cancel(false);
        }
        future = null;
    }

    public boolean isScheduled() {
        return future != null && !future.isCancelled();
    }
}
