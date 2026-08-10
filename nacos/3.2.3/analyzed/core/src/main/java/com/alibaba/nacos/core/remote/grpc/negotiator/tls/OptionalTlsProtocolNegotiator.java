/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.remote.grpc.negotiator.tls;

import com.alibaba.nacos.core.remote.grpc.negotiator.NacosGrpcProtocolNegotiator;
import com.alibaba.nacos.core.remote.tls.RpcServerTlsConfig;
import io.grpc.netty.shaded.io.grpc.netty.GrpcHttp2ConnectionHandler;
import io.grpc.netty.shaded.io.grpc.netty.InternalProtocolNegotiators;
import io.grpc.netty.shaded.io.grpc.netty.ProtocolNegotiationEvent;
import io.grpc.netty.shaded.io.netty.buffer.ByteBuf;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandler;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import io.grpc.netty.shaded.io.netty.handler.codec.ByteToMessageDecoder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslHandler;
import io.grpc.netty.shaded.io.netty.util.AsciiString;
import io.grpc.netty.shaded.io.netty.util.Attribute;
import io.grpc.netty.shaded.io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 可选 TLS 协议协商器：同一端口同时支持 TLS 与明文，通过首字节探测选择处理器链。
 * support the tls and plain protocol one the same port.
 *
 * @author githubcheng2978.
 */
public class OptionalTlsProtocolNegotiator implements NacosGrpcProtocolNegotiator {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger(OptionalTlsProtocolNegotiator.class);
    
    /** 探测 TLS/明文所需的最小可读字节数。 */
    private static final int MAGIC_VALUE = 5;
    
    /** 是否允许兼容模式下的明文连接。 */
    private final boolean supportPlainText;
    
    /** TLS 配置，用于热重载 SslContext。 */
    private final RpcServerTlsConfig config;
    
    /** 当前 TLS 上下文，可在 reload 时更新。 */
    private SslContext sslContext;
    
    /** 构造协商器；compatibility 配置决定是否支持明文。 */
    public OptionalTlsProtocolNegotiator(SslContext sslContext, RpcServerTlsConfig config) {
        this.sslContext = sslContext;
        this.config = config;
        this.supportPlainText = config.getCompatibility();
    }
    
    /** 包内更新 SslContext（供测试或刷新使用）。 */
    void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }
    
    /** TLS 连接使用 https scheme。 */
    @Override
    public AsciiString scheme() {
        return AsciiString.of("https");
    }
    
    /** 创建端口统一处理器，内部按探测结果挂载 TLS 或明文 handler。 */
    @Override
    public ChannelHandler newHandler(GrpcHttp2ConnectionHandler grpcHttp2ConnectionHandler) {
        ChannelHandler plaintext =
            InternalProtocolNegotiators.serverPlaintext().newHandler(grpcHttp2ConnectionHandler);
        ChannelHandler ssl = InternalProtocolNegotiators.serverTls(sslContext)
            .newHandler(grpcHttp2ConnectionHandler);
        return new PortUnificationServerHandler(ssl, plaintext);
    }
    
    @Override
    public void close() {
        
    }
    
    /** TLS 仍启用时重新构建 SslContext。 */
    @Override
    public void reloadNegotiator() {
        if (config.getEnableTls()) {
            sslContext = DefaultTlsContextBuilder.getSslContext(config);
        }
    }
    
    /** 反射获取 ProtocolNegotiationEvent.DEFAULT 供 pipeline 触发。 */
    private ProtocolNegotiationEvent getDefPne() {
        try {
            Field aDefault = ProtocolNegotiationEvent.class.getDeclaredField("DEFAULT");
            aDefault.setAccessible(true);
            return (ProtocolNegotiationEvent) aDefault.get(null);
        } catch (Exception e) {
            LOGGER.warn("Failed to access ProtocolNegotiationEvent.DEFAULT via reflection; "
                + "the negotiation event will be null, which may break gRPC TLS negotiation", e);
        }
        return null;
    }
    
    /** 端口统一解码器：根据首包判断 TLS 并切换 pipeline。 */
    public class PortUnificationServerHandler extends ByteToMessageDecoder {
        
        private final ProtocolNegotiationEvent pne;
        
        private final ChannelHandler ssl;
        
        private final ChannelHandler plaintext;
        
        public PortUnificationServerHandler(ChannelHandler ssl, ChannelHandler plaintext) {
            this.ssl = ssl;
            this.plaintext = plaintext;
            this.pne = getDefPne();
        }
        
        /** 判断缓冲区是否为 TLS 加密流量。 */
        private boolean isSsl(ByteBuf buf) {
            return SslHandler.isEncrypted(buf);
        }
        
        /** 读取足够字节后选择 SSL 或明文 handler 并移除自身。 */
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
            if (in.readableBytes() < MAGIC_VALUE) {
                return;
            }
            Attribute<Boolean> tlsProtected =
                ctx.channel().attr(AttributeKey.valueOf("TLS_PROTECTED"));
            if (isSsl(in) || !supportPlainText) {
                tlsProtected.set(true);
                ctx.pipeline().addAfter(ctx.name(), null, this.ssl);
            } else {
                tlsProtected.set(false);
                ctx.pipeline().addAfter(ctx.name(), null, this.plaintext);
            }
            ctx.fireUserEventTriggered(pne);
            ctx.pipeline().remove(this);
        }
    }
    
}
