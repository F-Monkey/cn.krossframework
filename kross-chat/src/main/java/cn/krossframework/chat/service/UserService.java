package cn.krossframework.chat.service;

import cn.krossframework.chat.db.IDGenerator;
import cn.krossframework.chat.model.ChatUser;
import cn.krossframework.commons.model.Result;
import cn.krossframework.websocket.User;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final MongoTemplate mongoTemplate;

    private final IDGenerator idGenerator;

    public UserService(MongoTemplate mongoTemplate,
                       @Qualifier("redisIdGenerator") IDGenerator idGenerator) {
        Preconditions.checkNotNull(mongoTemplate);
        Preconditions.checkNotNull(idGenerator);
        this.mongoTemplate = mongoTemplate;
        this.idGenerator = idGenerator;
    }

    @Override
    public Result<User> loginByUsername(String username) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        User user;
        try {
            user = this.mongoTemplate.findOne(query, ChatUser.class);
        } catch (Exception e) {
            log.error("user query error:\n", e);
            return Result.error(e);
        }
        if (user != null) {
            return Result.ok(user);
        }
        String uid;
        try {
            uid = this.idGenerator.generate();
        } catch (Exception e) {
            log.error("uid generate error:\n", e);
            return Result.error(e);
        }
        ChatUser chatUser = new ChatUser();
        chatUser.setUid(uid);
        chatUser.setUsername(username);
        Date date = new Date();
        chatUser.setCreateTime(date);
        chatUser.setUpdateTime(date);
        try {
            return Result.ok(this.mongoTemplate.save(chatUser));
        } catch (Exception e) {
            log.error("user save error:\n", e);
            return Result.error(e);
        }
    }

    @Override
    public Result<User> loginByUid(String uid) {
        Query query = new Query();
        query.addCriteria(Criteria.where("uid").is(uid));
        try {
            ChatUser user = this.mongoTemplate.findOne(query, ChatUser.class);
            return user == null ? Result.fail("invalid uid:" + uid) : Result.ok(user);
        } catch (Exception e) {
            log.error("user find error:\n", e);
            return Result.error(e);
        }
    }
}
