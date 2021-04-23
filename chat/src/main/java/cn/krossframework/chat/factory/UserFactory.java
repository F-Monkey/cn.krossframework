package cn.krossframework.chat.factory;

import cn.krossframework.chat.db.IDGenerator;
import cn.krossframework.chat.model.ChatUser;
import cn.krossframework.websocket.User;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class UserFactory implements IUserFactory {

    private final MongoTemplate mongoTemplate;

    private final IDGenerator idGenerator;

    public UserFactory(MongoTemplate mongoTemplate,
                       @Qualifier("redisIdGenerator") IDGenerator idGenerator) {
        Preconditions.checkNotNull(mongoTemplate);
        Preconditions.checkNotNull(idGenerator);
        this.mongoTemplate = mongoTemplate;
        this.idGenerator = idGenerator;
    }

    @Override
    public User findOrCreate(String username) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));

        ChatUser user = this.mongoTemplate.findOne(query, ChatUser.class);
        if (user != null) {
            return user;
        }
        String uid = this.idGenerator.generate();
        ChatUser chatUser = new ChatUser();
        chatUser.setUid(uid);
        chatUser.setUsername(username);
        Date date = new Date();
        chatUser.setCreateTime(date);
        chatUser.setUpdateTime(date);
        return this.mongoTemplate.save(chatUser);
    }

    @Override
    public User find(String uid) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uid").is(uid));
        return this.mongoTemplate.findOne(query, ChatUser.class);
    }
}
