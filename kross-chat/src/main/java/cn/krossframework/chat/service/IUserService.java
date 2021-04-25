package cn.krossframework.chat.service;

import cn.krossframework.commons.model.Result;
import cn.krossframework.websocket.User;

public interface IUserService {
    Result<User> loginByUsername(String username);

    Result<User> loginByUid(String uid);
}
