package cn.krossframework.chat.factory;

import cn.krossframework.websocket.User;

public interface IUserFactory {
    User findOrCreate(String username);

    User find(String uid);
}
