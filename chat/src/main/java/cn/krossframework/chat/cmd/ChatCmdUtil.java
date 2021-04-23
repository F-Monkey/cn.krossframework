package cn.krossframework.chat.cmd;

import cn.krossframework.chat.state.ChatRoom;
import cn.krossframework.chat.websocket.Chatter;
import cn.krossframework.commons.model.ResultCode;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.Entity;
import cn.krossframework.proto.chat.Chat;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.websocket.Character;

import java.util.List;

public class ChatCmdUtil {
    private ChatCmdUtil() {
    }

    private static Entity.Character character(Character chatter) {
        Entity.Character.Builder builder = Entity.Character.newBuilder();
        return builder.setId(chatter.getId())
                .setIsOnline(!chatter.isOffLine())
                .setNickName(chatter.getNickName())
                .build();
    }

    public static Command.PackageGroup enterRoomResult(String msg, ChatRoom chatRoom) {
        List<Chat.ChatMessageResult> history = chatRoom.getHistory();
        List<Character> chatterList = chatRoom.getChatterList();
        Chat.ChatRoomData.Builder builder = Chat.ChatRoomData.newBuilder();
        builder.setId(chatRoom.getId());
        if (history != null) {
            builder.addAllHistory(history);
        }
        if (chatterList != null) {
            for (Character chatter : chatterList) {
                builder.addChatter(character(chatter));
            }
        }
        return CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.SUCCESS, msg, ChatCmdType.LOGIN_RESULT, builder.build().toByteString()));
    }
}
