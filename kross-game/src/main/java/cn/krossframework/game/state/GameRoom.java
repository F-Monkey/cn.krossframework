package cn.krossframework.game.state;

import cn.krossframework.game.util.GameCmdUtil;
import cn.krossframework.proto.ResultCode;
import cn.krossframework.state.AbstractStateGroup;
import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.state.Task;
import cn.krossframework.state.util.Time;
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

    public void broadCastMsg(String excludeCharacterId, Object msg) {
        GameData gameData = (GameData) super.stateData;
        for (Seat seat : gameData.getSeatList()) {
            Character character = seat.getCharacter();
            if (character == null) {
                continue;
            }
            if (excludeCharacterId != null && excludeCharacterId.equals(character.getId())) {
                continue;
            }
            character.sendMsg(msg);
        }
    }

    @Override
    public boolean tryEnterGroup(Task task) {
        if (!super.tryEnterGroup(task)) {
            return false;
        }
        if (task instanceof GameTask) {
            Character character = ((GameTask) task).getCharacter();
            GameData gameData = (GameData) super.stateData;
            Seat emptySeat = gameData.findEmptySeat();
            if (emptySeat == null) {
                return false;
            }
            emptySeat.setCharacter(character);
            character.sendMsg(GameCmdUtil.enterResult
                    (ResultCode.SUCCESS, "enter ok"));
            this.broadCastMsg(character.getId(), GameCmdUtil.enterResult(ResultCode.SUCCESS, "player: " + character.getNickName() + " enter room"));
        }
        return false;
    }

    public Seat findSeat(Character character) {
        GameData gameData = (GameData) super.stateData;
        for (Seat seat : gameData.getSeatList()) {
            Character existsCharacter = seat.getCharacter();
            if (existsCharacter == null) {
                continue;
            }
            if (character.getId().equals(existsCharacter.getId())) {
                return seat;
            }
        }
        return null;
    }
}
