package cn.krossframework.state.data;

import com.google.common.base.Preconditions;

public class ExecuteTask extends AbstractTask {

    public ExecuteTask(Long groupId,
                       Task task) {
        super(groupId, task);
        Preconditions.checkNotNull(groupId);
    }
}
