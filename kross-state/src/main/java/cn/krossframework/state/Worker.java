package cn.krossframework.state;

import com.google.common.base.Preconditions;

public interface Worker {

    class GroupIdTaskPair {
        private final Long groupId;
        private final Task task;

        public GroupIdTaskPair(Long groupId, Task task) {
            Preconditions.checkNotNull(task);
            this.groupId = groupId;
            this.task = task;
        }

        public Long getGroupId() {
            return groupId;
        }

        public Task getTask() {
            return task;
        }
    }

    long getId();

    void start();

    boolean isStart();

    boolean isFull();

    boolean isEmpty();

    void update();

    void stop();

    boolean tryAddStateGroup(StateGroup stateGroup);

    boolean tryAddTask(GroupIdTaskPair groupIdTaskPair);
}
