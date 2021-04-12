package cn.krossframework.state;

public abstract class AbstractStateData implements StateData {

    protected long groupId;

    @Override
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

}
