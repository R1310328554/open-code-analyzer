package com.taobao.arthas.grpc.server;

import com.alibaba.arthas.deps.ch.qos.logback.classic.Level;
import com.alibaba.arthas.deps.ch.qos.logback.classic.LoggerContext;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.grpc.server.handler.GrpcDispatcher;
import com.taobao.arthas.grpc.server.handler.Http2Handler;
import com.taobao.arthas.grpc.server.handler.executor.GrpcExecutorFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.lang.invoke.MethodHandles;

/**
 * 基于 Netty HTTP/2 的轻量 gRPC 服务端。
 * <p>
 * 扫描 {@link com.taobao.arthas.grpc.server.handler.GrpcDispatcher} 注册的
 * {@code @GrpcService} 实现，通过 {@link Http2Handler} 处理帧级请求。
 *
 * @author: FengYe
 * @date: 2024/7/3 上午12:30
 * @description: ArthasGrpcServer
 */
public class ArthasGrpcServer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    /** 监听端口，默认 9091。 */
    private int port = 9091;

    /** 待扫描的 gRPC 服务实现包名，null 时使用默认包。 */
    private String grpcServicePackageName;

    /**
     * @param port 监听端口
     * @param grpcServicePackageName 服务实现扫描包，可为 null
     */
    public ArthasGrpcServer(int port, String grpcServicePackageName) {
        this.port = port;
        this.grpcServicePackageName = grpcServicePackageName;
    }

    /**
     * 启动 Netty ServerBootstrap：boss/worker 线程组 + HTTP/2 编解码 + {@link Http2Handler}。
     * 阻塞直至 channel 关闭后优雅释放线程组。
     */
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(10);

        GrpcDispatcher grpcDispatcher = new GrpcDispatcher();
        grpcDispatcher.loadGrpcService(grpcServicePackageName);
        GrpcExecutorFactory grpcExecutorFactory = new GrpcExecutorFactory();
        grpcExecutorFactory.loadExecutor(grpcDispatcher);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(Http2FrameCodecBuilder.forServer().build());
                            ch.pipeline().addLast(new Http2Handler(grpcDispatcher, grpcExecutorFactory));
                        }
                    });
            Channel channel = b.bind(port).sync().channel();
            logger.info("ArthasGrpcServer start successfully on port: {}", port);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("ArthasGrpcServer start error", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
