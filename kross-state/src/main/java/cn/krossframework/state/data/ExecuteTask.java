package cn.krossframework.state.data;

import cn.krossframework.state.Task;
import cn.krossframework.state.util.FailCallBack;
import com.google.common.base.Preconditions;

public class ExecuteTask implements Task {

    private final Long groupId;

    private final Task task;

    private final FailCallBack failCallBack;

    public ExecuteTask(Long groupId,
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
