/*
 * Copyright 2016 The Netty Project
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
package io.netty.util.concurrent;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * 将单个 {@link Future} 的结果级联到目标 {@link Promise} 的监听器。
 *
 * @deprecated use {@link PromiseNotifier#cascade(boolean, Future, Promise)}.
 */
@Deprecated
public final class UnaryPromiseNotifier<T> implements FutureListener<T> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(UnaryPromiseNotifier.class);
    private final Promise<? super T> promise;

    public UnaryPromiseNotifier(Promise<? super T> promise) {
        this.promise = ObjectUtil.checkNotNull(promise, "promise");
    }

    /** 完成时将 success/cancel/failure 传播到构造时绑定的 promise。 */
    @Override
    public void operationComplete(Future<T> future) throws Exception {
        cascadeTo(future, promise);
    }

    /** 静态级联：成功 trySuccess，取消 cancel，失败 tryFailure；已完成的 promise 打 warn 日志。 */
    public static <X> void cascadeTo(Future<X> completedFuture, Promise<? super X> promise) {
        if (completedFuture.isSuccess()) {
            if (!promise.trySuccess(completedFuture.getNow())) {
                logger.warn("Failed to mark a promise as success because it is done already: {}", promise);
            }
        } else if (completedFuture.isCancelled()) {
            if (!promise.cancel(false)) {
                logger.warn("Failed to cancel a promise because it is done already: {}", promise);
            }
        } else {
            if (!promise.tryFailure(completedFuture.cause())) {
                logger.warn("Failed to mark a promise as failure because it's done already: {}", promise,
                            completedFuture.cause());
            }
        }
    }
}
