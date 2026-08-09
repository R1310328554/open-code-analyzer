/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.NetUtil;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisClientConfig;
import org.redisson.client.RedisConnection;
import org.redisson.config.SslProvider;
import org.redisson.config.SslVerificationMode;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Arrays;

/**
 * Redis 连接 Netty {@link ChannelInitializer}，装配编解码器、队列与 SSL 等处理器。
 * <p>
 * 按 {@link Type#PLAIN} 或 {@link Type#PUBSUB} 区分普通命令连接与 Pub/Sub 专用连接。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisChannelInitializer extends ChannelInitializer<Channel> {

    /** 连接类型：Pub/Sub 订阅连接或普通命令连接。 */
    public enum Type {PUBSUB, PLAIN}

    /** 客户端配置（地址、SSL、Ping 间隔等）。 */
    private final RedisClientConfig config;
    /** 所属 {@link RedisClient} 实例。 */
    private final RedisClient redisClient;
    /** 当前初始化的连接类型。 */
    private final Type type;
    /** 断线重连看门狗。 */
    private final ConnectionWatchdog connectionWatchdog;
    /** 周期性 Ping 保活处理器，未启用时为 {@code null}。 */
    private final PingConnectionHandler pingConnectionHandler;
    
    /**
     * 构造通道初始化器并创建看门狗与可选 Ping 处理器。
     *
     * @param bootstrap Netty 客户端引导
     * @param config Redis 客户端配置
     * @param redisClient 所属 Redis 客户端
     * @param channels 全局连接组，供看门狗管理
     * @param type 连接类型（普通或 Pub/Sub）
     */
    public RedisChannelInitializer(Bootstrap bootstrap, RedisClientConfig config, RedisClient redisClient, ChannelGroup channels, Type type) {
        super();
        this.config = config;
        this.redisClient = redisClient;
        this.type = type;
        
        if (config.getPingConnectionInterval() > 0) {
            pingConnectionHandler = new PingConnectionHandler(config);
        } else {
            pingConnectionHandler = null;
        }
        connectionWatchdog = new ConnectionWatchdog(bootstrap, channels, config);
    }
    
    /** 按连接类型装配 SSL、连接处理器、编解码器、命令队列与错误日志。 */
    @Override
    protected void initChannel(Channel ch) throws Exception {
        initSsl(config, ch);
        
        if (type == Type.PLAIN) {
            ch.pipeline().addLast(new RedisConnectionHandler(redisClient));
        } else {
            ch.pipeline().addLast(new RedisPubSubConnectionHandler(redisClient));
        }

        ch.pipeline().addLast(
            connectionWatchdog,
            new CommandEncoder(config.getCommandMapper()),
            CommandBatchEncoder.INSTANCE);

        if (type == Type.PLAIN) {
            ch.pipeline().addLast(new CommandsQueue());
        } else {
            ch.pipeline().addLast(new CommandsQueuePubSub());
        }

        if (pingConnectionHandler != null) {
            ch.pipeline().addLast(pingConnectionHandler);
        }
        
        if (type == Type.PLAIN) {
            ch.pipeline().addLast(new CommandDecoder(config.getAddress().getScheme()));
        } else {
            ch.pipeline().addLast(new CommandPubSubDecoder(config));
        }

        ch.pipeline().addLast(new ErrorsLoggingHandler());

        config.getNettyHook().afterChannelInitialization(ch);
    }
    
    /** 若地址为 rediss 则配置 SSL 上下文、信任/密钥库并在握手完成后触发 {@code channelActive}。 */
    private void initSsl(RedisClientConfig config, Channel ch) throws GeneralSecurityException, IOException {
        if (!config.getAddress().isSsl()) {
            return;
        }

        io.netty.handler.ssl.SslProvider provided = io.netty.handler.ssl.SslProvider.JDK;
        if (config.getSslProvider() == SslProvider.OPENSSL) {
            provided = io.netty.handler.ssl.SslProvider.OPENSSL;
        }
        
        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                                                    .sslProvider(provided)
                                                    .keyStoreType(config.getSslKeystoreType());

        sslContextBuilder.protocols(config.getSslProtocols());
        if (config.getSslCiphers() != null) {
            sslContextBuilder.ciphers(Arrays.asList(config.getSslCiphers()));
        }

        if (config.getSslTruststore() != null) {
            KeyStore keyStore = getKeyStore(config);
            
            InputStream stream = config.getSslTruststore().openStream();
            try {
                char[] password = null;
                if (config.getSslTruststorePassword() != null) {
                    password = config.getSslTruststorePassword().toCharArray();
                }
                keyStore.load(stream, password);
            } finally {
                stream.close();
            }
            
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContextBuilder.trustManager(trustManagerFactory);
        }
        if (config.getSslTrustManagerFactory() != null) {
            sslContextBuilder.trustManager(config.getSslTrustManagerFactory());
        }

        if (config.getSslKeystore() != null){
            KeyStore keyStore = getKeyStore(config);
            
            InputStream stream = config.getSslKeystore().openStream();
            char[] password = null;
            if (config.getSslKeystorePassword() != null) {
                password = config.getSslKeystorePassword().toCharArray();
            }
            try {
                keyStore.load(stream, password);
            } finally {
                stream.close();
            }
            
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);
            sslContextBuilder.keyManager(keyManagerFactory);
        }
        if (config.getSslKeyManagerFactory() != null) {
            sslContextBuilder.keyManager(config.getSslKeyManagerFactory());
        }
        
        SSLParameters sslParams = new SSLParameters();

        if (config.getSslVerificationMode() == SslVerificationMode.STRICT) {
            sslParams.setEndpointIdentificationAlgorithm("HTTPS");
        } else if (config.getSslVerificationMode() == SslVerificationMode.CA_ONLY) {
            sslParams.setEndpointIdentificationAlgorithm("");
        } else {
            if (config.getSslTruststore() == null) {
                sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
            }
        }

        SslContext sslContext = sslContextBuilder.build();
        String hostname = config.getSslHostname();
        if (hostname == null || NetUtil.createByteArrayFromIpAddressString(hostname) != null) {
            hostname = config.getAddress().getHost();
        }

        SSLEngine sslEngine = sslContext.newEngine(ch.alloc(), hostname, config.getAddress().getPort());
        sslEngine.setSSLParameters(sslParams);
        
        SslHandler sslHandler = new SslHandler(sslEngine);
        ch.pipeline().addLast(sslHandler);
        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            
            /** SSL 握手是否已成功完成。 */
            volatile boolean sslInitDone;
            
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                if (sslInitDone) {
                    super.channelActive(ctx);
                }
            }
            
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                if (!sslInitDone && evt instanceof SslHandshakeCompletionEvent) {
                    SslHandshakeCompletionEvent e = (SslHandshakeCompletionEvent) evt;
                    if (e.isSuccess()) {
                        sslInitDone = true;
                        ctx.fireChannelActive();
                    } else {
                        RedisConnection connection = RedisConnection.getFrom(ctx.channel());
                        connection.closeAsync();
                        connection.getConnectionPromise().completeExceptionally(e.cause());
                    }
                }

                super.userEventTriggered(ctx, evt);
            }

        });
    }

    /** 按配置类型或 JVM 默认类型创建 {@link KeyStore} 实例。 */
    private KeyStore getKeyStore(RedisClientConfig config) throws KeyStoreException {
        if (config.getSslKeystoreType() != null) {
            return KeyStore.getInstance(config.getSslKeystoreType());
        }
        return KeyStore.getInstance(KeyStore.getDefaultType());
    }

}
