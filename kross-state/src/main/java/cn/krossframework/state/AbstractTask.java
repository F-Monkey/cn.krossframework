package cn.krossframework.state;

import cn.krossframework.state.util.FailCallBack;

public abstract class AbstractTask implements Task {

    protected final Long groupId;

    protected final FailCallBack failCallBack;

    public AbstractTask(Long groupId,
                        FailCallBack failCallBack) {
        this.groupId = groupId;
        this.failCallBack = failCallBack;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public FailCallBack failCallBack() {
        return this.failCallBack;
    }
}
