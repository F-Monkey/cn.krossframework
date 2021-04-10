package cn.krossframework.game.state;

import cn.krossframework.state.AbstractState;
import cn.krossframework.state.StateInfo;
import cn.krossframework.state.Time;

public class PlayingState extends AbstractState {

    public static final String CODE = "playing";

    private boolean hasWinner;

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void init(Time time) {
        this.hasWinner = false;
    }

    @Override
    public void update(Time time, StateInfo stateInfo) {
        if (this.hasWinner) {
            stateInfo.isFinished = true;
        }
    }
}
