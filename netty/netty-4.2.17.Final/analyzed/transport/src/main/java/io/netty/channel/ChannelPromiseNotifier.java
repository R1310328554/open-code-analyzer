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
package io.netty.channel;

import io.netty.util.concurrent.PromiseNotifier;

/**
 * ChannelFutureListener implementation which takes other {@link ChannelPromise}(s) and notifies them on completion.
 * <p>已废弃：作为 {@link ChannelFutureListener}，在源 future 完成时将结果转发至
 * 一个或多个 {@link ChannelPromise}。请改用 {@link PromiseNotifier}。</p>
 *
 * @deprecated use {@link PromiseNotifier}.
 */
@Deprecated
public final class ChannelPromiseNotifier
    extends PromiseNotifier<Void, ChannelFuture>
    implements ChannelFutureListener {

    /**
     * Create a new instance
     * <p>创建实例：源 future 完成时同步通知所有 {@code promises}。</p>
     *
     * @param promises  the {@link ChannelPromise}s to notify once this {@link ChannelFutureListener} is notified.
     */
    public ChannelPromiseNotifier(ChannelPromise... promises) {
        super(promises);
    }

    /**
     * Create a new instance
     * <p>创建实例，可选择在通知失败时记录日志。</p>
     *
     * @param logNotifyFailure {@code true} if logging should be done in case notification fails.
     * @param promises  the {@link ChannelPromise}s to notify once this {@link ChannelFutureListener} is notified.
     */
    public ChannelPromiseNotifier(boolean logNotifyFailure, ChannelPromise... promises) {
        super(logNotifyFailure, promises);
    }
}
