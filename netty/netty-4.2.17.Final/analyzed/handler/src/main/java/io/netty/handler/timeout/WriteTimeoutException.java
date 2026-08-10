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
 * 写超时异常：{@link WriteTimeoutHandler} 在指定时间内写操作未完成时抛出。
 * <p>默认使用无堆栈的 {@link #INSTANCE} 单例。</p>
 */
public final class WriteTimeoutException extends TimeoutException {

    private static final long serialVersionUID = -144786655770296065L;

    /** 无堆栈的共享实例，供 handler 高频抛出。 */
    public static final WriteTimeoutException INSTANCE = new WriteTimeoutException(true);

    /** 创建带默认消息的可变实例。 */
    public WriteTimeoutException() { }

    /**
     * @param message 自定义异常消息
     */
    public WriteTimeoutException(String message) {
        super(message, false);
    }

    /** @param shared 是否为共享单例（不填充堆栈） */
    private WriteTimeoutException(boolean shared) {
        super(null, shared);
    }
}
