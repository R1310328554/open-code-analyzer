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

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ProgressivePromise;

/**
 * Special {@link ChannelPromise} which will be notified once the associated bytes is transferring.
 * <p>可写且支持进度通知的 {@link ChannelPromise}：在字节传输过程中可调用
 * {@link #setProgress(long, long)} 更新进度，并通知已注册的渐进式监听器。</p>
 */
public interface ChannelProgressivePromise extends ProgressivePromise<Void>, ChannelProgressiveFuture, ChannelPromise {

    /** 添加完成监听器；返回 {@code this} 以支持链式调用。 */
    @Override
    ChannelProgressivePromise addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    /** 批量添加完成监听器。 */
    @Override
    ChannelProgressivePromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    /** 移除指定监听器。 */
    @Override
    ChannelProgressivePromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    /** 批量移除监听器。 */
    @Override
    ChannelProgressivePromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    /** 阻塞直至完成；中断时抛出 {@link InterruptedException}。 */
    @Override
    ChannelProgressivePromise sync() throws InterruptedException;

    /** 不可中断地阻塞直至完成。 */
    @Override
    ChannelProgressivePromise syncUninterruptibly();

    /** 等待完成但不强制成功；可被中断。 */
    @Override
    ChannelProgressivePromise await() throws InterruptedException;

    /** 不可中断地等待完成。 */
    @Override
    ChannelProgressivePromise awaitUninterruptibly();

    /** 标记操作成功并设置结果。 */
    @Override
    ChannelProgressivePromise setSuccess(Void result);

    /** 标记操作成功（无返回值）。 */
    @Override
    ChannelProgressivePromise setSuccess();

    /** 标记操作失败并设置异常原因。 */
    @Override
    ChannelProgressivePromise setFailure(Throwable cause);

    /** 更新传输进度；{@code progress} 为已传输字节，{@code total} 为总字节数。 */
    @Override
    ChannelProgressivePromise setProgress(long progress, long total);

    /** 若为 void promise 则返回新的可写实例，否则返回自身。 */
    @Override
    ChannelProgressivePromise unvoid();
}
