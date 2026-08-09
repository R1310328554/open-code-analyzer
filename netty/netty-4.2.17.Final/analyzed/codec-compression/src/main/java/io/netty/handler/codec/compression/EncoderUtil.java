/*
 * Copyright 2023 The Netty Project
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
package io.netty.handler.codec.compression;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;

import java.util.concurrent.TimeUnit;

/**
 * 压缩编码器通道工具：在编码完成后安全关闭 {@link ChannelHandlerContext}。
 */
final class EncoderUtil {
    /** 编码未完成时延迟关闭通道的秒数。 */
    private static final int THREAD_POOL_DELAY_SECONDS = 10;

    /**
     * 监听 {@code finishFuture}，完成后关闭通道；若超时未完成则 10 秒后强制关闭。
     * @param ctx 通道上下文
     * @param finishFuture 编码结束写操作
     * @param promise 关闭操作的 promise
     */
    static void closeAfterFinishEncode(final ChannelHandlerContext ctx, final ChannelFuture finishFuture,
                                       final ChannelPromise promise) {
        if (!finishFuture.isDone()) {
            // 即使写操作超时未完成，也保证通道最终关闭
            final Future<?> future = ctx.executor().schedule(new Runnable() {
                @Override
                public void run() {
                    ctx.close(promise);
                }
            }, THREAD_POOL_DELAY_SECONDS, TimeUnit.SECONDS);

            finishFuture.addListener(f -> {
                // 写完成时取消定时关闭任务
                future.cancel(true);
                if (!promise.isDone()) {
                    ctx.close(promise);
                }
            });
        } else {
            ctx.close(promise);
        }
    }

    private EncoderUtil() { }
}

