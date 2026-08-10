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

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

/**
 * Special {@link ChannelFuture} which is writable.
 * <p>可写的 {@link ChannelFuture}：允许调用方通过 {@link #setSuccess()}、
 * {@link #setFailure(Throwable)} 等主动完成 I/O 操作，常用于 {@link Channel#write} 等出站调用。</p>
 */
public interface ChannelPromise extends ChannelFuture, Promise<Void> {

    /** 返回与本 promise 关联的 {@link Channel}。 */
    @Override
    Channel channel();

    /** 标记操作成功并设置结果。 */
    @Override
    ChannelPromise setSuccess(Void result);

    /** 标记操作成功（无返回值）。 */
    ChannelPromise setSuccess();

    /** 尝试标记成功；若已完成则返回 {@code false}。 */
    boolean trySuccess();

    /** 标记操作失败并设置异常原因。 */
    @Override
    ChannelPromise setFailure(Throwable cause);

    /** 添加完成监听器；返回 {@code this} 以支持链式调用。 */
    @Override
    ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    /** 批量添加完成监听器。 */
    @Override
    ChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    /** 移除指定监听器。 */
    @Override
    ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    /** 批量移除监听器。 */
    @Override
    ChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    /** 阻塞直至完成；中断时抛出 {@link InterruptedException}。 */
    @Override
    ChannelPromise sync() throws InterruptedException;

    /** 不可中断地阻塞直至完成。 */
    @Override
    ChannelPromise syncUninterruptibly();

    /** 等待完成但不强制成功；可被中断。 */
    @Override
    ChannelPromise await() throws InterruptedException;

    /** 不可中断地等待完成。 */
    @Override
    ChannelPromise awaitUninterruptibly();

    /**
     * Returns a new {@link ChannelPromise} if {@link #isVoid()} returns {@code true} otherwise itself.
     * <p>若 {@link #isVoid()} 为 {@code true}（如 {@link Channel#voidPromise()}），
     * 则返回新的可写 promise；否则返回自身。</p>
     */
    ChannelPromise unvoid();
}
