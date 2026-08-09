/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec;

import io.netty.channel.Channel;

/**
 * 编解码器尚未完成当前消息处理时 {@link Channel} 意外关闭所抛出的 {@link CodecException}。
 * <p>
 * 例如等待请求响应时连接提前断开。
 */
public class PrematureChannelClosureException extends CodecException {

    private static final long serialVersionUID = 4907642202594703094L;

    /** 创建无详细消息的新实例。 */
    public PrematureChannelClosureException() { }

    /** 创建带消息与原因的新实例。 */
    public PrematureChannelClosureException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 创建带消息的新实例。 */
    public PrematureChannelClosureException(String message) {
        super(message);
    }

    /** 创建带原因的新实例。 */
    public PrematureChannelClosureException(Throwable cause) {
        super(cause);
    }
}
