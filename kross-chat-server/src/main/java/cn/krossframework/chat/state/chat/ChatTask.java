package cn.krossframework.chat.state.chat;

import cn.krossframework.proto.Command;
import cn.krossframework.state.Task;
import cn.krossframework.websocket.Character;

public class ChatTask implements Task {

    private final Character character;
    private final Command.Cmd cmd;

    public ChatTask(Character character,
                    Command.Cmd cmd) {
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