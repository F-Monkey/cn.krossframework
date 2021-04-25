package cn.krossframework.state.data;

import cn.krossframework.state.util.FailCallBack;
import com.google.common.base.Preconditions;

public class ExecuteTask extends AbstractTask {

    public ExecuteTask(Long groupId,
                       Task task,
                       FailCallBack failCallBack) {
        super(groupId, task, failCallBack);
        Preconditions.checkNotNull(groupId);
        Preconditions.checkNotNull(task);
    }
}
