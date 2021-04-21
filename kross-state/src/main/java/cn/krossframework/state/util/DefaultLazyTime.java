package cn.krossframework.state.util;

import com.google.common.base.Preconditions;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DefaultLazyTime implements Time {

    private final ScheduledExecutorService scheduledExecutorService;

    private final long period;

    private volatile long currentTime;

    public DefaultLazyTime(long period) {
        Preconditions.checkArgument(period > 0);
        this.period = period;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.refreshTime();
    }

    private void refreshTime() {
        this.scheduledExecutorService.scheduleAtFixedRate(() -> this.currentTime = System.currentTimeMillis(), 0, this.period, TimeUnit.MICROSECONDS);
    }

    @Override
    public long getCurrentTimeMillis() {
        return this.currentTime;
    }
}
