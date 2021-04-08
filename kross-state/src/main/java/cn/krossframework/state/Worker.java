package cn.krossframework.state;

import com.google.common.base.Preconditions;

public interface Worker {

    class GroupIdTaskPair {
        private final Long groupId;
        private final Task task;
        private final FailCallBack callBack;

        public GroupIdTaskPair(Long groupId,
                               Task task,
                               FailCallBack failCallBack) {
            Preconditions.checkNotNull(task);
            this.groupId = groupId;
            this.task = task;
            this.callBack = failCallBack;
        }

        public Long getGroupId() {
            return groupId;
        }

        public Task getTask() {
            return task;
        }

        public FailCallBack getCallBack() {
            return callBack;
        }
    }

    long getId();

    void start();

    boolean isStart();

    default boolean isFull() {
        return false;
    }

    default boolean isEmpty() {
        return false;
    }

    void update();

    void stop();

}
