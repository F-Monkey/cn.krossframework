package cn.krossframework.cat.server.state;

import cn.krossframework.proto.Command;
import cn.krossframework.state.*;

public class Walk extends AbstractState {

    public static final String CODE = "walk";

    private int walkDistance;

    private int status;

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void init(Time time) {
        this.walkDistance = 100;
    }

    @Override
    public void update(Time time, StateInfo stateInfo) {
        this.walkDistance -= 10;
        System.out.println("walking");
        if (this.walkDistance <= 0) {
            stateInfo.isFinished = true;
        }
    }

    @Override
    public void handleTask(Time time, Task task) {
        CatTask catTask = (CatTask) task;
        Command.Cmd cmd = catTask.getCmd();
        if (cmd.getCmdType() == 1) {
            this.status = 1;
        }
    }

    @Override
    public String finish(Time time) {
        if (this.status == 1) {
            return DefaultErrorState.CODE;
        }
        return Sleep.CODE;
    }
}
