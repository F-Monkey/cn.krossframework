package cn.krossframework.websocket;

import com.google.common.base.Preconditions;
import com.google.protobuf.MessageLite;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyWebSocketSession implements Session {

    private static final Logger log = LoggerFactory.getLogger(NettyWebSocketSession.class);

    private final ChannelHandlerContext context;

    public NettyWebSocketSession(ChannelHandlerContext ctx) {
        Preconditions.checkNotNull(ctx);
        this.context = ctx;
    }

    @Override
    public String getId() {
        return this.context.channel().id().asLongText();
    }

    @Override
    public String getRemoteAddress() {
        return this.context.channel().remoteAddress().toString();
    }

    @Override
    public <T> T getAttribute(AttributeKey<T> key) {
        if (this.context == null) {
            return null;
        }
        return this.context.channel().attr(key).get();
    }

    @Override
    public <T> void setAttribute(AttributeKey<T> key, T val) {
        this.context.channel().attr(key).set(val);
    }

    @Override
    public void send(Object data) {
        if (this.context == null) {
            return;
        }
        if (data instanceof BinaryWebSocketFrame) {
            this.context.writeAndFlush(data);
            return;
        }

        if (data instanceof MessageLite) {
            BinaryWebSocketFrame socketFrame = new BinaryWebSocketFrame(Unpooled.copiedBuffer(((MessageLite) data).toByteArray()));
            this.context.writeAndFlush(socketFrame);
            return;
        }
        log.info("invalid Object data: {}", data);
    }

    @Override
    public boolean isAlive() {
        return this.context.channel().isOpen();
    }
}
