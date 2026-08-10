/*
 * Copyright 2016 The Netty Project
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
package io.netty.channel.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

import java.net.Socket;

/**
 * A duplex {@link Channel} that has two sides that can be shutdown independently.
 * <p>全双工 {@link Channel}，输入端与输出端可独立 shutdown，语义与 JDK {@link Socket} 的半关闭相关 API 对应但略有差异。</p>
 */
public interface DuplexChannel extends Channel {
    /**
     * Returns {@code true} if and only if the remote peer shut down its output so that no more
     * data is received from this channel.  Note that the semantic of this method is different from
     * that of {@link Socket#shutdownInput()} and {@link Socket#isInputShutdown()}.
     * <p>若远端已关闭其输出（本端不再收到数据）则返回 {@code true}；语义与 {@link Socket#isInputShutdown()} 不完全相同。</p>
     */
    boolean isInputShutdown();

    /**
     * @see Socket#shutdownInput()
     * <p>关闭本 channel 的输入端（停止接收）。</p>
     */
    ChannelFuture shutdownInput();

    /**
     * Will shutdown the input and notify {@link ChannelPromise}.
     *
     * @see Socket#shutdownInput()
     * <p>关闭输入端并在完成时通知 {@link ChannelPromise}。</p>
     */
    ChannelFuture shutdownInput(ChannelPromise promise);

    /**
     * @see Socket#isOutputShutdown()
     * <p>本 channel 输出端是否已 shutdown。</p>
     */
    boolean isOutputShutdown();

    /**
     * @see Socket#shutdownOutput()
     * <p>关闭本 channel 的输出端（停止发送）。</p>
     */
    ChannelFuture shutdownOutput();

    /**
     * Will shutdown the output and notify {@link ChannelPromise}.
     *
     * @see Socket#shutdownOutput()
     * <p>关闭输出端并在完成时通知 {@link ChannelPromise}。</p>
     */
    ChannelFuture shutdownOutput(ChannelPromise promise);

    /**
     * Determine if both the input and output of this channel have been shutdown.
     * <p>输入与输出是否均已 shutdown。</p>
     */
    boolean isShutdown();

    /**
     * Will shutdown the input and output sides of this channel.
     * @return will be completed when both shutdown operations complete.
     * <p>同时 shutdown 输入与输出，两侧操作均完成后 future 完成。</p>
     */
    ChannelFuture shutdown();

    /**
     * Will shutdown the input and output sides of this channel.
     * @param promise will be completed when both shutdown operations complete.
     * @return will be completed when both shutdown operations complete.
     * <p>同时 shutdown 输入与输出；{@code promise} 在两侧均完成后完成。</p>
     */
    ChannelFuture shutdown(ChannelPromise promise);
}
