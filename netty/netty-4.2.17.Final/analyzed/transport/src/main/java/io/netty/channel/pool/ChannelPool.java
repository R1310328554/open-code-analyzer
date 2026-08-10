/*
 * Copyright 2015 The Netty Project
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
package io.netty.channel.pool;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import java.io.Closeable;

/**
 * Allows to acquire and release {@link Channel} and so act as a pool of these.
 * <p>Channel 连接池：借出（acquire）与归还（release）{@link Channel}。</p>
 */
public interface ChannelPool extends Closeable {

    /**
     * Acquire a {@link Channel} from this {@link ChannelPool}. The returned {@link Future} is notified once
     * the acquire is successful and failed otherwise.
     * <p>从池中借出 channel；成功或失败时通知 Future。</p>
     *
     * <strong>Its important that an acquired is always released to the pool again, even if the {@link Channel}
     * is explicitly closed..</strong>
     * <p><strong>借出的 channel 必须归还池，即使已显式 close。</strong></p>
     */
    Future<Channel> acquire();

    /**
     * Acquire a {@link Channel} from this {@link ChannelPool}. The given {@link Promise} is notified once
     * the acquire is successful and failed otherwise.
     * <p>借出 channel 并通知调用方提供的 {@link Promise}。</p>
     *
     * <strong>Its important that an acquired is always released to the pool again, even if the {@link Channel}
     * is explicitly closed..</strong>
     * <p><strong>借出的 channel 必须归还池，即使已显式 close。</strong></p>
     */
    Future<Channel> acquire(Promise<Channel> promise);

    /**
     * Release a {@link Channel} back to this {@link ChannelPool}. The returned {@link Future} is notified once
     * the release is successful and failed otherwise. When failed the {@link Channel} will automatically closed.
     * <p>将 channel 归还池；失败时 channel 会被自动关闭。</p>
     */
    Future<Void> release(Channel channel);

    /**
     * Release a {@link Channel} back to this {@link ChannelPool}. The given {@link Promise} is notified once
     * the release is successful and failed otherwise. When failed the {@link Channel} will automatically closed.
     * <p>归还 channel 并通知 {@link Promise}；失败时自动关闭 channel。</p>
     */
    Future<Void> release(Channel channel, Promise<Void> promise);

    @Override
    void close();
}
