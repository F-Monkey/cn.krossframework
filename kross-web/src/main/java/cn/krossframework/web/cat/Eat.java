package cn.krossframework.web.cat;

import cn.krossframework.state.*;

public class Eat extends AbstractState {

    public static final String CODE = "eat";

    private int foodCount;

    private int status;

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void init(Time time) {
        this.foodCount = 10;
    }

    @Override
    public void handleTask(Time time, Task task) {
        CatTask catTask = (CatTask) task;
        if (catTask.getCmd() == 1) {
            this.status = 1;
        }
    }

    @Override
    public void update(Time time, StateInfo stateInfo) {
        if (this.status == 1) {
            stateInfo.isFinished = true;
            return;
        }
        this.foodCount -= 1;
        System.out.println("eating");
        if (this.foodCount <= 0) {
            stateInfo.isFinished = true;
        }
    }

    @Override
    public String finish(Time time) {
        if (this.status == 1) {
            return DefaultErrorState.CODE;
        }
        return Walk.CODE;
    }
}

