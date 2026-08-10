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


/**
 * Listens to the result of a {@link ChannelFuture}.  The result of the
 * asynchronous {@link Channel} I/O operation is notified once this listener
 * is added by calling {@link ChannelFuture#addListener(GenericFutureListener)}.
 * <p>监听 {@link ChannelFuture} 完成结果的回调接口。通过
 * {@link ChannelFuture#addListener(GenericFutureListener)} 注册后，I/O 线程在操作完成时调用
 * {@link #operationComplete(Future)}。</p>
 *
 * <h3>Return the control to the caller quickly</h3>
 * <p>{@link #operationComplete(Future)} 由 I/O 线程直接调用，不宜在其中执行阻塞或耗时任务；
 * 如需阻塞后续逻辑，应提交到独立线程池处理。</p>
 */
public interface ChannelFutureListener extends GenericFutureListener<ChannelFuture> {

    /**
     * A {@link ChannelFutureListener} that closes the {@link Channel} which is
     * associated with the specified {@link ChannelFuture}.
     * <p>无论成功或失败，在 future 完成时关闭关联 {@link Channel} 的监听器。</p>
     */
    ChannelFutureListener CLOSE = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) {
            future.channel().close();
        }
    };

    /**
     * A {@link ChannelFutureListener} that closes the {@link Channel} when the
     * operation ended up with a failure or cancellation rather than a success.
     * <p>仅在操作失败或取消时关闭 {@link Channel} 的监听器。</p>
     */
    ChannelFutureListener CLOSE_ON_FAILURE = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) {
            if (!future.isSuccess()) {
                future.channel().close();
            }
        }
    };

    /**
     * A {@link ChannelFutureListener} that forwards the {@link Throwable} of the {@link ChannelFuture} into the
     * {@link ChannelPipeline}. This mimics the old behavior of Netty 3.
     * <p>操作失败时将 {@link ChannelFuture#cause()} 经 {@link ChannelPipeline#fireExceptionCaught(Throwable)}
     * 传播，模拟 Netty 3 的异常处理方式。</p>
     */
    ChannelFutureListener FIRE_EXCEPTION_ON_FAILURE = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) {
            if (!future.isSuccess()) {
                future.channel().pipeline().fireExceptionCaught(future.cause());
            }
        }
    };

    // Just a type alias
}
