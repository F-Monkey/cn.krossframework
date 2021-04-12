package cn.krossframework.state;

public interface StateData {
    void setGroupId(long groupId);

    default boolean isFull(){
        return false;
    }
}
