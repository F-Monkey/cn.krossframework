package cn.krossframework.state.data;

import com.google.common.base.Preconditions;

public abstract class AbstractTask implements Task {

    protected final Long groupId;

    protected final Task task;


    public AbstractTask(Long groupId,
                        Task task) {
        Preconditions.checkNotNull(task);
        this.groupId = groupId;
        this.task = task;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public Task getTask() {
        return task;
    }

}
