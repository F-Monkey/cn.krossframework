package cn.krossframework.state.data;

public interface StateData {

    void setGroupId(long groupId);

    default boolean isFull() {
        return false;
    }

    default boolean isEmpty() {
        return true;
    }
}
