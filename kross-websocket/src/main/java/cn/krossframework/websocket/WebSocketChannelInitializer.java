package cn.krossframework.websocket;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public abstract class WebSocketChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ch.pipeline().addLast("http-codec", new HttpServerCodec())
                .addLast("chunked-write", new ChunkedWriteHandler())
                .addLast("http-aggregator", new HttpObjectAggregator(8192))
                .addLast("log-handler", new LoggingHandler(LogLevel.INFO))
                .addLast("ws-server-handler", new WebSocketServerProtocolHandler("/ws",1000));
        // TODO add your personal handler
    }
}
