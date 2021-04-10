package cn.krossframework.game.state;

import cn.krossframework.proto.Command;
import cn.krossframework.state.Task;
import cn.krossframework.websocket.Character;
import com.google.common.base.Preconditions;

public class GameTask implements Task {

    private final Character character;
    private final Command.Cmd cmd;

    public GameTask(Character character, Command.Cmd cmd) {
        Preconditions.checkNotNull(character);
        Preconditions.checkNotNull(cmd);
        this.character = character;
        this.cmd = cmd;
    }

    public Character getCharacter() {
        return character;
    }

    public Command.Cmd getCmd() {
        return cmd;
    }
}
