package cn.krossframework.chat.service;

import cn.krossframework.chat.factory.UserFactory;
import cn.krossframework.commons.model.Result;
import cn.krossframework.websocket.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserFactory userFactory;

    public UserService(UserFactory factory) {
        this.userFactory = factory;
    }

    @Override
    public Result<User> loginByUsername(String username) {
        try {
            return Result.ok(this.userFactory.findOrCreate(username));
        } catch (Exception e) {
            log.error("user find or create error:\n", e);
            return Result.error(e);
        }
    }

    @Override
    public Result<User> loginByUid(String uid) {
        try {
            User user = this.userFactory.find(uid);
            if (user == null) {
                return Result.fail("invalid uid:" + uid);
            }
            return Result.ok(user);
        } catch (Exception e) {
            log.error("user find error:\n", e);
            return Result.error(e);
        }
    }
}
