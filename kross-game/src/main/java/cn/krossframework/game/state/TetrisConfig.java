package cn.krossframework.game.state;

import cn.krossframework.state.StateData;
import cn.krossframework.state.StateGroupConfig;

import java.util.ArrayList;

public class TetrisConfig implements StateGroupConfig {

    @Override
    public StateData createStateData() {
        ArrayList<Seat> seatList = new ArrayList<>(2);
        seatList.add(new Seat("PLAYER_1"));
        seatList.add(new Seat("PLAYER_2"));
        return new GameData(seatList);
    }
}
