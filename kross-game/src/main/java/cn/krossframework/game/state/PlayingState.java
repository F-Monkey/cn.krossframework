package cn.krossframework.game.state;

import cn.krossframework.game.util.GameCmdUtil;
import cn.krossframework.state.*;

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
        GameRoom stateGroup = this.getStateGroup();
        stateGroup.broadCastMsg(null, GameCmdUtil.gameStart(stateGroup));
    }

    @Override
    public void update(Time time, StateInfo stateInfo) {
        if (this.hasWinner) {
            stateInfo.isFinished = true;
        }
    }
}
