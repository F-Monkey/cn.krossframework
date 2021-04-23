package cn.krossframework.state.data;

import cn.krossframework.state.AbstractTask;
import cn.krossframework.state.Task;
import cn.krossframework.state.util.FailCallBack;
import com.google.common.base.Preconditions;

public class ExecuteTask extends AbstractTask {

    protected final Task task;

    public ExecuteTask(Long groupId,
                       Task task,
                       FailCallBack failCallBack) {
        super(groupId, failCallBack);
        Preconditions.checkNotNull(groupId);
        Preconditions.checkNotNull(task);
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public Long getGroupId() {
        return groupId;
    }
}
