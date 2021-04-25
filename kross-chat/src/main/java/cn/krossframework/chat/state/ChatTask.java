package cn.krossframework.chat.state;

import cn.krossframework.proto.Command;
import cn.krossframework.state.data.Task;
import cn.krossframework.websocket.Character;
import com.google.common.base.Preconditions;

public class ChatTask implements Task {

    private final Character character;
    private final Command.Package cmd;

    public ChatTask(Character character, Command.Package cmd) {
        Preconditions.checkNotNull(character);
        Preconditions.checkNotNull(cmd);
        this.character = character;
        this.cmd = cmd;
    }

    public Character getCharacter() {
        return character;
    }

    public Command.Package getCmd() {
        return cmd;
    }
}
