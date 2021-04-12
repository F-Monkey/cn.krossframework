package cn.krossframework.web.cat;

import cn.krossframework.state.FailCallBack;
import cn.krossframework.state.Task;

public class CatTask implements Task {
    private final int cmd;

    public CatTask(int cmd) {
        this.cmd = cmd;
    }

    public int getCmd() {
        return cmd;
    }

    @Override
    public FailCallBack getFailCallBack() {
        return null;
    }
}

