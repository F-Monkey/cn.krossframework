package cn.krossframework.commons.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * create a autoTask
 */
public abstract class AutoTask {

    private static final Logger log = LoggerFactory.getLogger(AutoTask.class);

    private static final long DEFAULT_PERIOD = 1000 * 60;

    private final long period;

    private final long nThreads;

    private final ScheduledExecutorService scheduledExecutorService;

    public AutoTask(long period, int nThreads) {
        this.nThreads = nThreads;
        this.period = period <= 0 ? DEFAULT_PERIOD : period;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(nThreads);
    }

    public void addTask(Runnable runnable) {
        for (int i = 0; i < this.nThreads; i++) {
            this.scheduledExecutorService.scheduleAtFixedRate(runnable, 0, this.period, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        this.scheduledExecutorService.shutdownNow();
    }
}
