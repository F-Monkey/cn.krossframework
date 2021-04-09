package cn.krossframework.chat.service;

import cn.krossframework.websocket.User;

public interface IUserService {
    User findOrCreateUser(String username);
}
