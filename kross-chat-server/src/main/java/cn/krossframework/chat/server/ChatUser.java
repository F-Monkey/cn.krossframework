package cn.krossframework.chat.server;

import cn.krossframework.websocket.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document("user")
public class ChatUser implements User, Serializable {

    private ObjectId objectId;

    private String username;

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    @Override
    public String getUid() {
        return this.objectId.toHexString();
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }
}
