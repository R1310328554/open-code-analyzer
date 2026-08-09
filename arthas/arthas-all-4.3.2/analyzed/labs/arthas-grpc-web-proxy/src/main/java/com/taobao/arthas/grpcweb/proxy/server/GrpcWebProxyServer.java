package com.taobao.arthas.grpcweb.proxy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * gRPC-Web 代理 HTTP 服务器：对外提供 gRPC-Web 协议，转发到本地 gRPC 端口。
 *
 * <p>使用 Netty {@link ServerBootstrap} 绑定 {@link #port}，子通道由
 * {@link GrpcWebProxyServerInitializer} 装配 HTTP 编解码与 {@link GrpcWebProxyHandler}。</p>
 */
public final class GrpcWebProxyServer {

    private static final Logger logger = LoggerFactory.getLogger(GrpcWebProxyServer.class);

    /** 代理 HTTP 监听端口 */
    private int port;

    /** 后端 gRPC 服务端口，传给 Handler 建立 ManagedChannel */
    private int grpcPort;

    /** 接受连接的 boss 线程组 */
    private EventLoopGroup bossGroup;

    /** 处理 I/O 的 worker 线程组 */
    private EventLoopGroup workerGroup;

    /** 已绑定的服务端 Channel */
    private Channel channel;

    /**
     * @param port 对外 HTTP/gRPC-Web 端口
     * @param grpcPort 本地 gRPC 后端端口
     */
    public GrpcWebProxyServer(int port, int grpcPort) {
        this.port = port;
        this.grpcPort = grpcPort;
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
    }

    /**
     * 启动服务器并阻塞直到 Channel 关闭。
     * 退出时在 finally 中优雅关闭 EventLoopGroup。
     */
    public void start() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new GrpcWebProxyServerInitializer(grpcPort));
            channel = serverBootstrap.bind(port).sync().channel();

            logger.info("grpc web proxy server started, listening on " + port);
            System.out.println("grpc web proxy server started, listening on " + port);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.info("fail to start grpc web proxy server!");
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /** 主动关闭 boss/worker 线程组（不等待 closeFuture）。 */
    public void close() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if(workerGroup != null){
            workerGroup.shutdownGracefully();
        }
        logger.info("success to close grpc web proxy server!");
    }

    /** 返回实际绑定端口（若 bind 时 port 为 0 则与构造参数可能不同）。 */
    public int actualPort() {
        int boundPort = ((InetSocketAddress) channel.localAddress()).getPort();
        return boundPort;
    }
}
