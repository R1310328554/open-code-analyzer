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

import io.netty.util.concurrent.CompleteFuture;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.ObjectUtil;

/**
 * A skeletal {@link ChannelFuture} implementation which represents a
 * {@link ChannelFuture} which has been completed already.
 * <p>已完成 {@link ChannelFuture} 的骨架实现：{@link #sync()}、{@link #await()} 等
 * 立即返回自身，{@link #isDone()} 恒为 {@code true}。</p>
 */
abstract class CompleteChannelFuture extends CompleteFuture<Void> implements ChannelFuture {

    /** 关联的 {@link Channel} */
    private final Channel channel;

    /**
     * Creates a new instance.
     * <p>创建与指定 {@link Channel} 关联的已完成 future。</p>
     *
     * @param channel the {@link Channel} associated with this future
     */
    protected CompleteChannelFuture(Channel channel, EventExecutor executor) {
        super(executor);
        this.channel = ObjectUtil.checkNotNull(channel, "channel");
    }

    /** 优先使用显式 executor，否则回退到 channel 的 EventLoop。 */
    @Override
    protected EventExecutor executor() {
        EventExecutor e = super.executor();
        if (e == null) {
            return channel().eventLoop();
        } else {
            return e;
        }
    }

    /** 添加监听器；已完成 future 会立即触发回调。 */
    @Override
    public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.addListener(listener);
        return this;
    }

    /** 批量添加监听器。 */
    @Override
    public ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.addListeners(listeners);
        return this;
    }

    /** 移除监听器。 */
    @Override
    public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.removeListener(listener);
        return this;
    }

    /** 批量移除监听器。 */
    @Override
    public ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.removeListeners(listeners);
        return this;
    }

    /** 已完成，直接返回自身。 */
    @Override
    public ChannelFuture syncUninterruptibly() {
        return this;
    }

    /** 已完成，直接返回自身。 */
    @Override
    public ChannelFuture sync() throws InterruptedException {
        return this;
    }

    /** 已完成，直接返回自身。 */
    @Override
    public ChannelFuture await() throws InterruptedException {
        return this;
    }

    /** 已完成，直接返回自身。 */
    @Override
    public ChannelFuture awaitUninterruptibly() {
        return this;
    }

    /** 返回关联的 {@link Channel}。 */
    @Override
    public Channel channel() {
        return channel;
    }

    /** 通道 future 无业务结果，恒为 {@code null}。 */
    @Override
    public Void getNow() {
        return null;
    }

    /** 非 void future，允许注册监听器与 await/sync。 */
    @Override
    public boolean isVoid() {
        return false;
    }
}
