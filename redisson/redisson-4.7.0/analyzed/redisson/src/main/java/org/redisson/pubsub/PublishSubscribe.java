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
package org.redisson.pubsub;

import org.redisson.PubSubEntry;
import org.redisson.client.BaseRedisPubSubListener;
import org.redisson.client.ChannelName;
import org.redisson.client.RedisPubSubListener;
import org.redisson.client.codec.LongCodec;
import org.redisson.misc.AsyncSemaphore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 内部 Pub/Sub 订阅模板：管理 entryName → {@link PubSubEntry} 映射，
 * 协调 {@link PublishSubscribeService} 完成 SUBSCRIBE/UNSUBSCRIBE。
 * 子类实现 {@link #createEntry} 与 {@link #onMessage} 处理具体业务消息。
 *
 * @author Nikita Koksharov
 *
 * @param <E> PubSub 条目类型
 */
abstract class PublishSubscribe<E extends PubSubEntry<E>> {

    /** 逻辑 entry 名称到本地 PubSub 条目的映射。 */
    private final ConcurrentMap<String, E> entries = new ConcurrentHashMap<>();
    /** 全局 Pub/Sub 连接与锁管理。 */
    private final PublishSubscribeService service;

    /** @param service 注入 PublishSubscribeService */
    PublishSubscribe(PublishSubscribeService service) {
        super();
        this.service = service;
    }

    /**
     * 递减 entry 引用计数；归零时从 map 移除并 UNSUBSCRIBE Redis 频道。
     * 通过 per-channel {@link AsyncSemaphore} 串行化订阅变更。
     */
    public void unsubscribe(E entry, String entryName, String channelName) {
        ChannelName cn = new ChannelName(channelName);
        AsyncSemaphore semaphore = service.getSemaphore(cn);
        semaphore.acquire().thenAccept(c -> {
            if (entry.release() == 0) {
                entries.remove(entryName);
                service.unsubscribeLocked(cn)
                        .whenComplete((r, e) -> {
                            semaphore.release();
                        });
            } else {
                semaphore.release();
            }
        });
    }

    /** 使用默认 subscriptionTimeout 为 promise 设置超时。 */
    public void timeout(CompletableFuture<?> promise) {
        service.timeout(promise);
    }

    /** 为订阅 promise 设置指定毫秒超时。 */
    public void timeout(CompletableFuture<?> promise, long timeout) {
        service.timeout(promise, timeout);
    }
    /** 订阅频道，默认 permits=1。 */
    public CompletableFuture<E> subscribe(String entryName, String channelName) {
        return subscribe(entryName, channelName, 1);
    }
    /**
     * 订阅 Redis 频道并返回 entry Future。
     * 若 entry 已存在则复用并 acquire(permits)；否则创建、SUBSCRIBE 并注册 listener。
     */
    public CompletableFuture<E> subscribe(String entryName, String channelName, int permits) {
        AsyncSemaphore semaphore = service.getSemaphore(new ChannelName(channelName));
        CompletableFuture<E> newPromise = new CompletableFuture<>();

        semaphore.acquire().thenAccept(c -> {
            if (newPromise.isDone()) {
                semaphore.release();
                return;
            }

            // 已有 entry：增加引用并等待其 promise
            E entry = entries.get(entryName);
            if (entry != null) {
                entry.acquire(permits);
                semaphore.release();
                entry.getPromise().whenComplete((r, e) -> {
                    if (e != null) {
                        newPromise.completeExceptionally(e);
                        return;
                    }
                    newPromise.complete(r);
                });
                return;
            }

            // 新建 entry 并尝试放入 map
            E value = createEntry(newPromise);
            value.acquire(permits);

            E oldValue = entries.putIfAbsent(entryName, value);
            if (oldValue != null) {
                oldValue.acquire(permits);
                semaphore.release();
                oldValue.getPromise().whenComplete((r, e) -> {
                    if (e != null) {
                        newPromise.completeExceptionally(e);
                        return;
                    }
                    newPromise.complete(r);
                });
                return;
            }

            // 向 Redis SUBSCRIBE 并绑定 LongCodec 消息监听
            RedisPubSubListener<Object> listener = createListener(channelName, value);
            CompletableFuture<PubSubConnectionEntry> s = service.subscribeNoTimeout(LongCodec.INSTANCE, channelName, semaphore, listener);
            newPromise.whenComplete((r, e) -> {
                if (e != null) {
                    s.completeExceptionally(e);
                }
            });
            s.whenComplete((r, e) -> {
                if (e != null) {
                    entries.remove(entryName);
                    value.getPromise().completeExceptionally(e);
                    return;
                }
                if (!value.getPromise().complete(value)) {
                    if (value.getPromise().isCompletedExceptionally()) {
                        entries.remove(entryName);
                    }
                }
            });

        });

        return newPromise;
    }

    /** 子类创建具体 PubSub 条目实例。 */
    protected abstract E createEntry(CompletableFuture<E> newPromise);

    /** 子类处理频道 Long 载荷。 */
    protected abstract void onMessage(E value, Long message);

    /** 包装 onMessage，过滤非目标频道。 */
    private RedisPubSubListener<Object> createListener(String channelName, E value) {
        RedisPubSubListener<Object> listener = new BaseRedisPubSubListener() {

            @Override
            public void onMessage(CharSequence channel, Object message) {
                if (!channelName.equals(channel.toString())) {
                    return;
                }

                PublishSubscribe.this.onMessage(value, (Long) message);
            }
        };
        return listener;
    }

}
