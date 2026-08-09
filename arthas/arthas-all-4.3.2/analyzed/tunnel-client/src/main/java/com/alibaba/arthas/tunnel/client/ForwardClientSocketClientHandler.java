package com.alibaba.arthas.tunnel.client;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.arthas.common.ArthasConstants;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Forward 模式的 WebSocket 客户端处理器：远端 Tunnel 握手完成后，
 * 再连接本地 Arthas Server（Netty LocalChannel），并通过 {@link RelayHandler} 双向转发帧。
 *
 * @author hengyunabc 2019-08-28
 */
public class ForwardClientSocketClientHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(ForwardClientSocketClientHandler.class);

    /** 本地 Arthas Server WebSocket 握手完成的 Promise */
    private ChannelPromise handshakeFuture;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("WebSocket Client disconnected!");
    }

    /**
     * 远端 WebSocket 握手完成后触发本地 Server 连接与双向 relay 搭建。
     */
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) {
        if (evt.equals(ClientHandshakeStateEvent.HANDSHAKE_COMPLETE)) {
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
     * 经 LocalChannel 连接本机 Arthas Server，握手成功后两端各挂一个 {@link RelayHandler} 做帧转发。
     */
    private void connectLocalServer(final ChannelHandlerContext ctx) throws InterruptedException, URISyntaxException {
        final EventLoopGroup group = new NioEventLoopGroup(1, new DefaultThreadFactory("arthas-forward-client-connect-local", true));
        ChannelFuture closeFuture = null;
        try {
            logger.info("ForwardClientSocketClientHandler star connect local arthas server");
            // 占位 URI，仅满足 WebSocket 协议处理器构造要求，实际走 LocalChannel
            WebSocketClientProtocolConfig clientProtocolConfig = WebSocketClientProtocolConfig.newBuilder()
                    .webSocketUri("ws://127.0.0.1:8563/ws")
                    .maxFramePayloadLength(ArthasConstants.MAX_HTTP_CONTENT_LENGTH).build();

            final WebSocketClientProtocolHandler websocketClientHandler = new WebSocketClientProtocolHandler(
                    clientProtocolConfig);

            final LocalFrameHandler localFrameHandler = new LocalFrameHandler();

            Bootstrap b = new Bootstrap();
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
            b.group(group).channel(LocalChannel.class)
                    .handler(new ChannelInitializer<LocalChannel>() {
                        @Override
                        protected void initChannel(LocalChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new HttpClientCodec(), new HttpObjectAggregator(ArthasConstants.MAX_HTTP_CONTENT_LENGTH), websocketClientHandler,
                                    localFrameHandler);
                        }
                    });

            LocalAddress localAddress = new LocalAddress(ArthasConstants.NETTY_LOCAL_ADDRESS);
            Channel localChannel = b.connect(localAddress).sync().channel();
            this.handshakeFuture = localFrameHandler.handshakeFuture();
            handshakeFuture.addListener(new GenericFutureListener<ChannelFuture>() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            ChannelPipeline pipeline = future.channel().pipeline();
                            pipeline.remove(localFrameHandler);
                            // 本地 → 远端
                            pipeline.addLast(new RelayHandler(ctx.channel()));
                        }
                    });

            handshakeFuture.sync();
            ctx.pipeline().remove(ForwardClientSocketClientHandler.this);
            // 远端 → 本地
            ctx.pipeline().addLast(new RelayHandler(localChannel));
            logger.info("ForwardClientSocketClientHandler connect local arthas server success");

            closeFuture = localChannel.closeFuture();
        } finally {
            if (closeFuture != null) {
                closeFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        group.shutdownGracefully();
                    }
                });
            } else {
                group.shutdownGracefully();
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("ForwardClientSocketClient channel: {}" , ctx.channel(), cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
