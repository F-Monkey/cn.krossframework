package cn.krossframework.cat.server.state;

import cn.krossframework.proto.Command;
import cn.krossframework.state.*;

public class Sleep extends AbstractState {

    public static final String CODE = "sleep";

    private int sleepTime;

    private int status;

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void init(Time time) {
        this.sleepTime = 100;
    }

    @Override
    public void update(Time time, StateInfo stateInfo) {
        this.sleepTime -= 20;
        System.out.println("sleeping");
        if (this.sleepTime <= 0) {
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
        return Eat.CODE;
    }
}
