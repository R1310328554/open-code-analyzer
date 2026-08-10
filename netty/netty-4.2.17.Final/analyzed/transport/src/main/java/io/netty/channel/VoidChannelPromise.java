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

import io.netty.util.concurrent.AbstractFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.UnstableApi;

import java.util.concurrent.TimeUnit;

/**
 * 不存储结果、不阻塞等待的“空”{@link ChannelPromise}，常用于内部 fire-and-forget 写操作。
 * <p>多数 mutating 方法会抛出 {@link IllegalStateException}；失败时可选地将异常
 * 通过 pipeline 的 {@code fireExceptionCaught} 传播。</p>
 */
@UnstableApi
public final class VoidChannelPromise extends AbstractFuture<Void> implements ChannelPromise {

    /** 关联的 {@link Channel} */
    private final Channel channel;
    // Will be null if we should not propagate exceptions through the pipeline on failure case.
    /** 失败时是否经 pipeline 传播异常；为 {@code null} 则不传播 */
    private final ChannelFutureListener fireExceptionListener;

    /**
     * Creates a new instance.
     * <p>创建 void promise；{@code fireException=true} 时在 channel 已注册的情况下
     * 将失败原因投递到 pipeline。</p>
     *
     * @param channel the {@link Channel} associated with this future
     * @param fireException 失败时是否触发 {@code fireExceptionCaught}
     */
    public VoidChannelPromise(final Channel channel, boolean fireException) {
        ObjectUtil.checkNotNull(channel, "channel");
        this.channel = channel;
        if (fireException) {
            fireExceptionListener = new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    Throwable cause = future.cause();
                    if (cause != null) {
                        fireException0(cause);
                    }
                }
            };
        } else {
            fireExceptionListener = null;
        }
    }

    /** void promise 不支持添加监听器 */
    @Override
    public VoidChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        fail();
        return this;
    }

    @Override
    public VoidChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        fail();
        return this;
    }

    @Override
    public VoidChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        // NOOP
        return this;
    }

    @Override
    public VoidChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        // NOOP
        return this;
    }

    @Override
    public VoidChannelPromise await() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) {
        fail();
        return false;
    }

    @Override
    public boolean await(long timeoutMillis) {
        fail();
        return false;
    }

    @Override
    public VoidChannelPromise awaitUninterruptibly() {
        fail();
        return this;
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        fail();
        return false;
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        fail();
        return false;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean setUncancellable() {
        return true;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public Throwable cause() {
        return null;
    }

    @Override
    public VoidChannelPromise sync() {
        fail();
        return this;
    }

    @Override
    public VoidChannelPromise syncUninterruptibly() {
        fail();
        return this;
    }

    /** 设置失败原因并可选地经 pipeline 传播异常 */
    @Override
    public VoidChannelPromise setFailure(Throwable cause) {
        fireException0(cause);
        return this;
    }

    /** 成功设置对 void promise 无效果 */
    @Override
    public VoidChannelPromise setSuccess() {
        return this;
    }

    /** 尝试设置失败；始终返回 {@code false} */
    @Override
    public boolean tryFailure(Throwable cause) {
        fireException0(cause);
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @param mayInterruptIfRunning this value has no effect in this implementation.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    /** 尝试标记成功；始终返回 {@code false} */
    @Override
    public boolean trySuccess() {
        return false;
    }

    /** void future 上调用 mutating/await 操作时抛出 */
    private static void fail() {
        throw new IllegalStateException("void future");
    }

    @Override
    public VoidChannelPromise setSuccess(Void result) {
        return this;
    }

    @Override
    public boolean trySuccess(Void result) {
        return false;
    }

    @Override
    public Void getNow() {
        return null;
    }

    /** 转为可等待、可监听的普通 {@link ChannelPromise} */
    @Override
    public ChannelPromise unvoid() {
        ChannelPromise promise = new DefaultChannelPromise(channel);
        if (fireExceptionListener != null) {
            promise.addListener(fireExceptionListener);
        }
        return promise;
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    /** 仅在 channel 已注册且配置了传播时，向 pipeline 投递异常 */
    private void fireException0(Throwable cause) {
        // Only fire the exception if the channel is open and registered
        // if not the pipeline is not setup and so it would hit the tail
        // of the pipeline.
        // See https://github.com/netty/netty/issues/1517
        if (fireExceptionListener != null && channel.isRegistered()) {
            channel.pipeline().fireExceptionCaught(cause);
        }
    }
}
