/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.codec.stomp;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;

/**
 * Default implementation of {@link StompHeadersSubframe}.
 * <p>{@link StompHeadersSubframe} 的默认实现，持有命令字与 {@link DefaultStompHeaders}。</p>
 */
public class DefaultStompHeadersSubframe implements StompHeadersSubframe {

    /** 本帧 STOMP 命令。 */
    protected final StompCommand command;
    /** 解码结果，默认可用。 */
    protected DecoderResult decoderResult = DecoderResult.SUCCESS;
    /** 本帧头部集合。 */
    protected final DefaultStompHeaders headers;

    /** 使用空 {@link DefaultStompHeaders} 构造。 */
    public DefaultStompHeadersSubframe(StompCommand command) {
        this(command, null);
    }

    /**
     * @param command 命令字，不可为 {@code null}
     * @param headers 可选头部；为 {@code null} 时新建空头部
     */
    DefaultStompHeadersSubframe(StompCommand command, DefaultStompHeaders headers) {
        this.command = ObjectUtil.checkNotNull(command, "command");
        this.headers = headers == null ? new DefaultStompHeaders() : headers;
    }

    @Override
    public StompCommand command() {
        return command;
    }

    @Override
    public StompHeaders headers() {
        return headers;
    }

    @Override
    public DecoderResult decoderResult() {
        return decoderResult;
    }

    @Override
    public void setDecoderResult(DecoderResult decoderResult) {
        this.decoderResult = decoderResult;
    }

    @Override
    public String toString() {
        return "StompFrame{" +
            "command=" + command +
            ", headers=" + headers +
            '}';
    }
}
