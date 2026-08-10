/*
 * Copyright 2017 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.util.List;

/**
 * {@link OptionalSslHandler} is a utility decoder to support both SSL and non-SSL handlers
 * based on the first message received.
 *
 * <p>根据首包是否为 TLS 记录头，动态将 pipeline 中的本 handler 替换为 {@link SslHandler} 或
 * 明文 {@link ChannelHandler}，实现同一端口同时接受 SSL 与明文流量。</p>
 */
public class OptionalSslHandler extends ByteToMessageDecoder {

    /** 检测到 TLS 时使用的 {@link SslContext}。 */
    private final SslContext sslContext;

    /** @param sslContext 非 null，用于构造 {@link SslHandler} */
    public OptionalSslHandler(SslContext sslContext) {
        this.sslContext = ObjectUtil.checkNotNull(sslContext, "sslContext");
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws Exception {
        // 至少需要 5 字节 TLS 记录头才能判断
        if (in.readableBytes() < SslUtils.SSL_RECORD_HEADER_LENGTH) {
            return;
        }
        if (SslHandler.isEncrypted(in, false)) {
            handleSsl(context);
        } else {
            handleNonSsl(context);
        }
    }

    /** 首包为 TLS：创建 {@link SslHandler} 并替换自身。 */
    private void handleSsl(ChannelHandlerContext context) {
        SslHandler sslHandler = null;
        try {
            sslHandler = newSslHandler(context, sslContext);
            context.pipeline().replace(this, newSslHandlerName(), sslHandler);
            sslHandler = null;
        } finally {
            // 若 replace 失败，SslHandler 未入 pipeline，须手动释放 SSLEngine
            if (sslHandler != null) {
                ReferenceCountUtil.safeRelease(sslHandler.engine());
            }
        }
    }

    /** 首包非 TLS：替换为明文 handler 或直接移除自身。 */
    private void handleNonSsl(ChannelHandlerContext context) {
        ChannelHandler handler = newNonSslHandler(context);
        if (handler != null) {
            context.pipeline().replace(this, newNonSslHandlerName(), handler);
        } else {
            context.pipeline().remove(this);
        }
    }

    /**
     * Optionally specify the SSL handler name, this method may return {@code null}.
     * @return the name of the SSL handler.
     *
     * <p>子类可指定插入 pipeline 的 {@link SslHandler} 名称；默认 {@code null} 由 Netty 自动命名。</p>
     */
    protected String newSslHandlerName() {
        return null;
    }

    /**
     * Override to configure the SslHandler eg. {@link SSLParameters#setEndpointIdentificationAlgorithm(String)}.
     * The hostname and port is not known by this method so servers may want to override this method and use the
     * {@link SslContext#newHandler(ByteBufAllocator, String, int)} variant.
     *
     * @param context the {@link ChannelHandlerContext} to use.
     * @param sslContext the {@link SSLContext} to use.
     * @return the {@link SslHandler} which will replace the {@link OptionalSslHandler} in the pipeline if the
     * traffic is SSL.
     *
     * <p>子类可在此配置 {@link SSLParameters} 等；服务端若需 SNI 主机名应改用
     * {@link SslContext#newHandler(ByteBufAllocator, String, int)} 重载。</p>
     */
    protected SslHandler newSslHandler(ChannelHandlerContext context, SslContext sslContext) {
        return sslContext.newHandler(context.alloc());
    }

    /**
     * Optionally specify the non-SSL handler name, this method may return {@code null}.
     * @return the name of the non-SSL handler.
     *
     * <p>明文分支插入 pipeline 的 handler 名称；可为 {@code null}。</p>
     */
    protected String newNonSslHandlerName() {
        return null;
    }

    /**
     * Override to configure the ChannelHandler.
     * @param context the {@link ChannelHandlerContext} to use.
     * @return the {@link ChannelHandler} which will replace the {@link OptionalSslHandler} in the pipeline
     * or {@code null} to simply remove the {@link OptionalSslHandler} if the traffic is non-SSL.
     *
     * <p>返回替换本 handler 的明文处理器；{@code null} 表示仅移除自身。</p>
     */
    protected ChannelHandler newNonSslHandler(ChannelHandlerContext context) {
        return null;
    }
}
