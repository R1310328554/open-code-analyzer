package com.alibaba.arthas.tunnel.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.arthas.tunnel.common.MethodConstants;
import com.alibaba.arthas.tunnel.common.URIConstans;
import com.taobao.arthas.common.ArthasConstants;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * Arthas Agent 侧隧道客户端：通过 WebSocket 连接 tunnel server 并完成 agent 注册。
 * 连接断开后会自动按 {@link #reconnectDelay} 重连，重连时复用已有 agent id。
 *
 * @author hengyunabc 2019-08-28
 *
 */
public class TunnelClient {
    private final static Logger logger = LoggerFactory.getLogger(TunnelClient.class);

    /** tunnel server 的 WebSocket 地址，例如 ws://127.0.0.1:7777/ws */
    private String tunnelServerUrl;

    /** 断线后重连前的等待秒数 */
    private int reconnectDelay = 5;

    // 连接 tunnel server 的事件循环组；2 个线程用于断线重连，见 issue #1284
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2, new DefaultThreadFactory("arthas-TunnelClient", true));

    /** 应用名，可选；用于生成带前缀的 agent id */
    private String appName;
    /** agent id，由 tunnel server 分配；重连时复用已有 id */
    volatile private String id;

    /** Arthas 版本号，注册时上报给 tunnel server */
    private String version = "unknown";

    /** 当前 WebSocket 是否已成功注册并保持连接 */
    private volatile boolean connected = false;

        /** 启动客户端并首次连接 tunnel server。 */
    public ChannelFuture start() throws IOException, InterruptedException, URISyntaxException {
        return connect(false);
    }

        /**
     * 建立 WebSocket 连接并发起 agent 注册。
     *
     * @param reconnect 是否为断线重连
     */
    public ChannelFuture connect(boolean reconnect) throws SSLException, URISyntaxException, InterruptedException {
        QueryStringEncoder queryEncoder = new QueryStringEncoder(this.tunnelServerUrl);
        queryEncoder.addParam(URIConstans.METHOD, MethodConstants.AGENT_REGISTER);
        queryEncoder.addParam(URIConstans.ARTHAS_VERSION, this.version);
        if (appName != null) {
            queryEncoder.addParam(URIConstans.APP_NAME, appName);
        }
        if (id != null) {
            queryEncoder.addParam(URIConstans.ID, id);
        }
        // 示例：ws://127.0.0.1:7777/ws?method=agentRegister
        final URI agentRegisterURI = queryEncoder.toUri();

        logger.info("Try to register arthas agent, uri: {}", agentRegisterURI);

        String scheme = agentRegisterURI.getScheme() == null ? "ws" : agentRegisterURI.getScheme();
        final String host = agentRegisterURI.getHost() == null ? "127.0.0.1" : agentRegisterURI.getHost();
        final int port;
        if (agentRegisterURI.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = agentRegisterURI.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Only WS(S) is supported. tunnelServerUrl: " + tunnelServerUrl);
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        WebSocketClientProtocolConfig clientProtocolConfig = WebSocketClientProtocolConfig.newBuilder()
                .webSocketUri(agentRegisterURI)
                .maxFramePayloadLength(ArthasConstants.MAX_HTTP_CONTENT_LENGTH).build();

        final WebSocketClientProtocolHandler websocketClientHandler = new WebSocketClientProtocolHandler(
                clientProtocolConfig);
        final TunnelClientSocketClientHandler handler = new TunnelClientSocketClientHandler(TunnelClient.this);

        Bootstrap bs = new Bootstrap();

        bs.group(eventLoopGroup)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .option(ChannelOption.TCP_NODELAY, true)
        .channel(NioSocketChannel.class).remoteAddress(host, port)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }

                        p.addLast(new HttpClientCodec(), new HttpObjectAggregator(ArthasConstants.MAX_HTTP_CONTENT_LENGTH), websocketClientHandler,
                                new IdleStateHandler(0, 0, ArthasConstants.WEBSOCKET_IDLE_SECONDS),
                                handler);
                    }
                });

        ChannelFuture connectFuture = bs.connect();
        if (reconnect) {
            connectFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.cause() != null) {
                        logger.error("connect to tunnel server error, uri: {}", tunnelServerUrl, future.cause());
                    }
                }
            });
        }
        connectFuture.sync();

        return handler.registerFuture();
    }

        /** 关闭事件循环组，释放 Netty 资源 */
    public void stop() {
        eventLoopGroup.shutdownGracefully();
    }

    public String getTunnelServerUrl() {
        return tunnelServerUrl;
    }

    public void setTunnelServerUrl(String tunnelServerUrl) {
        this.tunnelServerUrl = tunnelServerUrl;
    }

    public int getReconnectDelay() {
        return reconnectDelay;
    }

    public void setReconnectDelay(int reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
