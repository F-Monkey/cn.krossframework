package cn.krossframework.chat.state;

import cn.krossframework.chat.cmd.ChatCmdUtil;
import cn.krossframework.proto.*;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.state.AbstractState;
import cn.krossframework.state.Task;
import cn.krossframework.state.Time;
import cn.krossframework.websocket.Character;
import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class ChatState extends AbstractState {

    public static final String CODE = "chat";

    private final RoomData stateData;

    public ChatState(RoomData stateData) {
        Preconditions.checkNotNull(stateData);
        this.stateData = stateData;
    }

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void handleTask(Time time, Task task) {
        ChatTask chatTask = (ChatTask) task;
        Command.Cmd cmd = chatTask.getCmd();
        Character character = chatTask.getCharacter();
        int cmdType = cmd.getCmdType();
        switch (cmdType) {
            case CmdType.SEND_MESSAGE:
                this.sendMessage(character, cmd);
                break;
            case CmdType.ENTER:
                this.enterRoom(character, cmd);
                break;
            default:
                character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL,
                        "invalid cmdType",
                        cmdType, null)));
        }
    }

    private void enterRoom(Character character, Command.Cmd cmd) {
        this.stateData.addCharacter(character);
        character.sendMsg(ChatCmdUtil.enterRoomResult(ResultCode.SUCCESS, "enter room"));
        Command.PackageGroup enterRoom = ChatCmdUtil.enterRoomResult(ResultCode.SUCCESS, character.getNickName() + " enter room");
        for (Character other : this.stateData.getChatterList()) {
            if (character.getId().equals(other.getId())) {
                continue;
            }
            character.sendMsg(enterRoom);
        }
    }

    private void sendMessage(Character character, Command.Cmd cmd) {
        ByteString content = cmd.getContent();
        Chat.ChatMessage chatMessage;
        try {
            chatMessage = Chat.ChatMessage.parseFrom(content);
        } catch (InvalidProtocolBufferException e) {
            character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL,
                    "invalid content",
                    cmd.getCmdType(),
                    null)));
            log.error("body parse error:\n", e);
            return;
        }
        String id = character.getId();
        for (Character other : this.stateData.getChatterList()) {
            if (id.equals(other.getId())) {
                continue;
            }
            character.sendMsg(ChatCmdUtil.sendMsgResult(ResultCode.SUCCESS, "ok", id, chatMessage));
        }
    }
}
