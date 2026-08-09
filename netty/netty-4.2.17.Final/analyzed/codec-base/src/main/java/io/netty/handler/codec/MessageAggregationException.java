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

package io.netty.handler.codec;

/**
 * 当 {@link MessageAggregator} 因意外消息序列导致聚合失败时抛出。
 */
/**
 * {@link MessageAggregator} 因意外消息序列导致聚合失败时抛出。
 */
public class MessageAggregationException extends IllegalStateException {

    private static final long serialVersionUID = -1995826182950310255L;

    /** 创建无消息实例。 */
    /** 创建无消息实例。 */
    public MessageAggregationException() { }

    /** @param s 异常消息 */
    /** @param s 异常消息 */
    public MessageAggregationException(String s) {
        super(s);
    }

    public MessageAggregationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageAggregationException(Throwable cause) {
        super(cause);
    }
}
