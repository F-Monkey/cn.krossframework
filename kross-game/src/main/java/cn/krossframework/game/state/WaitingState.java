package cn.krossframework.game.state;

import cn.krossframework.proto.CmdType;
import cn.krossframework.state.*;
import cn.krossframework.state.data.AbstractState;
import cn.krossframework.state.data.StateInfo;
import cn.krossframework.state.util.Time;
import cn.krossframework.websocket.Character;

public class WaitingState extends AbstractState {

    public static final String CODE = "waiting";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void handleTask(Time time, Task task) {
        GameTask gameTask = (GameTask) task;
        Command.Cmd cmd = gameTask.getCmd();
        int cmdType = cmd.getCmdType();
        switch (cmdType) {
            case CmdType.START_GAME:
                this.seatStart(gameTask.getCharacter());
                break;
            default:

        }
    }

    private void seatStart(Character character) {
        GameRoom stateGroup = super.getStateGroup();
        Seat seat = stateGroup.findSeat(character);
        if (seat == null) {
            character.sendMsg(null);
            return;
        }
        seat.setReady(true);
    }

    @Override
    public void update(Time time, StateInfo stateInfo) {
        GameData gameData = this.getStateGroup();
        if (gameData.isFull() && gameData.isAllReady()) {
            stateInfo.isFinished = true;
        }
    }
}
