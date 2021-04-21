package cn.krossframework.game.state;

import cn.krossframework.proto.Command;
import cn.krossframework.state.util.FailCallBack;
import cn.krossframework.state.Task;
import cn.krossframework.websocket.Character;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

public class GameTask implements Task {

    private final Character character;
    private final Command.Cmd cmd;
    private final FailCallBack failCallBack;

    public GameTask(Character character, Command.Cmd cmd,
                    FailCallBack failCallBack) {
        Preconditions.checkNotNull(character);
        Preconditions.checkNotNull(cmd);
        Preconditions.checkNotNull(failCallBack);
        this.character = character;
        this.cmd = cmd;
        this.failCallBack = failCallBack;
    }

    public Character getCharacter() {
        return character;
    }

    @Override
    @Nullable
    public FailCallBack getFailCallBack() {
        return failCallBack;
    }

    public Command.Cmd getCmd() {
        return cmd;
    }
}
