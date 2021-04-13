package cn.krossframework.web.cat;

import cn.krossframework.commons.web.Character;
import cn.krossframework.state.FailCallBack;
import cn.krossframework.state.Task;

public class CatTask implements Task {
    private final int cmd;

    public CatTask(int cmd) {
        this.cmd = cmd;
    }

    public Character getCharacter() {
        return null;
    }

    public int getCmd() {
        return this.cmd;
    }

    @Override
    public FailCallBack getFailCallBack() {
        return null;
    }
}

