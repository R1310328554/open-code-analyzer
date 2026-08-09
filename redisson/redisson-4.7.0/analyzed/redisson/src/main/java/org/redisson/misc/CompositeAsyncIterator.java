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
package org.redisson.misc;

import org.redisson.api.AsyncIterator;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * 将多个 {@link AsyncIterator} 顺序拼接为一个异步迭代器。
 * <p>
 * 当前子迭代器耗尽后自动切换到下一个；
 * 可选 {@code limit} 限制总共产出的元素个数。
 *
 * @author seakider
 *
 */
public class CompositeAsyncIterator<T> implements AsyncIterator<T> {
    /** 待遍历的子 AsyncIterator 列表迭代器。 */
    private final Iterator<AsyncIterator<T>> iterator;
    /** 当前正在读取的子异步迭代器。 */
    private AsyncIterator<T> currentAsyncIterator;
    /** 最大元素数；{@code <= 0} 表示无限制。 */
    private final int limit;
    /** 已返回的元素计数。 */
    private int counter;
    
    /** 用子迭代器列表与上限构造组合异步迭代器。 */
    public CompositeAsyncIterator(List<AsyncIterator<T>> asyncIterators, int limit) {
        this.iterator = asyncIterators.iterator();
        this.limit = limit;
    }
    
    /** 异步判断是否存在下一元素，必要时递归切换子迭代器。 */
    @Override
    public CompletionStage<Boolean> hasNext() {
        // 已达上限，直接返回 false
        if (limit > 0 && limit <= counter) {
            return CompletableFuture.completedFuture(false);
        }
        while (currentAsyncIterator == null && iterator.hasNext()) {
            currentAsyncIterator = iterator.next();
        }
        if (currentAsyncIterator == null) {
            return CompletableFuture.completedFuture(false);
            
        }
        CompletionStage<Boolean> main = currentAsyncIterator.hasNext();
        return main.thenCompose(v -> {
            if (v) {
                return CompletableFuture.completedFuture(true);
            } else {
                currentAsyncIterator = null;
                return hasNext();
            }
        });
    }
    
    /** 异步获取下一元素并在成功后递增计数器。 */
    @Override
    public CompletionStage<T> next() {
        CompletableFuture<T> result = new CompletableFuture<>();
        hasNext().whenComplete((v1, e) -> {
            if (e != null) {
                result.completeExceptionally(e);
                return;
            }
            if (!v1) {
                result.completeExceptionally(new NoSuchElementException());
                return;
            }
            currentAsyncIterator.next().whenComplete((v2, e2) -> {
                if (e2 != null) {
                    result.completeExceptionally(new CompletionException(e2));
                    return;
                }
                result.complete(v2);
                counter++;
            });
        });
        return result;
    }
}
