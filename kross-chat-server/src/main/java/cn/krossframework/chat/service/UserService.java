package cn.krossframework.chat.service;

import cn.krossframework.chat.server.ChatUser;
import cn.krossframework.websocket.User;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private final MongoTemplate mongoTemplate;

    public UserService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public User findOrCreateUser(String username) {
        // TODO  this is not safe code
        ChatUser chatUser = this.mongoTemplate.findOne(new Query(Criteria.where("username").is(username)), ChatUser.class);
        if (chatUser == null) {
            chatUser = new ChatUser();
            chatUser.setUsername(username);
            this.mongoTemplate.save(chatUser);
        }
        return chatUser;
    }
}
