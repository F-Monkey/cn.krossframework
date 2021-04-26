package cn.krossframework.chat.state;

import cn.krossframework.chat.cmd.ChatCmdType;
import cn.krossframework.chat.cmd.ChatCmdUtil;
import cn.krossframework.chat.state.data.ChatTask;
import cn.krossframework.commons.model.ResultCode;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.chat.Chat;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.state.StateGroup;
import cn.krossframework.state.data.AbstractState;
import cn.krossframework.state.data.DefaultErrorState;
import cn.krossframework.state.data.StateInfo;
import cn.krossframework.state.data.Task;
import cn.krossframework.state.util.Time;
import cn.krossframework.websocket.Character;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ProtocolStringList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
            case ChatCmdType.EXISTS_ROOM:
                this.handleExists(character, cmd);
                return;
            case ChatCmdType.CLICK_OFF:
                this.handleClickOff(character, cmd);
                return;
            default:
                try {
                    super.handleTask(time, task);
                } catch (UnsupportedOperationException e) {
                    character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL, "bad cmd", cmdType, null)));
                }
        }
    }

    private void handleClickOff(Character character, Command.Package cmd) {
        ChatRoom chatRoom = this.getStateGroup();
        String master = chatRoom.getMaster();
        if (!character.getId().equals(master)) {
            character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL, "you are not room master", ChatCmdType.CLICK_OFF_RESULT, null)));
            return;
        }
        ByteString content = cmd.getContent();
        Chat.ClickOff clickOff;
        try {
            clickOff = Chat.ClickOff.parseFrom(content);
        } catch (InvalidProtocolBufferException e) {
            character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.ERROR, "invalid sendMsg content", ChatCmdType.CLICK_OFF, null)));
            return;
        }
        ProtocolStringList characterIdList = clickOff.getCharacterIdList();
        if (characterIdList.isEmpty()) {
            character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL, "please input click off user id", ChatCmdType.CLICK_OFF_RESULT, null)));
            return;
        }
        if (characterIdList.contains(master)) {
            character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.FAIL, "you can not click off your self", ChatCmdType.CLICK_OFF_RESULT, null)));
            return;
        }
        String clickOffMsgTemplate = "%s have been click off";
        List<String> idList = new ArrayList<>();
        Chat.ClickOffResult clickOffResult = ChatCmdUtil.clickOffResult(chatRoom);
        for (String id : characterIdList) {
            List<Character> chatterList = chatRoom.getChatterList();
            Iterator<Character> iterator = chatterList.iterator();
            while (iterator.hasNext()) {
                Character c = iterator.next();
                if (id.equals(c.getId())) {
                    idList.add(id);
                    iterator.remove();
                    c.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.SUCCESS, String.format(clickOffMsgTemplate, "you"), ChatCmdType.CLICK_OFF_RESULT, null)));
                }
            }
        }
        Command.PackageGroup broadCastMsg = CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.SUCCESS, String.format(clickOffMsgTemplate, idList), ChatCmdType.CLICK_OFF_RESULT, clickOffResult.toByteString()));
        chatRoom.broadCast(broadCastMsg);
    }

    private void handleExists(Character character, Command.Package cmd) {
        ChatRoom chatRoom = this.getStateGroup();
        chatRoom.getChatterList().remove(character);
        character.setCurrentGroupId(null);
        character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.SUCCESS, "exists room: " + chatRoom.getId(),
                ChatCmdType.EXISTS_ROOM_RESULT, null)));
        chatRoom.broadCast(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.SUCCESS, character.getId() + " exists", ChatCmdType.EXISTS_ROOM_RESULT,
                ChatCmdUtil.existsResult(chatRoom).toByteString())), character.getId());
    }

    private void handleSendMessage(Character character, Command.Package cmd) {
        ByteString content = cmd.getContent();
        Chat.ChatMessage chatMessage;
        try {
            chatMessage = Chat.ChatMessage.parseFrom(content);
        } catch (InvalidProtocolBufferException e) {
            character.sendMsg(CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.ERROR, "invalid sendMsg content", ChatCmdType.SEND_MESSAGE, null)));
            return;
        }
        ChatRoom chatRoom = this.getStateGroup();
        String id = character.getId();
        Chat.ChatMessageResult chatMessageResult = ChatCmdUtil.chatMessageResult(id, chatMessage);
        chatRoom.addChatMessage(chatMessageResult);
        Command.PackageGroup packageGroup = CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.SUCCESS, "", ChatCmdType.SEND_MESSAGE_RESULT, chatMessageResult.toByteString()));
        character.sendMsg(packageGroup);
        chatRoom.broadCast(packageGroup, id);
    }

    @Override
    public void update(Time time, StateInfo stateInfo) {
        ChatRoom chatRoom = this.getStateGroup();
        if (chatRoom.getChatterList().isEmpty()) {
            stateInfo.isFinished = true;
        }
    }

    @Override
    public String finish(Time time) {
        return DefaultErrorState.CODE;
    }
}
