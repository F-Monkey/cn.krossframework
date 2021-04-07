package cn.krossframework.state;

import cn.krossframework.commons.thread.AutoTask;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class AbstractStateGroupPool implements StateGroupPool {

    private static final Logger log = LoggerFactory.getLogger(AbstractStateGroup.class);

    private static final AtomicLong ID_COUNT = new AtomicLong(0);

    public static final long DEFAULT_PERIOD = 1000 * 60;

    private final StateGroupFactory stateGroupFactory;

    private volatile ConcurrentHashMap<Long, StateGroup> stateGroupMap;


    /**
     * remove deposed stateGroup period
     */
    private Long period;

    public AbstractStateGroupPool(StateGroupFactory stateGroupFactory) {
        Preconditions.checkNotNull(stateGroupFactory);
        this.stateGroupFactory = stateGroupFactory;
        this.stateGroupMap = new ConcurrentHashMap<>();
    }

    @Override
    public StateGroupFactory getStateGroupFactory() {
        return this.stateGroupFactory;
    }

    @Override
    public void setRemoveDeposedStateGroupPeriod(long period) {
        this.period = period;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new AutoTask(this.period == null ? DEFAULT_PERIOD : this.period, 2) {
            @Override
            protected void run() {
                AbstractStateGroupPool.this.removeDeposedStateGroup();
            }
        }.start();
    }

    @Override
    public void removeDeposedStateGroup() {
        final ConcurrentHashMap<Long, StateGroup> stateGroupMap = new ConcurrentHashMap<>(this.stateGroupMap);
        if (stateGroupMap.size() == 0) {
            return;
        }
        stateGroupMap.entrySet().removeIf(e -> {
            boolean b = e.getValue().canDeposed();
            if (b) {
                log.info("stateGroup can be deposed, id: {}", e.getKey());
            }
            return b;
        });
        this.stateGroupMap = stateGroupMap;
    }

    @Override
    public FetchStateGroup findOrCreate(Long id) {
        if (id == null) {
            id = ID_COUNT.incrementAndGet();
        }
        boolean[] isNew = {false};
        final ConcurrentHashMap<Long, StateGroup> stateGroupMap = this.stateGroupMap;
        StateGroup stateGroup = stateGroupMap.computeIfAbsent(id, (i) -> {
            isNew[0] = true;
            return this.getStateGroupFactory().create(i);
        });
        log.info("find or create stateGroup, stateGroup id: {}, isNew: {}", stateGroup.getId(), isNew[0]);
        this.stateGroupMap = stateGroupMap;
        return new FetchStateGroup(isNew[0], stateGroup);
    }
}
