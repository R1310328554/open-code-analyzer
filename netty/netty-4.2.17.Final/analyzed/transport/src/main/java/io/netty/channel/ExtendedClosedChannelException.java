/*
 * Copyright 2019 The Netty Project
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
package io.netty.channel;

import java.nio.channels.ClosedChannelException;

/**
 * 扩展的 {@link ClosedChannelException}，用于在通道已关闭时携带原始失败原因。
 * <p>内部类；通过 {@link #fillInStackTrace()} 跳过堆栈填充以降低异常创建开销。</p>
 */
final class ExtendedClosedChannelException extends ClosedChannelException {

    /**
     * 构造异常并可选地设置 {@code cause}。
     *
     * @param cause 底层失败原因，可为 {@code null}
     */
    ExtendedClosedChannelException(Throwable cause) {
        if (cause != null) {
            initCause(cause);
        }
    }

    // Suppress a warning since the method doesn't need synchronization
    // 无需同步；不填充堆栈以降低开销
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
