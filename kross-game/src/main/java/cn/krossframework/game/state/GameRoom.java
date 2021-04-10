package cn.krossframework.game.state;

import cn.krossframework.game.util.GameCmdUtil;
import cn.krossframework.proto.ResultCode;
import cn.krossframework.state.AbstractStateGroup;
import cn.krossframework.state.StateGroupConfig;
import cn.krossframework.state.Task;
import cn.krossframework.state.Time;
import cn.krossframework.websocket.Character;

public class GameRoom extends AbstractStateGroup {

    public GameRoom(long id, Time time, StateGroupConfig stateGroupConfig) {
        super(id, time, stateGroupConfig);
    }

    @Override
    public boolean tryAddTask(Task task) {
        if (task instanceof GameTask) {
            super.tryAddTask(task);
        }
        return false;
    }

    @Override
    public boolean tryEnterGroup(Task task) {
        if (task instanceof GameTask) {
            Character character = ((GameTask) task).getCharacter();
            if (!super.tryEnterGroup(task)) {
                character.sendMsg(GameCmdUtil.enterResult(ResultCode.FAIL, "enter fail"));
                return false;
            }
            GameData gameData = this.currentState.getStateData();
            for (Seat seat : gameData.getSeatList()) {
                if (seat.getCharacter() == null) {
                    seat.setCharacter(character);
                    character.sendMsg(GameCmdUtil.enterResult(ResultCode.SUCCESS, "enter ok"));
                    return true;
                }
            }
        }
        return false;
    }
}
