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

import io.netty.channel.ChannelException;

/**
 * 读/写超时基类：在指定时间内既未读到也未写出数据时抛出。
 * <p>子类如 {@link ReadTimeoutException}、{@link WriteTimeoutException} 表示具体超时类型。</p>
 */
public class TimeoutException extends ChannelException {

    private static final long serialVersionUID = 4673641882869672533L;

    /** 包内无消息构造。 */
    TimeoutException() {
    }

    /**
     * @param message 异常消息
     * @param shared  是否为可共享的单例实例（共享时不填充堆栈）
     */
    TimeoutException(String message, boolean shared) {
        super(message, null, shared);
    }

    // 共享单例无需同步；故意不填充堆栈以降低 GC 压力
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
