package cn.krossframework.game.util;

import cn.krossframework.game.state.GameData;
import cn.krossframework.game.state.GameRoom;
import cn.krossframework.game.state.Seat;
import cn.krossframework.proto.CmdType;
import cn.krossframework.proto.Command;
import cn.krossframework.commons.model.ResultCode;
import cn.krossframework.proto.game.Game;
import cn.krossframework.proto.util.CmdUtil;
import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.websocket.Character;
import com.google.common.collect.ImmutableList;

public class GameCmdUtil {
    private GameCmdUtil() {
    }

    public static Command.PackageGroup enterResult(int resultCode,
                                                   String msg) {
        Game.EnterResult.Builder builder = Game.EnterResult.newBuilder();
        Command.Package pkg = CmdUtil.pkg(resultCode, msg, CmdType.ENTER, builder.build().toByteString());
        return CmdUtil.packageGroup(pkg);
    }

    public static Command.PackageGroup gameStart(GameRoom gameRoom) {
        StateGroupConfig config = gameRoom.getConfig();
        GameData gameData = gameRoom.getStateData();
        Game.TetrisConfig.Builder configBuilder = Game.TetrisConfig.newBuilder();
        configBuilder.setFallTime(config.updatePeriod());
        Game.TetrisRoomData.Builder roomDataBuilder = Game.TetrisRoomData.newBuilder();
        ImmutableList<Seat> seatList = gameData.getSeatList();
        Seat seat;
        for (int i = 0; i < seatList.size(); i++) {
            seat = seatList.get(i);
            Character character = seat.getCharacter();
            Game.TetrisSeatData.Builder builder = Game.TetrisSeatData.newBuilder();
            builder.setPlayer(Game.Player.newBuilder().setNickname(character.getNickName())
                    .setHeadIcon(character.getHeadIcon())
                    .setUid(character.getId()));
            roomDataBuilder.addTetrisSeatDataList(builder);
        }

        Game.StartGame.Builder startGameBuilder = Game.StartGame.newBuilder();
        startGameBuilder.setTetrisConfig(configBuilder);
        startGameBuilder.setTetrisRoomData(roomDataBuilder);
        return CmdUtil.packageGroup(CmdUtil.pkg(ResultCode.SUCCESS, "", CmdType.START_GAME, startGameBuilder.build().toByteString()));
    }
}
