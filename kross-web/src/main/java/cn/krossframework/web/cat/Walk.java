package cn.krossframework.web.cat;

import cn.krossframework.state.*;

public class Walk extends AbstractState {

    public static final String CODE = "walk";

    private int walkDistance;

    private int status;

    private int cmdCode;

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
        if (this.status == 1) {
            stateInfo.isFinished = true;
            return;
        }
        if (this.cmdCode == 2) {
            stateInfo.isFinished = true;
            return;
        }
        this.walkDistance -= 10;
        System.out.println("walking");
        if (this.walkDistance <= 0) {
            stateInfo.isFinished = true;
        }
    }

    @Override
    public void handleTask(Time time, Task task) {
        CatTask catTask = (CatTask) task;
        if (catTask.getCmd() == 1) {
            this.status = 1;
            return;
        }
        this.cmdCode = catTask.getCmd();
    }

    @Override
    public String finish(Time time) {
        if (this.status == 1) {
            return DefaultErrorState.CODE;
        }
        return Sleep.CODE;
    }
}
