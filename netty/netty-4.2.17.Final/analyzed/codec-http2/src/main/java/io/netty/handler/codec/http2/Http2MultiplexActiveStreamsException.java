/*
 * Copyright 2023 The Netty Project
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
package io.netty.handler.codec.http2;

/**
 * 多路复用活跃流异常包装器：将底层 {@link Throwable} 包装后沿父 channel pipeline 传播。
 * <p>{@link Http2MultiplexHandler} 捕获后会解包原始异常，并分发给所有活跃
 * {@link Http2StreamChannel} 子 channel。
 */
public final class Http2MultiplexActiveStreamsException extends Exception {

    public Http2MultiplexActiveStreamsException(Throwable cause) {
        super(cause);
    }

    /**
     * 跳过堆栈填充：此异常仅作传播载体，不需要真实调用栈。
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
