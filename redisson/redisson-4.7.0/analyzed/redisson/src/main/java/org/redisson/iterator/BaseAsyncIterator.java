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
package org.redisson.iterator;

import org.redisson.ScanResult;
import org.redisson.api.AsyncIterator;
import org.redisson.api.RFuture;
import org.redisson.client.RedisClient;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * 基于 Redis SCAN 的异步迭代器抽象基类。
 * <p>
 * 实现 {@link org.redisson.api.AsyncIterator}，按游标分批拉取元素，
 * 在 {@link #hasNext()} 与 {@link #next()} 中通过 {@link CompletionStage} 非阻塞推进。
 *
 * @author seakider
 *
 */
public abstract class BaseAsyncIterator<V, E> implements AsyncIterator<V> {
    /** 当前 SCAN 批次内的元素迭代器。 */
    private Iterator<E> lastIt;
    /** 下一批 SCAN 的游标位置（{@code null} 表示迭代结束）。 */
    protected String nextItPos;
    /** 当前 SCAN 命令所连接的 Redis 节点客户端。 */
    protected RedisClient client;

    /** 初始化游标为 {@link #initValue()}（默认 "0"）。 */
    protected BaseAsyncIterator() {
        nextItPos = initValue();
    }

    /** SCAN 起始游标，子类可覆写。 */
    protected String initValue() {
        return "0";
    }

    /** 异步判断是否还有下一元素，必要时触发新一轮 SCAN。 */
    @Override
    public CompletionStage<Boolean> hasNext() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        if (nextItPos == null && (lastIt == null || !lastIt.hasNext())) {
            result.complete(false);
            return result;
        }
        if (lastIt == null || !lastIt.hasNext()) {
            iterator(client, nextItPos).whenComplete((v, e) -> {
                if (e != null || v == null) {
                    client = null;
                    nextItPos = null;
                    if (e != null) {
                        result.completeExceptionally(e);
                    } else {
                        result.complete(false);
                    }
                } else {
                    client = v.getRedisClient();
                    nextItPos = v.getPos();
                    lastIt = v.getValues().iterator();
                    if (initValue().equals(nextItPos)) {
                        nextItPos = null;
                    }
                    result.complete(lastIt.hasNext());
                }
            });
        } else {
            result.complete(true);
        }
        return result;
    }
    
    /** 异步返回下一元素，无元素时以 {@link NoSuchElementException} 完成 exceptionally。 */
    @Override
    public CompletionStage<V> next() {
        CompletableFuture<V> result = new CompletableFuture<>();
        hasNext().whenComplete((v, e) -> {
            if (e != null) {
                result.completeExceptionally(e);
                return;
            }
            if (!v) {
                result.completeExceptionally(new NoSuchElementException());
                return;
            }
            E next = lastIt.next();
            result.complete(getValue(next));
        });
        return result;
    }
    
    /** 子类实现：对指定游标执行 SCAN 并返回 {@link ScanResult}。 */
    protected abstract RFuture<ScanResult<E>> iterator(RedisClient client, String nextItPos);
    
    /** 将 SCAN 条目转换为对外暴露的值类型，默认同类型强转。 */
    protected V getValue(E entry) {
        return (V) entry;
    }
}
