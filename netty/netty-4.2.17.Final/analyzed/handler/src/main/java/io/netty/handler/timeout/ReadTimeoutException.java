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
package io.netty.handler.timeout;

/**
 * 读超时异常：{@link ReadTimeoutHandler} 在指定时间内未收到入站数据时抛出。
 * <p>默认使用无堆栈的 {@link #INSTANCE} 单例，可在 {@code exceptionCaught} 中按类型区分处理。</p>
 */
public final class ReadTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 169287984113283421L;

    /** 无堆栈的共享实例，供 handler 高频抛出。 */
    public static final ReadTimeoutException INSTANCE = new ReadTimeoutException(true);

    /** 创建带默认消息的可变实例。 */
    public ReadTimeoutException() { }

    /**
     * @param message 自定义异常消息
     */
    public ReadTimeoutException(String message) {
        super(message, false);
    }

    /** @param shared 是否为共享单例（不填充堆栈） */
    private ReadTimeoutException(boolean shared) {
        super(null, shared);
    }
}
