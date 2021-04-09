package cn.krossframework.state;

import com.google.common.base.Preconditions;

public class GroupIdTask implements Task {

    private final Long groupId;

    private final Task task;

    private final FailCallBack failCallBack;

    public GroupIdTask(Long groupId,
                       Task task,
                       FailCallBack failCallBack){
        Preconditions.checkNotNull(task);
        this.groupId = groupId;
        this.task = task;
        this.failCallBack = failCallBack;
    }

    public Task getTask() {
        return task;
    }

    public Long getGroupId() {
        return groupId;
    }

    public FailCallBack getFailCallBack() {
        return failCallBack;
    }
}
