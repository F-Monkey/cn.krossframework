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

    private final long period;

    private final ScheduledExecutorService scheduledExecutorService;

    public AutoTask(long period, int nThreads) {
        this.period = period;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(nThreads);
    }

    public void start() {
        this.scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                this.run();
            } catch (Throwable e) {
                log.error("autoTask run error:\n", e);
            }
        }, 0, this.period, TimeUnit.MILLISECONDS);
    }

    protected abstract void run();
}
