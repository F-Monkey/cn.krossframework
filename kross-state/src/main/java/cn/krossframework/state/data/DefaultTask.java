package cn.krossframework.state.data;

import cn.krossframework.state.util.FailCallBack;

public class DefaultTask extends AbstractTask {
    public DefaultTask(Long groupId, Task task, FailCallBack failCallBack) {
        super(groupId, task, failCallBack);
    }
}
