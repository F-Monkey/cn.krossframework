package cn.krossframework.state.data;

import cn.krossframework.state.data.StateData;

public abstract class AbstractStateData implements StateData {

    protected long groupId;

    @Override
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

}
