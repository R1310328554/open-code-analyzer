package com.taobao.arthas.grpcweb.grpc.server.httpServer;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.lang.invoke.MethodHandles;

/**
 * 基于 Netty 的简易 HTTP 静态文件服务器，用于托管 gRPC-Web 前端资源。
 * <p>
 * {@link #start()} 会阻塞直到服务器关闭；boss/work 线程组在 finally 中优雅退出。
 */
public class NettyHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    /** 监听端口 */
    private int port;

    /** 静态资源根目录 */
    private final String STATIC_LOCATION;

    /**
     * @param port           HTTP 监听端口
     * @param staticLocation 静态文件目录绝对路径
     */
    public NettyHttpServer(int port, String staticLocation) {
        this.port = port;
        this.STATIC_LOCATION = staticLocation;
    }

    /**
     * 启动 NIO HTTP 服务器并阻塞等待 channel 关闭。
     *
     * @throws InterruptedException 绑定或 closeFuture 等待被中断
     */
    public void start() throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup work = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyHttpInitializer(this.STATIC_LOCATION))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            logger.info("start http server on port: {}", port);
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            work.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
