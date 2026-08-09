package com.alibaba.arthas.nat.agent.server.forward;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 本地 WebSocket 客户端握手阶段的占位处理器，用于感知握手完成事件。
 *
 * @description: LocalFrameHandler
 * @author：flzjkl
 * @date: 2024-08-25 22:12
 */
public class LocalFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final static Logger logger = LoggerFactory.getLogger(LocalFrameHandler.class);
    /** 握手结果 Promise，供 {@link ForwardClientSocketClientHandler} 等待 */
    private ChannelPromise handshakeFuture;

    public LocalFrameHandler() {
    }

    public ChannelPromise handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof ClientHandshakeStateEvent) {
            if (evt.equals(ClientHandshakeStateEvent.HANDSHAKE_COMPLETE)) {
                handshakeFuture.setSuccess();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("LocalFrameHandler error", cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {

    }
}
