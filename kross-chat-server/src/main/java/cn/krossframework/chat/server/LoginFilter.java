package cn.krossframework.chat.server;

import cn.krossframework.chat.service.IUserService;
import cn.krossframework.proto.Chat;
import cn.krossframework.proto.CmdType;
import cn.krossframework.proto.Command;
import cn.krossframework.commons.web.Character;
import cn.krossframework.websocket.CharacterPool;
import cn.krossframework.websocket.Filter;
import cn.krossframework.commons.web.Session;
import cn.krossframework.websocket.User;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoginFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);

    private final IUserService userService;

    private final CharacterPool characterPool;

    public LoginFilter(IUserService userService,
                       CharacterPool characterPool) {
        this.userService = userService;
        this.characterPool = characterPool;
    }

    @Override
    public boolean filter(Session session, Command.Cmd cmd) {
        if (cmd.getCmdType() != CmdType.LOGIN) {
            return true;
        }
        Chat.Login login;

        try {
            login = Chat.Login.parseFrom(cmd.getContent());
        } catch (InvalidProtocolBufferException e) {
            log.error("invalid login content, error:\n", e);
            return false;
        }

        String username = login.getUsername();
        User u = this.userService.findOrCreateUser(username);
        CharacterPool.FetchCharacter fetchCharacter = this.characterPool.findOrCreate(session, u);
        Character character = fetchCharacter.getCharacter();
        if (!fetchCharacter.isNew()) {
            character.setSession(session);
        }
        session.setAttribute(Character.KEY, character);
        return true;
    }
}
