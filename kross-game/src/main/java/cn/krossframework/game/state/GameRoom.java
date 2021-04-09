package cn.krossframework.game.state;

import cn.krossframework.state.AbstractStateGroup;
import cn.krossframework.state.StateGroupConfig;
import cn.krossframework.state.Task;
import cn.krossframework.state.Time;

public class GameRoom extends AbstractStateGroup {
    public GameRoom(long id, Time time, StateGroupConfig stateGroupConfig) {
        super(id, time, stateGroupConfig);
    }

    @Override
    public boolean tryAddTask(Task task) {
        if(task instanceof GameTask){
            super.tryAddTask(task);
        }
        return false;
    }
}
