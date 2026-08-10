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

/**
 * Event that is fired once we did a selection of a {@link SslContext} based on the {@code SNI hostname},
 * which may be because it was successful or there was an error.
 *
 * <p>基于 SNI 主机名选定 {@link SslContext} 后触发；{@link #isSuccess()} 为 true 表示映射成功。</p>
 */
public final class SniCompletionEvent extends SslCompletionEvent {
    /** 客户端 SNI 扩展中的主机名；解析失败或未提供时为 {@code null}。 */
    private final String hostname;

    public SniCompletionEvent(String hostname) {
        this.hostname = hostname;
    }

    public SniCompletionEvent(String hostname, Throwable cause) {
        super(cause);
        this.hostname = hostname;
    }

    public SniCompletionEvent(Throwable cause) {
        this(null, cause);
    }

    /**
     * Returns the SNI hostname send by the client if we were able to parse it, {@code null} otherwise.
     *
     * <p>返回客户端发送的 SNI 主机名；无法解析时返回 {@code null}。</p>
     */
    public String hostname() {
        return hostname;
    }

    @Override
    public String toString() {
        final Throwable cause = cause();
        return cause == null ? getClass().getSimpleName() + "(SUCCESS='"  + hostname + "'\")":
                getClass().getSimpleName() +  '(' + cause + ')';
    }
}
