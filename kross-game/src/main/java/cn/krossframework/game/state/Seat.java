package cn.krossframework.game.state;

import cn.krossframework.commons.web.Character;

public class Seat {

    private final String seatName;

    private int score;

    private boolean isReady;

    private Character character;

    public Seat(String seatName) {
        this.seatName = seatName;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Character getCharacter() {
        return character;
    }

    public void setReady(boolean ready) {
        this.isReady = ready;
    }

    public boolean isReady() {
        return this.isReady;
    }

    public int getScore() {
        return score;
    }
}
