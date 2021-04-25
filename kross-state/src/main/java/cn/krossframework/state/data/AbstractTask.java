package cn.krossframework.state.data;

import cn.krossframework.state.util.FailCallBack;
import com.google.common.base.Preconditions;

public abstract class AbstractTask implements Task {

    protected final Long groupId;

    protected final Task task;

    protected final FailCallBack failCallBack;

    public AbstractTask(Long groupId,
                        Task task,
                        FailCallBack failCallBack) {
        Preconditions.checkNotNull(task);
        this.groupId = groupId;
        this.task = task;
        this.failCallBack = failCallBack;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public Task getTask() {
        return task;
    }

    public FailCallBack failCallBack() {
        return this.failCallBack;
    }
}
