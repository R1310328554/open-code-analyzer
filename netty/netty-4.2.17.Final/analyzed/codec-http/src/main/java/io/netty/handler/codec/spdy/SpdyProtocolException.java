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
package io.netty.handler.codec.spdy;

import io.netty.util.internal.ThrowableUtil;

/**
 * SPDY 协议违规或会话/流状态不一致时抛出的受检异常。
 * <p>{@link SpdySessionHandler} 捕获后会触发 GOAWAY 或 RST_STREAM 等协议规定的错误处理。
 */
public class SpdyProtocolException extends Exception {

    private static final long serialVersionUID = 7870000537743847264L;

    /** 创建无消息的实例。 */
    public SpdyProtocolException() { }

    /** 创建带消息与原因的实例。 */
    public SpdyProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 创建带消息的实例。 */
    public SpdyProtocolException(String message) {
        super(message);
    }

    /** 创建以给定异常为原因的实例。 */
    public SpdyProtocolException(Throwable cause) {
        super(cause);
    }

    /** 创建无堆栈跟踪的静态单例异常，避免热路径上填充 stack trace 的开销 */
    static SpdyProtocolException newStatic(String message, Class<?> clazz, String method) {
        final SpdyProtocolException exception = new StacklessSpdyProtocolException(message, true);
        return ThrowableUtil.unknownStackTrace(exception, clazz, method);
    }

    private SpdyProtocolException(String message, boolean shared) {
        super(message, null, false, true);
        assert shared;
    }

    /** 无堆栈版本，供 {@link #newStatic} 使用 */
    private static final class StacklessSpdyProtocolException extends SpdyProtocolException {
        private static final long serialVersionUID = -6302754207557485099L;

        StacklessSpdyProtocolException(String message, boolean shared) {
            super(message, shared);
        }

        // 不重写 fillInStackTrace，避免 native 调用并防止 ClassLoader 泄漏
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
