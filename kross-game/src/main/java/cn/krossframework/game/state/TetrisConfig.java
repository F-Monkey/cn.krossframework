package cn.krossframework.game.state;

import cn.krossframework.state.data.StateData;
import cn.krossframework.state.config.StateGroupConfig;

import java.util.ArrayList;

public class TetrisConfig implements StateGroupConfig {

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getStartState() {
        return WaitingState.CODE;
    }

    @Override
    public StateData createStateData() {
        ArrayList<Seat> seatList = new ArrayList<>(2);
        seatList.add(new Seat("PLAYER_1"));
        seatList.add(new Seat("PLAYER_2"));
        return new GameData(seatList);
    }
}
