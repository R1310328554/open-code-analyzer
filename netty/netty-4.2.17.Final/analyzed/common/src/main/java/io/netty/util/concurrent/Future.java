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
package io.netty.util.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;


/**
 * The result of an asynchronous operation.
 *
 * <p>Netty 异步操作结果接口，扩展 {@link java.util.concurrent.Future}，
 * 提供 success/cause 语义、链式 {@link #addListener}、{@link #await}/{@link #sync} 等 Netty 风格 API。</p>
 */
@SuppressWarnings("ClassNameSameAsAncestorName")
public interface Future<V> extends java.util.concurrent.Future<V> {

    /**
     * Returns {@code true} if and only if the I/O operation was completed
     * successfully.
     *
     * <p>操作是否成功完成（与 {@link #cause()} 配合判断失败原因）。</p>
     */
    boolean isSuccess();

    /**
     * returns {@code true} if and only if the operation can be cancelled via {@link #cancel(boolean)}.
     *
     * <p>是否仍可通过 {@link #cancel} 取消（未完成且未标记为不可取消时）。</p>
     */
    boolean isCancellable();

    /**
     * Returns the cause of the failed I/O operation if the I/O operation has
     * failed.
     *
     * @return the cause of the failure.
     *         {@code null} if succeeded or this future is not
     *         completed yet.
     *
     * <p>失败原因；成功或未完成时返回 {@code null}。</p>
     */
    Throwable cause();

    /**
     * Adds the specified listener to this future.  The
     * specified listener is notified when this future is
     * {@linkplain #isDone() done}.  If this future is already
     * completed, the specified listeners are notified immediately.
     *
     * <p>添加完成回调；若 Future 已完成则立即通知。</p>
     */
    Future<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);

    /**
     * Adds the specified listeners to this future.  The
     * specified listeners are notified when this future is
     * {@linkplain #isDone() done}.  If this future is already
     * completed, the specified listeners are notified immediately.
     *
     * <p>批量添加监听器。</p>
     */
    Future<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners);

    /**
     * Removes the first occurrence of the specified listener from this future.
     * The specified listener is no longer notified when this
     * future is {@linkplain #isDone() done}.  If the specified
     * listener is not associated with this future, this method
     * does nothing and returns silently.
     *
     * <p>移除首个匹配的监听器；不存在则静默忽略。</p>
     */
    Future<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener);

    /**
     * Removes the first occurrence for each of the listeners from this future.
     * The specified listeners are no longer notified when this
     * future is {@linkplain #isDone() done}.  If the specified
     * listeners are not associated with this future, this method
     * does nothing and returns silently.
     *
     * <p>批量移除监听器。</p>
     */
    Future<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners);

    /**
     * Waits for this future until it is done, and rethrows the cause of the failure if this future
     * failed.
     *
     * <p>阻塞至完成；失败时重新抛出 cause（成功则返回 this）。</p>
     */
    Future<V> sync() throws InterruptedException;

    /**
     * Waits for this future until it is done, and rethrows the cause of the failure if this future
     * failed.
     *
     * <p>不可中断版 {@link #sync()}。</p>
     */
    Future<V> syncUninterruptibly();

    /**
     * Waits for this future to be completed.
     *
     * @throws InterruptedException
     *         if the current thread was interrupted
     *
     * <p>阻塞等待完成，不重新抛出失败原因。</p>
     */
    Future<V> await() throws InterruptedException;

    /**
     * Waits for this future to be completed without
     * interruption.  This method catches an {@link InterruptedException} and
     * discards it silently.
     *
     * <p>不可中断版 {@link #await()}。</p>
     */
    Future<V> awaitUninterruptibly();

    /**
     * Waits for this future to be completed within the
     * specified time limit.
     *
     * @return {@code true} if and only if the future was completed within
     *         the specified time limit
     *
     * @throws InterruptedException
     *         if the current thread was interrupted
     *
     * <p>限时等待；超时返回 {@code false}。</p>
     */
    boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Waits for this future to be completed within the
     * specified time limit.
     *
     * @return {@code true} if and only if the future was completed within
     *         the specified time limit
     *
     * @throws InterruptedException
     *         if the current thread was interrupted
     *
     * <p>限时等待（毫秒）。</p>
     */
    boolean await(long timeoutMillis) throws InterruptedException;

    /**
     * Waits for this future to be completed within the
     * specified time limit without interruption.  This method catches an
     * {@link InterruptedException} and discards it silently.
     *
     * @return {@code true} if and only if the future was completed within
     *         the specified time limit
     *
     * <p>不可中断限时等待。</p>
     */
    boolean awaitUninterruptibly(long timeout, TimeUnit unit);

    /**
     * Waits for this future to be completed within the
     * specified time limit without interruption.  This method catches an
     * {@link InterruptedException} and discards it silently.
     *
     * @return {@code true} if and only if the future was completed within
     *         the specified time limit
     *
     * <p>不可中断限时等待（毫秒）。</p>
     */
    boolean awaitUninterruptibly(long timeoutMillis);

    /**
     * Return the result without blocking. If the future is not done yet this will return {@code null}.
     * <p>
     * As it is possible that a {@code null} value is used to mark the future as successful you also need to check
     * if the future is really done with {@link #isDone()} and not rely on the returned {@code null} value.
     *
     * <p>非阻塞获取结果；未完成返回 {@code null}。注意成功结果本身也可能为 {@code null}，需结合 {@link #isDone()} 判断。</p>
     */
    V getNow();

    /**
     * {@inheritDoc}
     *
     * If the cancellation was successful it will fail the future with a {@link CancellationException}.
     *
     * <p>取消成功时 Future 以 {@link CancellationException} 失败完成。</p>
     */
    @Override
    boolean cancel(boolean mayInterruptIfRunning);
}
