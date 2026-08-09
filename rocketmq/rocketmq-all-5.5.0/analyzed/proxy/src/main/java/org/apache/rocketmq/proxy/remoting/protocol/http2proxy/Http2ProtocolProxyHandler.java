/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.rocketmq.proxy.remoting.protocol.http2proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.haproxy.HAProxyMessageEncoder;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import javax.net.ssl.SSLException;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.proxy.config.ProxyConfig;
import org.apache.rocketmq.proxy.remoting.protocol.ProtocolHandler;
import org.apache.rocketmq.remoting.common.TlsMode;
import org.apache.rocketmq.remoting.netty.TlsSystemConfig;

/**
 * HTTP/2 本地代理协议处理器：将 Remoting 端口流量转发至本机 gRPC 服务。
 */
public class Http2ProtocolProxyHandler implements ProtocolHandler {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_REMOTING_NAME);
    private static final String LOCAL_HOST = "127.0.0.1";
    /**
     * HTTP/2 连接前导 "PRI " 的整型值；当前以 4 字节识别协议。
     * <p>
     * 完整前导为 "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n"
     * <p>
     * 参考: https://datatracker.ietf.org/doc/html/rfc7540#section-3.5
     */
    private static final int PRI_INT = 0x50524920;

    /** 出站连接使用的 TLS 上下文，禁用 TLS 时为 null。 */
    private final SslContext sslContext;

    /** 按 {@link TlsSystemConfig} 构建支持 ALPN HTTP/2 的客户端 SslContext。 */
    public Http2ProtocolProxyHandler() {
        try {
            TlsMode tlsMode = TlsSystemConfig.tlsMode;
            if (TlsMode.DISABLED.equals(tlsMode)) {
                sslContext = null;
            } else {
                sslContext = SslContextBuilder
                    .forClient()
                    .sslProvider(SslProvider.OPENSSL)
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2))
                    .build();
            }
        } catch (SSLException e) {
            log.error("Failed to create SslContext for Http2ProtocolProxyHandler", e);
            throw new RuntimeException("Failed to create SslContext for Http2ProtocolProxyHandler", e);
        }
    }

    @Override
    /** 启用本地 gRPC 代理且前 4 字节为 "PRI " 时匹配 HTTP/2。 */
    public boolean match(ByteBuf in) {
        if (!ConfigurationManager.getProxyConfig().isEnableRemotingLocalProxyGrpc()) {
            return false;
        }

        // 判断是否为 HTTP/2 连接前导 "PRI "
        return in.getInt(in.readerIndex()) == PRI_INT;
    }

    @Override
    /** 建立至本机 gRPC 端口的出站连接并装配前后端代理处理器。 */
    public void config(final ChannelHandlerContext ctx, final ByteBuf msg) {
        // 将入站通道代理至 HTTP/2 gRPC 服务端
        final Channel inboundChannel = ctx.channel();

        ProxyConfig config = ConfigurationManager.getProxyConfig();
        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
            .channel(ctx.channel().getClass())
            .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(null, Http2ProxyBackendHandler.HANDLER_NAME,
                            new Http2ProxyBackendHandler(inboundChannel));
                }
            })
            .option(ChannelOption.AUTO_READ, false)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getLocalProxyConnectTimeoutMs());
        ChannelFuture f;
        try {
            f = b.connect(LOCAL_HOST, config.getGrpcServerPort()).sync();
        } catch (Exception e) {
            log.error("connect http2 server failed. port:{}", config.getGrpcServerPort(), e);
            inboundChannel.close();
            return;
        }

        final Channel outboundChannel = f.channel();
        configPipeline(inboundChannel, outboundChannel);

        SslHandler sslHandler = null;
        if (sslContext != null) {
            sslHandler = sslContext.newHandler(outboundChannel.alloc(), LOCAL_HOST, config.getGrpcServerPort());
        }
        ctx.pipeline().addLast(new Http2ProxyFrontendHandler(outboundChannel, sslHandler));
    }

    /** 在入站侧注入 HAProxy 转发器，出站侧添加 HAProxy 编码器。 */
    protected void configPipeline(Channel inboundChannel, Channel outboundChannel) {
        inboundChannel.pipeline().addLast(new HAProxyMessageForwarder(outboundChannel));
        outboundChannel.pipeline().addFirst(HAProxyMessageEncoder.INSTANCE);
    }
}
