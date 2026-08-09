/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.quic;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ObjectUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * 面向 QUIC 服务端的 {@link QuicCodecBuilder}，配置并构建应加入
 * {@code QUIC} 服务端 {@link io.netty.channel.ChannelPipeline} 的 {@link ChannelHandler}。
 */
public final class QuicServerCodecBuilder extends QuicCodecBuilder<QuicServerCodecBuilder> {
    // ChannelOption 应用顺序可能影响校验，故使用 LinkedHashMap 保持顺序
    private final Map<ChannelOption<?>, Object> options = new LinkedHashMap<>();
    private final Map<AttributeKey<?>, Object> attrs = new HashMap<>();
    private final Map<ChannelOption<?>, Object> streamOptions = new LinkedHashMap<>();
    private final Map<AttributeKey<?>, Object> streamAttrs = new HashMap<>();
    private ChannelHandler handler;
    private ChannelHandler streamHandler;
    private QuicConnectionIdGenerator connectionIdAddressGenerator;
    private QuicTokenHandler tokenHandler;
    private QuicResetTokenGenerator resetTokenGenerator;

    /** 创建新的服务端编解码器构建器实例。 */
    public QuicServerCodecBuilder() {
        super(true);
    }

    private QuicServerCodecBuilder(QuicServerCodecBuilder builder) {
        super(builder);
        options.putAll(builder.options);
        attrs.putAll(builder.attrs);
        streamOptions.putAll(builder.streamOptions);
        streamAttrs.putAll(builder.streamAttrs);
        handler = builder.handler;
        streamHandler = builder.streamHandler;
        connectionIdAddressGenerator = builder.connectionIdAddressGenerator;
        tokenHandler = builder.tokenHandler;
        resetTokenGenerator = builder.resetTokenGenerator;
    }

    /** 克隆当前构建器，复制全部配置。 */
    @Override
    public QuicServerCodecBuilder clone() {
        return new QuicServerCodecBuilder(this);
    }

    /**
     * 为新建 {@link QuicChannel} 设置 {@link ChannelOption}；{@code null} 表示移除先前选项。
     *
     * @param option    the {@link ChannelOption} to apply to the {@link QuicChannel}.
     * @param value     the value of the option.
     * @param <T>       the type of the value.
     * @return          this instance.
     */
    public <T> QuicServerCodecBuilder option(ChannelOption<T> option, @Nullable T value) {
        Quic.updateOptions(options, option, value);
        return self();
    }

    /**
     * 为新建 {@link QuicChannel} 设置初始 {@link AttributeKey} 属性；{@code null} 表示移除。
     *
     * @param key       the {@link AttributeKey} to apply to the {@link QuicChannel}.
     * @param value     the value of the attribute.
     * @param <T>       the type of the value.
     * @return          this instance.
     */
    public <T> QuicServerCodecBuilder attr(AttributeKey<T> key, @Nullable T value) {
        Quic.updateAttributes(attrs, key, value);
        return self();
    }

    /**
     * 设置 {@link QuicChannel} 创建后加入其 pipeline 的 {@link ChannelHandler}。
     *
     * @param handler   the {@link ChannelHandler} that is added to the {@link QuicChannel}s
     *                  {@link io.netty.channel.ChannelPipeline}.
     * @return          this instance.
     */
    public QuicServerCodecBuilder handler(ChannelHandler handler) {
        this.handler = ObjectUtil.checkNotNull(handler, "handler");
        return self();
    }

    /**
     * 为新建 {@link QuicStreamChannel} 设置 {@link ChannelOption}。
     *
     * @param option    the {@link ChannelOption} to apply to the {@link QuicStreamChannel}s.
     * @param value     the value of the option.
     * @param <T>       the type of the value.
     * @return          this instance.
     */
    public <T> QuicServerCodecBuilder streamOption(ChannelOption<T> option, @Nullable T value) {
        Quic.updateOptions(streamOptions, option, value);
        return self();
    }

    /**
     * Allow to specify an initial attribute of the newly created {@link QuicStreamChannel}. If the {@code value} is
     * {@code null}, the attribute of the specified {@code key} is removed.
     *
     * @param key       the {@link AttributeKey} to apply to the {@link QuicStreamChannel}s.
     * @param value     the value of the attribute.
     * @param <T>       the type of the value.
     * @return          this instance.
     */
    public <T> QuicServerCodecBuilder streamAttr(AttributeKey<T> key, @Nullable T value) {
        Quic.updateAttributes(streamAttrs, key, value);
        return self();
    }

    /**
     * 设置 {@link QuicStreamChannel} 创建后加入 pipeline 的 {@link ChannelHandler}。
     *
     * @param streamHandler     the {@link ChannelHandler} that is added to the {@link QuicStreamChannel}s
     *                          {@link io.netty.channel.ChannelPipeline}.
     * @return                  this instance.
     */
    public QuicServerCodecBuilder streamHandler(ChannelHandler streamHandler) {
        this.streamHandler = ObjectUtil.checkNotNull(streamHandler, "streamHandler");
        return self();
    }

    /**
     * 设置连接 ID 生成器；未指定时使用 {@link QuicConnectionIdGenerator#signGenerator()}。
     *
     * @param connectionIdAddressGenerator  the {@link QuicConnectionIdGenerator} to use.
     * @return                              this instance.
     */
    public QuicServerCodecBuilder connectionIdAddressGenerator(
            QuicConnectionIdGenerator connectionIdAddressGenerator) {
        this.connectionIdAddressGenerator = connectionIdAddressGenerator;
        return this;
    }

    /**
     * 设置地址验证 token 的生成/校验处理器；{@code null} 时使用 {@link NoQuicTokenHandler}。
     *
     * @param tokenHandler  the {@link QuicTokenHandler} to use.
     * @return              this instance.
     */
    public QuicServerCodecBuilder tokenHandler(@Nullable QuicTokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
        return self();
    }

    /**
     * 设置无状态重置 token 生成器；{@code null} 时使用 {@link QuicResetTokenGenerator#signGenerator()}。
     *
     * @param resetTokenGenerator  the {@link QuicResetTokenGenerator} to use.
     * @return                     this instance.
     */
    public QuicServerCodecBuilder resetTokenGenerator(@Nullable QuicResetTokenGenerator resetTokenGenerator) {
        this.resetTokenGenerator = resetTokenGenerator;
        return self();
    }

    /** 校验 handler 与 streamHandler 至少配置其一。 */
    @Override
    protected void validate() {
        super.validate();
        if (handler == null && streamHandler == null) {
            throw new IllegalStateException("handler and streamHandler not set");
        }
    }

    /** 组装默认 token/CID/reset 生成器并构建 {@link QuicheQuicServerCodec}。 */
    @Override
    ChannelHandler build(QuicheConfig config,
                                   Function<QuicChannel, ? extends QuicSslEngine> sslEngineProvider,
                                   Executor sslTaskExecutor,
                                   int localConnIdLength, FlushStrategy flushStrategy) {
        validate();
        QuicTokenHandler tokenHandler = this.tokenHandler;
        if (tokenHandler == null) {
            tokenHandler = NoQuicTokenHandler.INSTANCE;
        }
        QuicConnectionIdGenerator generator = connectionIdAddressGenerator;
        if (generator == null) {
            generator = QuicConnectionIdGenerator.signGenerator();
        }
        QuicResetTokenGenerator resetTokenGenerator = this.resetTokenGenerator;
        if (resetTokenGenerator == null) {
            resetTokenGenerator = QuicResetTokenGenerator.signGenerator();
        }
        ChannelHandler handler = this.handler;
        ChannelHandler streamHandler = this.streamHandler;
        return new QuicheQuicServerCodec(config, localConnIdLength, tokenHandler, generator, resetTokenGenerator,
                flushStrategy, sslEngineProvider, sslTaskExecutor, handler,
                Quic.toOptionsArray(options), Quic.toAttributesArray(attrs),
                streamHandler, Quic.toOptionsArray(streamOptions), Quic.toAttributesArray(streamAttrs));
    }
}
