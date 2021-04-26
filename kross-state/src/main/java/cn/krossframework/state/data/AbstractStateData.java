package cn.krossframework.state.data;

public abstract class AbstractStateData implements StateData {

    protected long groupId;

    @Override
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

}
