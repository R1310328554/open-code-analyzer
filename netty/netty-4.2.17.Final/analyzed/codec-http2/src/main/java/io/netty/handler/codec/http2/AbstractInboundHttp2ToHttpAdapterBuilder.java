/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.http2;

import io.netty.handler.codec.TooLongFrameException;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * {@link InboundHttp2ToHttpAdapter} 及其子类的骨架 Builder。
 * <p>将 HTTP/2 入站帧聚合为 {@link io.netty.handler.codec.http.HttpObject}，
 * 子类实现 {@link #build(Http2Connection, int, boolean, boolean)} 以构造具体适配器实例。
 */
public abstract class AbstractInboundHttp2ToHttpAdapterBuilder<
        T extends InboundHttp2ToHttpAdapter, B extends AbstractInboundHttp2ToHttpAdapterBuilder<T, B>> {

    private final Http2Connection connection;
    private int maxContentLength;
    private boolean validateHttpHeaders;
    private boolean propagateSettings;

    /**
     * 为指定 {@link Http2Connection} 创建 Builder；构建出的适配器会注册为连接监听器。
     *
     * @param connection 提供当前连接的流生命周期与 SETTINGS 通知
     */
    protected AbstractInboundHttp2ToHttpAdapterBuilder(Http2Connection connection) {
        this.connection = checkNotNull(connection, "connection");
    }

    @SuppressWarnings("unchecked")
    protected final B self() {
        return (B) this;
    }

    /** 返回绑定的 {@link Http2Connection}。 */
    protected Http2Connection connection() {
        return connection;
    }

    /** 返回消息体（DATA 聚合）允许的最大长度。 */
    protected int maxContentLength() {
        return maxContentLength;
    }

    /**
     * 设置消息体最大长度；超出时抛出 {@link TooLongFrameException}。
     *
     * @param maxContentLength 聚合 DATA 帧后的内容上限（字节）
     * @return {@link AbstractInboundHttp2ToHttpAdapterBuilder} the builder for the {@link InboundHttp2ToHttpAdapter}
     */
    protected B maxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
        return self();
    }

    /** 是否对转换后的 HTTP 头执行 http-codec 校验。 */
    protected boolean isValidateHttpHeaders() {
        return validateHttpHeaders;
    }

    /**
     * 指定是否在 http-codec 层校验 HTTP 头。
     *
     * @param validate
     * <ul>
     * <li>{@code true} to validate HTTP headers in the http-codec</li>
     * <li>{@code false} not to validate HTTP headers in the http-codec</li>
     * </ul>
     * @return {@link AbstractInboundHttp2ToHttpAdapterBuilder} the builder for the {@link InboundHttp2ToHttpAdapter}
     */
    protected B validateHttpHeaders(boolean validate) {
        validateHttpHeaders = validate;
        return self();
    }

    /** 是否将读到的 SETTINGS 帧继续向 pipeline 下游传播。 */
    protected boolean isPropagateSettings() {
        return propagateSettings;
    }

    /**
     * 指定是否沿 pipeline 传播 SETTINGS 帧。
     * <p>客户端可在收到对端 SETTINGS 后再发数据，避免违反初始窗口等约束。
     *
     * @param propagate if {@code true} read settings will be passed along the pipeline. This can be useful
     *                     to clients that need hold off sending data until they have received the settings.
     * @return {@link AbstractInboundHttp2ToHttpAdapterBuilder} the builder for the {@link InboundHttp2ToHttpAdapter}
     */
    protected B propagateSettings(boolean propagate) {
        propagateSettings = propagate;
        return self();
    }

    /**
     * 按当前配置构建 {@link InboundHttp2ToHttpAdapter}，并注册为 {@link Http2Connection} 监听器。
     */
    protected T build() {
        final T instance;
        try {
            instance = build(connection(), maxContentLength(),
                                     isValidateHttpHeaders(), isPropagateSettings());
        } catch (Throwable t) {
            throw new IllegalStateException("failed to create a new InboundHttp2ToHttpAdapter", t);
        }
        connection.addListener(instance);
        return instance;
    }

    /**
     * 子类实现：用给定参数构造具体适配器实例。
     */
    protected abstract T build(Http2Connection connection, int maxContentLength,
                               boolean validateHttpHeaders, boolean propagateSettings) throws Exception;
}
