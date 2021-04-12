package cn.krossframework.web.cat;

import cn.krossframework.state.AbstractStateGroup;
import cn.krossframework.state.StateGroupConfig;
import cn.krossframework.state.Task;
import cn.krossframework.state.Time;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Cat extends AbstractStateGroup {

    public Cat(long id, Time time, StateGroupConfig stateGroupConfig) {
        super(id, time, stateGroupConfig);
    }

    @Override
    protected BlockingQueue<Task> initTaskQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Override
    public boolean tryEnterGroup(Task task) {
        return super.tryEnterGroup(task);
    }

    @Override
    public boolean tryAddTask(Task task) {
        if (task instanceof CatTask) {
            return super.tryAddTask(task);
        }
        return false;
    }

    @Override
    public void update() {
        System.out.println(this.id + " update");
        super.update();
    }
}
