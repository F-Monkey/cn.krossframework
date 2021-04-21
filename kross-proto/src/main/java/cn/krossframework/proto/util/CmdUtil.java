package cn.krossframework.proto.util;

import cn.krossframework.proto.CmdType;
import cn.krossframework.proto.ResultCode;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;

public class CmdUtil {
    private CmdUtil() {
    }


    public static Command.PackageGroup clickOff() {
        Command.Package.Builder builder = Command.Package.newBuilder();
        builder.setCmdType(CmdType.CLICK_OFF);
        builder.setResultMsg(CmdUtil.resultMessage(ResultCode.SUCCESS, "click off"));
        return CmdUtil.packageGroup(builder.build());
    }

    public static Command.ResultMessage resultMessage(int resultCode, String message) {
        Command.ResultMessage.Builder builder = Command.ResultMessage.newBuilder();
        builder.setCode(resultCode);
        if (!Strings.isNullOrEmpty(message)) {
            builder.setMsg(message);
        }
        return builder.build();
    }

    public static Command.Package pkg(int resultCode, String message, int cmdType, ByteString content) {
        Command.Package.Builder builder = Command.Package.newBuilder();
        builder.setResultMsg(resultMessage(resultCode, message));
        builder.setCmdType(cmdType);
        if (content != null) {
            builder.setContent(content);
        }
        return builder.build();
    }

    public static Command.PackageGroup packageGroup(Command.Package... packages) {
        Command.PackageGroup.Builder builder = Command.PackageGroup.newBuilder();
        for (Command.Package pkg : packages) {
            builder.addPackages(pkg);
        }
        return builder.build();
    }
}
