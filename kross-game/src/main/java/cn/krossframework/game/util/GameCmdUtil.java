package cn.krossframework.game.util;

import cn.krossframework.proto.CmdType;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.Game;
import cn.krossframework.proto.util.CmdUtil;

public class GameCmdUtil {
    private GameCmdUtil() {
    }

    public static Command.PackageGroup enterResult(int resultCode,
                                                   String msg) {
        Game.EnterResult.Builder builder = Game.EnterResult.newBuilder();
        Command.Package pkg = CmdUtil.pkg(resultCode, msg, CmdType.ENTER, builder.build().toByteString());
        return CmdUtil.packageGroup(pkg);
    }
}
