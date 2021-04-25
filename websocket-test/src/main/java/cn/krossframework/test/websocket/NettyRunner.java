package cn.krossframework.test.websocket;

import cn.krossframework.websocket.NettyServer;
import cn.krossframework.websocket.ProtoWebSocketHandler;
import cn.krossframework.websocket.TextWebSocketHandler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class NettyRunner implements ApplicationRunner {


    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.start();
    }

    private void start() throws InterruptedException {
        //ProtoWebSocketHandler protoWebSocketHandler = new ProtoWebSocketHandler(new ArrayList<>(), new TestDispatcher());
        TextWebSocketHandler textWebSocketHandler = new TextWebSocketHandler();
        NettyServer nettyServer = new NettyServer(8888, textWebSocketHandler);
        nettyServer.start();
    }
}
