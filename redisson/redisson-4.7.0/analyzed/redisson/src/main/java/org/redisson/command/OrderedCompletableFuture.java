/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.command;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

/**
 * 保证 {@link #whenComplete} 回调按注册顺序执行的 {@link CompletableFuture} 包装。
 * <p>标准 {@code CompletableFuture.whenComplete} 在已完成时可能并发触发回调；
 * 本类将回调入队，在父 Future 完成时按 FIFO 顺序依次调用，用于连接获取等有序场景。
 *
 * @author Nikita Koksharov
 *
 */
public final class OrderedCompletableFuture<V> extends CompletableFuture<V> {

    /** 待执行的 whenComplete 回调队列。 */
    final Queue<BiConsumer<? super V, ? super Throwable>> actions = new ConcurrentLinkedQueue<>();

    /** 被包装的父 Future，状态变更委托给它。 */
    final CompletableFuture<V> parentFuture;

    /** @param parentFuture 底层 Future，完成时触发队列中的回调 */
    public OrderedCompletableFuture(CompletableFuture<V> parentFuture) {
        this.parentFuture = parentFuture;
        parentFuture.whenComplete((r, e) -> {
            invokeActions(r, e);
        });
    }

    /** 依次执行队列中所有 whenComplete 回调。 */
    void invokeActions(V r, Throwable e) {
        while (true) {
            BiConsumer<? super V, ? super Throwable> action = actions.poll();
            if (action != null) {
                action.accept(r, e);
            } else {
                break;
            }
        }
    }

    /** 从父 Future 取当前结果并触发回调（已完成时使用）。 */
    void invokeActions() {
        try {
            V r = parentFuture.getNow(null);
            invokeActions(r, null);
        } catch (CompletionException e) {
            invokeActions(null, e.getCause());
        }
    }

    /** 注册回调；若父 Future 已完成则立即按序触发。 */
    @Override
    public CompletableFuture<V> whenComplete(BiConsumer<? super V, ? super Throwable> action) {
        actions.add(action);
        if (parentFuture.isDone()) {
            invokeActions();
        }
        return this;
    }

    @Override
    public V getNow(V valueIfAbsent) {
        return parentFuture.getNow(valueIfAbsent);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return parentFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean complete(V value) {
        return parentFuture.complete(value);
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        return parentFuture.completeExceptionally(ex);
    }

    @Override
    public boolean isDone() {
        return parentFuture.isDone();
    }

    @Override
    public boolean isCompletedExceptionally() {
        return parentFuture.isCompletedExceptionally();
    }

    @Override
    public boolean isCancelled() {
        return parentFuture.isCancelled();
    }

    @Override
    public V join() {
        return parentFuture.join();
    }
}
