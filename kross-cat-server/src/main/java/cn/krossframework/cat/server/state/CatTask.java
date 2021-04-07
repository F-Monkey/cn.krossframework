package cn.krossframework.cat.server.state;

import cn.krossframework.proto.Command;
import cn.krossframework.state.Task;

public class CatTask implements Task {
    private final Command.Cmd cmd;

    public CatTask(Command.Cmd cmd) {
        this.cmd = cmd;
    }

    public Command.Cmd getCmd() {
        return cmd;
    }
}

