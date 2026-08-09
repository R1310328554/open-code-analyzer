package com.alibaba.arthas.nat.agent.server;

import com.alibaba.arthas.nat.agent.factory.NativeAgentRegistryFactory;
import com.alibaba.arthas.nat.agent.registry.NativeAgentRegistry;
import com.alibaba.arthas.nat.agent.core.ArthasHomeHandler;
import com.alibaba.arthas.nat.agent.server.forward.ForwardClientSocketClientHandler;
import com.alibaba.arthas.nat.agent.server.http.HttpRequestHandler;
import com.alibaba.arthas.nat.agent.common.utils.WelcomeUtil;
import com.taobao.arthas.common.UsageRender;
import com.taobao.middleware.cli.CLI;
import com.taobao.middleware.cli.CommandLine;
import com.taobao.middleware.cli.UsageMessageFormatter;
import com.taobao.middleware.cli.annotations.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.taobao.arthas.common.ArthasConstants.MAX_HTTP_CONTENT_LENGTH;

/**
 * Arthas Native Agent 启动入口：解析 CLI 参数、注册到服务发现、启动 HTTP 与 WebSocket 服务。
 *
 * @description: hello world
 * @author：flzjkl
 * @date: 2024-07-20 9:23
 */

@Name("arthas-native-agent")
@Summary("Bootstrap Arthas Native Agent")
@Description("EXAMPLES:\n" + "java -jar native-agent.jar --ip 116.196.97.114 --http-port 2671 --ws-port 2672 --registration-type etcd --registration-address 126.166.97.114:2379\n"
        + "  https://arthas.aliyun.com/doc\n")
public class NativeAgentBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(NativeAgentBootstrap.class);
    /** 默认 HTTP 管理端口 */
    private static final int DEFAULT_HTTP_PORT = 2671;
    /** 默认 WebSocket 转发端口 */
    private static final int DEFAULT_WS_PORT = 2672;
    /** 本机对外 IP，注册到注册中心 */
    public String ip;
    public Integer httpPort;
    public Integer wsPort;
    /** 注册中心类型，如 etcd、zookeeper */
    public String registrationType;
    /** 注册中心地址 */
    public String registrationAddress;

    @Option(longName = "ip", required = true)
    @Description("native agent ip")
    public void setIp(String ip) {
        this.ip = ip;
    }

    @Option(longName = "http-port")
    @Description("native agent http port, default 2671")
    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    @Option(longName = "ws-port")
    @Description("native agent ws port, default 2672")
    public void wsPort(Integer wsPort) {
        this.wsPort = wsPort;
    }

    @Option(longName = "registration-type", required = true)
    @Description("registration type")
    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    @Option(longName = "registration-address", required = true)
    @Description("registration address")
    public void setRegistrationAddress(String registrationAddress) {
        this.registrationAddress = registrationAddress;
    }

    public static void main(String[] args) {
        // 打印欢迎信息
        WelcomeUtil.printNativeAgentWelcomeMsg();

        // 检查并定位 Arthas 安装目录
        logger.info("check arthas file path...");
        ArthasHomeHandler.findArthasHome();
        logger.info("check arthas file path success");

        // 解析命令行启动参数
        logger.info("read input config...");
        NativeAgentBootstrap nativeAgentBootstrap = new NativeAgentBootstrap();
        CLI cli = CLIConfigurator.define(NativeAgentBootstrap.class);
        CommandLine commandLine = cli.parse(Arrays.asList(args));
        try {
            CLIConfigurator.inject(commandLine, nativeAgentBootstrap);
        } catch (Throwable e) {
            logger.error("Missing startup parameter");
            e.printStackTrace();
            System.out.println(usage(cli));
            System.exit(1);
        }
        logger.info("read input config success");

        // 向注册中心注册本 Agent 的 IP 与端口
        try {
            logger.info("register native agent ...");
            NativeAgentRegistryFactory nativeAgentRegistryFactory = NativeAgentRegistryFactory.getNativeAgentClientRegisterFactory();
            NativeAgentRegistry nativeAgentRegistry = nativeAgentRegistryFactory.getServiceRegistration(nativeAgentBootstrap.getRegistrationType());
            nativeAgentRegistry.registerNativeAgent(nativeAgentBootstrap.getRegistrationAddress()
                    , nativeAgentBootstrap.getIp()
                    , nativeAgentBootstrap.getHttpPortOrDefault() + ":" + nativeAgentBootstrap.getWsPortOrDefault());
            logger.info("register native agent success!");
        } catch (Exception e) {
            logger.error("register native agent failed!");
            e.printStackTrace();
            System.exit(1);
        }

        // 在独立线程中启动 WebSocket 服务，用于转发到本地 Arthas Server
        int wsPortOrDefault = nativeAgentBootstrap.getWsPortOrDefault();
        Thread wsServerThread = new Thread(() -> {
            logger.info("start the websocket server... ws port:" + wsPortOrDefault);
            try {
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .handler(new LoggingHandler(LogLevel.INFO))
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) {
                                    ChannelPipeline p = ch.pipeline();
                                    p.addLast(new HttpRequestDecoder());
                                    p.addLast(new HttpObjectAggregator(MAX_HTTP_CONTENT_LENGTH));
                                    p.addLast(new HttpResponseEncoder());
                                    p.addLast(new WebSocketServerProtocolHandler("/ws"));
                                    p.addLast(new ForwardClientSocketClientHandler());
                                }
                            });
                    ChannelFuture f = b.bind("0.0.0.0", wsPortOrDefault).sync();
                    logger.info("start the websocket server success! ws port:" + wsPortOrDefault);
                    f.channel().closeFuture().sync();
                } finally {
                    logger.info("shutdown websocket server, ws port:{}", wsPortOrDefault);
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            } catch (InterruptedException e) {
                logger.error("failed to start  websocket server, ws port: {}", wsPortOrDefault);
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
        wsServerThread.setName("native-agent-ws-server");
        wsServerThread.start();

        // 启动 HTTP 服务，提供 listProcess、attachJvm 等 REST 接口
        int httpPortOrDefault = nativeAgentBootstrap.getHttpPortOrDefault();
        logger.info("start the http server... http port:" + httpPortOrDefault);
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(MAX_HTTP_CONTENT_LENGTH));
                            ch.pipeline().addLast(new HttpRequestHandler());
                        }
                    });
            ChannelFuture f = b.bind("0.0.0.0", httpPortOrDefault).sync();
            logger.info("start the http server success, http port:" + httpPortOrDefault);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("failed to start http server, http port:" + httpPortOrDefault);
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            logger.info("shutdown http server");
        }

    }

    /** 生成 CLI 用法说明文本 */
    private static String usage(CLI cli) {
        StringBuilder usageStringBuilder = new StringBuilder();
        UsageMessageFormatter usageMessageFormatter = new UsageMessageFormatter();
        usageMessageFormatter.setOptionComparator(null);
        cli.usage(usageStringBuilder, usageMessageFormatter);
        return UsageRender.render(usageStringBuilder.toString());
    }


    /** 返回 HTTP 端口，未指定时使用默认值 */
    public int getHttpPortOrDefault() {
        if (this.httpPort == null) {
            return DEFAULT_HTTP_PORT;
        } else {
            return this.httpPort;
        }
    }

    /** 返回 WebSocket 端口，未指定时使用默认值 */
    public int getWsPortOrDefault() {
        if (this.wsPort == null) {
            return DEFAULT_WS_PORT;
        } else {
            return this.httpPort;
        }
    }

    public String getIp() {
        return ip;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public Integer getWsPort() {
        return wsPort;
    }

    public String getRegistrationAddress() {
        return registrationAddress;
    }

    public String getRegistrationType() {
        return registrationType;
    }
}
