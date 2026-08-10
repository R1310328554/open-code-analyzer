/*
 * Copyright 2013 The Netty Project
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

import io.netty.channel.ChannelFlushPromiseNotifier.FlushCheckpoint;
import io.netty.util.concurrent.DefaultProgressivePromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * The default {@link ChannelProgressivePromise} implementation.  It is recommended to use
 * {@link Channel#newProgressivePromise()} to create a new {@link ChannelProgressivePromise} rather than calling the
 * constructor explicitly.
 * <p>默认 {@link ChannelProgressivePromise} 实现：绑定 {@link Channel}，支持进度通知，
 * 并实现 {@link FlushCheckpoint} 以参与 flush 顺序追踪。建议通过
 * {@link Channel#newProgressivePromise()} 创建实例。</p>
 */
public class DefaultChannelProgressivePromise
        extends DefaultProgressivePromise<Void> implements ChannelProgressivePromise, FlushCheckpoint {

    /** 关联的 {@link Channel} */
    private final Channel channel;
    /** flush 检查点，用于 {@link ChannelFlushPromiseNotifier} 排序 */
    private long checkpoint;

    /**
     * Creates a new instance.
     * <p>创建与指定 {@link Channel} 绑定的渐进式 Promise。</p>
     *
     * @param channel
     *        the {@link Channel} associated with this future
     */
    public DefaultChannelProgressivePromise(Channel channel) {
        this.channel = channel;
    }

    /**
     * Creates a new instance.
     * <p>使用指定 {@link EventExecutor} 创建实例。</p>
     *
     * @param channel
     *        the {@link Channel} associated with this future
     */
    public DefaultChannelProgressivePromise(Channel channel, EventExecutor executor) {
        super(executor);
        this.channel = channel;
    }

    /** 未显式指定 executor 时回退到 {@link Channel#eventLoop()}。 */
    @Override
    protected EventExecutor executor() {
        EventExecutor e = super.executor();
        if (e == null) {
            return channel().eventLoop();
        } else {
            return e;
        }
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public ChannelProgressivePromise setSuccess() {
        return setSuccess(null);
    }

    @Override
    public ChannelProgressivePromise setSuccess(Void result) {
        super.setSuccess(result);
        return this;
    }

    @Override
    public boolean trySuccess() {
        return trySuccess(null);
    }

    @Override
    public ChannelProgressivePromise setFailure(Throwable cause) {
        super.setFailure(cause);
        return this;
    }

    /** 更新进度并通知监听器。 */
    @Override
    public ChannelProgressivePromise setProgress(long progress, long total) {
        super.setProgress(progress, total);
        return this;
    }

    @Override
    public ChannelProgressivePromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.addListener(listener);
        return this;
    }

    @Override
    public ChannelProgressivePromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.addListeners(listeners);
        return this;
    }

    @Override
    public ChannelProgressivePromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.removeListener(listener);
        return this;
    }

    @Override
    public ChannelProgressivePromise removeListeners(
            GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.removeListeners(listeners);
        return this;
    }

    @Override
    public ChannelProgressivePromise sync() throws InterruptedException {
        super.sync();
        return this;
    }

    @Override
    public ChannelProgressivePromise syncUninterruptibly() {
        super.syncUninterruptibly();
        return this;
    }

    @Override
    public ChannelProgressivePromise await() throws InterruptedException {
        super.await();
        return this;
    }

    @Override
    public ChannelProgressivePromise awaitUninterruptibly() {
        super.awaitUninterruptibly();
        return this;
    }

    /** 返回当前 flush 检查点。 */
    @Override
    public long flushCheckpoint() {
        return checkpoint;
    }

    /** 设置 flush 检查点。 */
    @Override
    public void flushCheckpoint(long checkpoint) {
        this.checkpoint = checkpoint;
    }

    @Override
    public ChannelProgressivePromise promise() {
        return this;
    }

    /** 仅在 Channel 已注册时检测 EventLoop 死锁。 */
    @Override
    protected void checkDeadLock() {
        if (channel().isRegistered()) {
            super.checkDeadLock();
        }
    }

    @Override
    public ChannelProgressivePromise unvoid() {
        return this;
    }

    /** 非 void promise，始终返回 {@code false}。 */
    @Override
    public boolean isVoid() {
        return false;
    }
}
