package cn.krossframework.game.state;

import cn.krossframework.state.AbstractStateData;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class GameData extends AbstractStateData {

    private final ImmutableList<Seat> seatList;

    public GameData(List<Seat> seatList) {
        this.seatList = ImmutableList.copyOf(seatList);
    }

    public ImmutableList<Seat> getSeatList() {
        return seatList;
    }

    @Override
    public boolean isFull() {
        for (Seat seat : this.getSeatList()) {
            if (seat.getCharacter() == null) {
                return false;
            }
        }
        return true;
    }
}