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
import io.netty.util.concurrent.ProgressiveFuture;

/**
 * An special {@link ChannelFuture} which is used to indicate the {@link FileRegion} transfer progress
 * <p>特殊的 {@link ChannelFuture}，用于报告 {@link FileRegion} 等大数据传输的进度。
 * 可通过 {@link ProgressiveFuture#getProgress()} 查询已传输字节数。</p>
 */
public interface ChannelProgressiveFuture extends ChannelFuture, ProgressiveFuture<Void> {
    /** 添加完成监听器；返回 {@code this} 以支持链式调用。 */
    @Override
    ChannelProgressiveFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    /** 批量添加完成监听器。 */
    @Override
    ChannelProgressiveFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    /** 移除指定监听器。 */
    @Override
    ChannelProgressiveFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    /** 批量移除监听器。 */
    @Override
    ChannelProgressiveFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    /** 阻塞直至完成；中断时抛出 {@link InterruptedException}。 */
    @Override
    ChannelProgressiveFuture sync() throws InterruptedException;

    /** 不可中断地阻塞直至完成。 */
    @Override
    ChannelProgressiveFuture syncUninterruptibly();

    /** 等待完成但不强制成功；可被中断。 */
    @Override
    ChannelProgressiveFuture await() throws InterruptedException;

    /** 不可中断地等待完成。 */
    @Override
    ChannelProgressiveFuture awaitUninterruptibly();
}
