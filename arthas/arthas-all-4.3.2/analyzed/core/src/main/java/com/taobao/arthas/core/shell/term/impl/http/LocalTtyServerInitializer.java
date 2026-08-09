package com.taobao.arthas.core.shell.term.impl.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.local.LocalChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import io.termd.core.function.Consumer;
import io.termd.core.tty.TtyConnection;

import com.taobao.arthas.common.ArthasConstants;

/**
 * JVM 内 {@link LocalChannel} TTY 服务器 Pipeline 初始化器。
 * <p>
 * 用于 Agent 与 Bootstrap 同进程通信（{@link ArthasConstants#NETTY_LOCAL_ADDRESS}），
 * 无 HTTP 认证 handler，Pipeline 与 {@link TtyServerInitializer} 类似但走本地通道。
 *
 * @author hengyunabc 2020-09-02
 */
public class LocalTtyServerInitializer extends ChannelInitializer<LocalChannel> {

    /** 活跃 WebSocket channel 组，便于统一关闭 */
    private final ChannelGroup group;
    /** TTY 连接就绪回调 */
    private final Consumer<TtyConnection> handler;
    private EventExecutorGroup workerGroup;

    public LocalTtyServerInitializer(ChannelGroup group, Consumer<TtyConnection> handler,
            EventExecutorGroup workerGroup) {
        this.group = group;
        this.handler = handler;
        this.workerGroup = workerGroup;
    }

    @Override
    /** 组装 HTTP 编解码 → 聚合 → 请求路由 → WebSocket → 空闲检测 → TTY handler */
    protected void initChannel(LocalChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(ArthasConstants.MAX_HTTP_CONTENT_LENGTH));
        pipeline.addLast(workerGroup, "HttpRequestHandler", new HttpRequestHandler(ArthasConstants.DEFAULT_WEBSOCKET_PATH));
        pipeline.addLast(new WebSocketServerProtocolHandler(ArthasConstants.DEFAULT_WEBSOCKET_PATH, null, false, ArthasConstants.MAX_HTTP_CONTENT_LENGTH, false, true));
        pipeline.addLast(new IdleStateHandler(0, ArthasConstants.WEBSOCKET_IDLE_SECONDS, 0));
        pipeline.addLast(new TtyWebSocketFrameHandler(group, handler));
    }
}
