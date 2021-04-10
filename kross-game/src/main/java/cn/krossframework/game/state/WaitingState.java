package cn.krossframework.game.state;

import cn.krossframework.state.AbstractState;
import cn.krossframework.state.StateData;
import cn.krossframework.state.StateInfo;
import cn.krossframework.state.Time;

public class WaitingState extends AbstractState {

    public static final String CODE = "waiting";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void update(Time time, StateInfo stateInfo) {
        GameData gameData = this.getStateData();
        if (gameData.isFull()) {
            stateInfo.isFinished = true;
        }
    }
}