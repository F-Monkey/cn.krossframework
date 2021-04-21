package cn.krossframework.state.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.Duration;
import java.util.Optional;

public abstract class AbstractStateGroupConfigPool implements StateGroupConfigPool {

    protected final LoadingCache<String, Optional<StateGroupConfig>> stateGroupCache;

    public AbstractStateGroupConfigPool() {
        this.stateGroupCache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .build(new CacheLoader<String, Optional<StateGroupConfig>>() {
                    @Override
                    public Optional<StateGroupConfig> load(String key) throws Exception {
                        return Optional.ofNullable(AbstractStateGroupConfigPool.this.findStateGroupConfig(key));
                    }
                });
    }

    protected abstract StateGroupConfig findStateGroupConfig(String id);

    @Override
    public StateGroupConfig find(final String id) {
        return this.stateGroupCache.getUnchecked(id).orElseThrow(() -> new NullPointerException("can not find stateGroupConfig by id: " + id));
    }
}
