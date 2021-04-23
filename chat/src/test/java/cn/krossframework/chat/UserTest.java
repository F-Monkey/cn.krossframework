package cn.krossframework.chat;

import cn.krossframework.chat.db.IDGenerator;
import cn.krossframework.chat.factory.IUserFactory;
import cn.krossframework.websocket.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ChatApplication.class)
@RunWith(SpringRunner.class)
public class UserTest {

    @Autowired
    IDGenerator idGenerator;

    @Test
    public void testIdGenerate() {
        String generate = idGenerator.generate();
        System.out.println(generate);
    }

    @Autowired
    IUserFactory userFactory;

    @Test
    public void testUserFactory() {
        User tom = userFactory.findOrCreate("tom");
        Assert.assertNotNull(tom);

        User user = userFactory.find(tom.getUid());
        Assert.assertNotNull(user);
    }

}
