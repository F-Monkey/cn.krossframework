package cn.krossframework.state.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public abstract class AbstractStateGroupConfig implements StateGroupConfig {

    protected final String id;

    public AbstractStateGroupConfig(String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
