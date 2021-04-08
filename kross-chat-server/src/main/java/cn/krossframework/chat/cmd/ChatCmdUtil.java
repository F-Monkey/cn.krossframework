package cn.krossframework.chat.cmd;

import cn.krossframework.proto.CmdType;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.ResultCode;
import com.google.protobuf.ByteString;

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

    public static Command.PackageGroup sendMsgResult(int resultCode, String resultMsg, ByteString content) {
        Command.Package.Builder builder = Command.Package.newBuilder();
        builder.setCmdType(CmdType.SEND_MESSAGE_RESULT);
        builder.setResultMsg(CmdUtil.resultMessage(resultCode, resultMsg));
        if(content != null){
            builder.setContent(content);
        }
        return CmdUtil.packageGroup(builder.build());
    }
}
