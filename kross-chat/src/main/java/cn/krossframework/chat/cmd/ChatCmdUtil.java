package cn.krossframework.chat.cmd;

import cn.krossframework.chat.state.ChatRoom;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.Entity;
import cn.krossframework.proto.chat.Chat;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.websocket.Character;
import cn.krossframework.websocket.User;
import com.google.common.base.Strings;
import com.google.protobuf.ProtocolStringList;

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

    public static Command.PackageGroup loginResult(int resultCode, String msg,
                                                   User user) {
        Chat.LoginResult.Builder builder = Chat.LoginResult.newBuilder();
        if (user != null) {
            String uid = user.getUid();
            if (!Strings.isNullOrEmpty(uid)) {
                builder.setUid(uid);
            }
            String username = user.getUsername();
            if (Strings.isNullOrEmpty(username)) {
                builder.setUsername(username);
            }
        }
        return CmdUtil.packageGroup(CmdUtil.pkg(resultCode, msg, ChatCmdType.LOGIN_RESULT, builder.build().toByteString()));
    }

    public static Chat.ChatRoomData chatRoomData(ChatRoom chatRoom) {
        Chat.ChatRoomData.Builder builder = Chat.ChatRoomData.newBuilder();
        List<Chat.ChatMessageResult> history = chatRoom.getHistory();
        List<Character> chatterList = chatRoom.getChatterList();
        builder.setId(chatRoom.getId());
        if (history != null) {
            builder.addAllHistory(history);
        }
        if (chatterList != null) {
            for (Character chatter : chatterList) {
                builder.addChatter(character(chatter));
            }
        }
        String master = chatRoom.getMaster();
        if (!Strings.isNullOrEmpty(master)) {
            builder.setMaster(master);
        }
        return builder.build();
    }

    public static Command.PackageGroup enterRoomResult(int resultCode, String msg, ChatRoom chatRoom) {
        Chat.ChatRoomData.Builder builder = Chat.ChatRoomData.newBuilder();
        if (chatRoom != null) {
            builder = chatRoomData(chatRoom).toBuilder();
        }
        return CmdUtil.packageGroup(CmdUtil.pkg(resultCode, msg, ChatCmdType.ENTER_ROOM_RESULT, builder.build().toByteString()));
    }

    public static Command.PackageGroup createRoomResult(int resultCode, String msg, ChatRoom chatRoom) {
        Chat.ChatRoomData.Builder builder = Chat.ChatRoomData.newBuilder();
        if (chatRoom != null) {
            builder = chatRoomData(chatRoom).toBuilder();
        }
        return CmdUtil.packageGroup(CmdUtil.pkg(resultCode, msg, ChatCmdType.CREATE_ROOM_RESULT, builder.build().toByteString()));
    }

    public static Chat.ChatMessageResult chatMessageResult(String from, Chat.ChatMessage chatMessage) {
        Chat.ChatMessageResult.Builder builder = Chat.ChatMessageResult.newBuilder();
        int emoticon = chatMessage.getEmoticon();
        builder.setEmoticon(emoticon);
        String msg = chatMessage.getMsg();
        builder.setFrom(from);
        if (!Strings.isNullOrEmpty(msg)) {
            builder.setMsg(msg);
        }
        Chat.Stream stream = chatMessage.getStream();
        if (!stream.equals(Chat.Stream.getDefaultInstance())) {
            builder.setStream(stream);
        }
        boolean toAll = chatMessage.getToAll();
        builder.setToAll(toAll);
        ProtocolStringList toList = chatMessage.getToList();
        if (!toList.isEmpty()) {
            builder.addAllTo(toList);
        }
        return builder.build();
    }

    public static Chat.ExistsResult existsResult(ChatRoom chatRoom) {
        Chat.ExistsResult.Builder builder = Chat.ExistsResult.newBuilder();
        if (chatRoom != null) {
            builder.setChatRoomData(chatRoomData(chatRoom));
        }
        return builder.build();
    }

    public static Chat.ClickOffResult clickOffResult(ChatRoom chatRoom) {
        Chat.ClickOffResult.Builder builder = Chat.ClickOffResult.newBuilder();
        if (chatRoom != null) {
            builder.setChatRoomData(chatRoomData(chatRoom));
        }
        return builder.build();
    }
}
