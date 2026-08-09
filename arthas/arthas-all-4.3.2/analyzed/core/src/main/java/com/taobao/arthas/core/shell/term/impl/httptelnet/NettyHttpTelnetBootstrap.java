package com.taobao.arthas.core.shell.term.impl.httptelnet;

import com.taobao.arthas.core.shell.term.impl.http.session.HttpSessionManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.termd.core.function.Consumer;
import io.termd.core.function.Supplier;
import io.termd.core.telnet.TelnetBootstrap;
import io.termd.core.telnet.TelnetHandler;
import io.termd.core.tty.TtyConnection;

/**
 * 基于 Netty 的 HTTP/Telnet 复用端口引导类，扩展 termd {@link TelnetBootstrap}。
 * <p>
 * 每个新连接首包由 {@link ProtocolDetectHandler} 分流至 HTTP 或 Telnet pipeline。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author hengyunabc 2019-11-05
 */
public class NettyHttpTelnetBootstrap extends TelnetBootstrap {

    private EventLoopGroup group;
    /** 活跃连接集合，stop 时统一关闭 */
    private ChannelGroup channelGroup;
    private EventExecutorGroup workerGroup;
    private HttpSessionManager httpSessionManager;

    public NettyHttpTelnetBootstrap(EventExecutorGroup workerGroup, HttpSessionManager httpSessionManager) {
        this.workerGroup = workerGroup;
        this.group = new NioEventLoopGroup(new DefaultThreadFactory("arthas-NettyHttpTelnetBootstrap", true));
        this.channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
        this.httpSessionManager = httpSessionManager;
    }

    public NettyHttpTelnetBootstrap setHost(String host) {
        return (NettyHttpTelnetBootstrap) super.setHost(host);
    }

    public NettyHttpTelnetBootstrap setPort(int port) {
        return (NettyHttpTelnetBootstrap) super.setPort(port);
    }

    @Override
    public void start(Supplier<TelnetHandler> factory, Consumer<Throwable> doneHandler) {
        // ignore, never invoke

    }

    /**
     * 绑定端口并安装协议探测 handler；成功/失败通过 doneHandler 回调。
     *
     * @param handlerFactory Telnet 侧 TelnetHandler 工厂
     * @param factory        HTTP WebSocket 侧 TtyConnection 消费者
     * @param doneHandler    启动完成回调
     */
    public void start(final Supplier<TelnetHandler> handlerFactory, final Consumer<TtyConnection> factory,
                    final Consumer<Throwable> doneHandler) {
        ServerBootstrap boostrap = new ServerBootstrap();
        boostrap.group(group).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new ProtocolDetectHandler(channelGroup, handlerFactory, factory, workerGroup, httpSessionManager));
                            }
                        });

        boostrap.bind(getHost(), getPort()).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    doneHandler.accept(null);
                } else {
                    doneHandler.accept(future.cause());
                }
            }
        });
    }

    @Override
    public void stop(final Consumer<Throwable> doneHandler) {
        GenericFutureListener<Future<Object>> adapter = new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                try {
                    doneHandler.accept(future.cause());
                } finally {
                    group.shutdownGracefully();
                }
            }
        };
        channelGroup.close().addListener(adapter);
    }

}
