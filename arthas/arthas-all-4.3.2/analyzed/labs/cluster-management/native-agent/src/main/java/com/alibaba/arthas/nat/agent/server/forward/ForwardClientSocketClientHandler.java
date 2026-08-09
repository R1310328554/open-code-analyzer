package com.alibaba.arthas.nat.agent.server.forward;


import com.alibaba.arthas.nat.agent.common.constants.NativeAgentConstants;
import com.taobao.arthas.common.ArthasConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * WebSocket 客户端侧处理器：外部 WS 握手完成后，连接本地 Arthas Server 并建立双向转发。
 *
 * @description: Forward the ws request to arthas server
 * @author：flzjkl
 * @date: 2024-09-07 8:34
 */
public class ForwardClientSocketClientHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(ForwardClientSocketClientHandler.class);

    /** 与本地 Arthas Server 完成 WS 握手的 Future */
    private ChannelPromise handshakeFuture;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("WebSocket Client disconnected!");
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) {
        if (evt.equals(WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE)) {
            try {
                connectLocalServer(ctx);
            } catch (Throwable e) {
                logger.error("ForwardClientSocketClientHandler connect local arthas server error", e);
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    /**
     * 连接本机 Arthas Server，握手成功后用 {@link RelayHandler} 替换当前 pipeline 实现透明转发。
     */
    private void connectLocalServer(final ChannelHandlerContext ctx) throws InterruptedException, URISyntaxException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        // 配置出站 WebSocket 客户端 Bootstrap
        Bootstrap bootstrap = new Bootstrap();
        LocalFrameHandler localFrameHandler = new LocalFrameHandler();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(ArthasConstants.MAX_HTTP_CONTENT_LENGTH));
                        pipeline.addLast(new WebSocketClientProtocolHandler(
                                WebSocketClientHandshakerFactory.newHandshaker(
                                        new URI("ws://127.0.0.1:" + NativeAgentConstants.ARTHAS_SERVER_HTTP_PORT + "/ws"),
                                        WebSocketVersion.V13, null, false, null
                                )
                        ));
                        pipeline.addLast(localFrameHandler);
                    }
                });

        // 连接到 attach 后启动的本地 Arthas Server
        Channel arthasChannel = bootstrap.connect("127.0.0.1", NativeAgentConstants.ARTHAS_SERVER_HTTP_PORT).sync().channel();

        this.handshakeFuture = localFrameHandler.handshakeFuture();

        handshakeFuture.addListener(new GenericFutureListener<ChannelFuture>() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                ChannelPipeline pipeline = future.channel().pipeline();
                pipeline.remove(localFrameHandler);
                pipeline.addLast(new RelayHandler(ctx.channel()));
            }
        });

        handshakeFuture.sync();
        // 客户端侧也切换为 RelayHandler，形成双向管道
        ctx.pipeline().remove(ForwardClientSocketClientHandler.this);
        ctx.pipeline().addLast(new RelayHandler(arthasChannel));

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        handshakeFuture = null;
    }
}
