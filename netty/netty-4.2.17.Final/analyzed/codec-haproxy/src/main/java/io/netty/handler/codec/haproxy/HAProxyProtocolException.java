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
package io.netty.handler.codec.haproxy;

import io.netty.handler.codec.DecoderException;

/**
 * 遇到无效 HAProxy PROXY 协议头部时抛出的 {@link DecoderException}。
 * <p>
 * 解码器在协议格式错误、长度超限等情况下抛出，并通常伴随连接关闭。
 */
public class HAProxyProtocolException extends DecoderException {

    private static final long serialVersionUID = 713710864325167351L;

    /** 创建无消息与原因的异常实例。 */
    public HAProxyProtocolException() { }

    /** 创建带消息与原因的异常实例。 */
    public HAProxyProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 创建带消息的异常实例。 */
    public HAProxyProtocolException(String message) {
        super(message);
    }

    /** 创建带原因的异常实例。 */
    public HAProxyProtocolException(Throwable cause) {
        super(cause);
    }
}
