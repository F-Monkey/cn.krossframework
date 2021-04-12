package cn.krossframework.game.state;

import cn.krossframework.state.AbstractState;
import cn.krossframework.state.StateInfo;
import cn.krossframework.state.Task;
import cn.krossframework.state.Time;

public class EndState extends AbstractState {

    public static final String CODE = "end";

    @Override
    public void init(Time time) {
        GameData stateData = this.getStateGroup().getStateData();
        for (Seat seat : stateData.getSeatList()) {
            seat.setReady(false);
        }
    }

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void update(Time time, StateInfo stateInfo) {
        GameData stateData = this.getStateGroup().getStateData();
        if (stateData.isFull() && stateData.isAllReady()) {
            stateInfo.isFinished = true;
        }
    }

    @Override
    public void handleTask(Time time, Task task) {

    }
}
