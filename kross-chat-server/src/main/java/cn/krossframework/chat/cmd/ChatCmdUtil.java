package cn.krossframework.chat.cmd;

import cn.krossframework.proto.Chat;
import cn.krossframework.proto.CmdType;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.ResultCode;
import com.google.common.base.Strings;
import com.google.protobuf.ProtocolStringList;
import org.springframework.util.CollectionUtils;

public class ChatCmdUtil {
    private ChatCmdUtil() {
    }

    public static Command.PackageGroup clickOff() {
        Command.Package.Builder builder = Command.Package.newBuilder();
        builder.setCmdType(CmdType.CLICK_OFF_RESULT);
        builder.setResultMsg(CmdUtil.resultMessage(ResultCode.SUCCESS, "click off"));
        return CmdUtil.packageGroup(builder.build());
    }

    public static Command.PackageGroup enterRoomResult(int resultCode, String resultMsg) {
        Command.Package.Builder builder = Command.Package.newBuilder();
        builder.setCmdType(CmdType.ENTER_RESULT);
        builder.setResultMsg(CmdUtil.resultMessage(resultCode, resultMsg));
        return CmdUtil.packageGroup(builder.build());
    }

    public static Command.PackageGroup sendMsgResult(int resultCode, String resultMsg, String fromId,
                                                     Chat.ChatMessage chatMessage) {
        Command.Package.Builder builder = Command.Package.newBuilder();
        builder.setCmdType(CmdType.SEND_MESSAGE_RESULT);
        builder.setResultMsg(CmdUtil.resultMessage(resultCode, resultMsg));
        Chat.ChatMessageResult.Builder msgBuilder = Chat.ChatMessageResult.newBuilder();
        if (chatMessage != null) {
            msgBuilder.setChatMessage(chatMessage);
            ProtocolStringList toList = chatMessage.getToList();
            if (!CollectionUtils.isEmpty(toList)) {
                msgBuilder.addAllTo(toList);
            }
        }

        if (!Strings.isNullOrEmpty(fromId)) {
            msgBuilder.setFrom(fromId);
        }
        builder.setContent(msgBuilder.build().toByteString());
        return CmdUtil.packageGroup(builder.build());
    }
}
