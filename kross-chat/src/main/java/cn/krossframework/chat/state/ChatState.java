package cn.krossframework.chat.state;

import cn.krossframework.chat.cmd.ChatCmdType;
import cn.krossframework.chat.cmd.ChatCmdUtil;
import cn.krossframework.commons.model.ResultCode;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.chat.Chat;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.state.data.AbstractState;
import cn.krossframework.state.data.Task;
import cn.krossframework.state.util.Time;
import cn.krossframework.websocket.Character;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class ChatState extends AbstractState {

    public static final String CODE = "chat";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void handleTask(Time time, Task task) {
        ChatTask chatTask = (ChatTask) task;
        Character character = chatTask.getCharacter();
        Command.Package cmd = chatTask.getCmd();
        int cmdType = cmd.getCmdType();
        switch (cmdType) {
            case ChatCmdType.SEND_MESSAGE:
                this.handleSendMessage(character, cmd);
                return;
            default:
                super.handleTask(time, task);
        }
    }

    private void handleSendMessage(Character character, Command.Package cmd) {
        ByteString content = cmd.getContent();
        Chat.ChatMessage chatMessage;
        try {
            chatMessage = Chat.ChatMessage.parseFrom(content);
        } catch (InvalidProtocolBufferException e) {
            character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.ERROR, "invalid sendMsg content", ChatCmdType.SEND_MESSAGE_RESULT, null)));
            return;
        }
        ChatRoom chatRoom = this.getStateGroup();
        String id = character.getId();
        Chat.ChatMessageResult chatMessageResult = ChatCmdUtil.chatMessageResult(id, chatMessage);
        chatRoom.addChatMessage(chatMessageResult);
        Command.PackageGroup packageGroup = CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.SUCCESS, "", ChatCmdType.SEND_MESSAGE_RESULT, chatMessageResult.toByteString()));
        character.sendMsg(packageGroup);
        chatRoom.broadCast(id, packageGroup);
    }
}
