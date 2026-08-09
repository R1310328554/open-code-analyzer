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
package org.redisson.spring.data.connection;

import org.redisson.client.BaseRedisPubSubListener;
import org.redisson.client.ChannelName;
import org.redisson.client.RedisPubSubListener;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.pubsub.PubSubType;
import org.redisson.connection.ConnectionManager;
import org.redisson.pubsub.PubSubConnectionEntry;
import org.redisson.pubsub.PublishSubscribeService;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.connection.SubscriptionListener;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Spring Data Redis 响应式 Pub/Sub 订阅实现。
 * <p>通过 {@link PublishSubscribeService} 管理频道与模式订阅；
 {@link #receive()} 以 Reactor {@link Flux} 推送 {@link ChannelMessage}/{@link PatternMessage}；
 订阅状态变更经 {@link SubscriptionListener} 回调。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReactiveSubscription implements ReactiveSubscription {

    /** 引用计数器：在并发 subscribe/unsubscribe 完成前延迟注册消息监听器。 */
    public static class ListenableCounter {

        private final AtomicInteger state = new AtomicInteger();
        private Runnable r;

        /** 递增进行中操作计数。 */
        public void acquire() {
            state.incrementAndGet();
        }

        /** 递减计数；归零时执行挂起的 {@link Runnable}。 */
        public void release() {
            if (state.decrementAndGet() != 0) {
                return;
            }

            if (r != null) {
                r.run();
                r = null;
            }
        }

        /** 无进行中操作时立即执行，否则在 {@link #release()} 归零时执行。 */
        public void addListener(Runnable r) {
            if (state.get() != 0) {
                this.r = r;
                return;
            }

            r.run();
        }

    }

    private final Map<ChannelName, Collection<PubSubConnectionEntry>> channels = new ConcurrentHashMap<>();
    private final Map<ChannelName, Collection<PubSubConnectionEntry>> patterns = new ConcurrentHashMap<>();

    private final ListenableCounter monosListener = new ListenableCounter();

    /** 内部 Pub/Sub 监听器，将 Redisson 状态事件转发给 {@link SubscriptionListener}。 */
    private final RedisPubSubListener subscriptionListener;
    /** Redisson Pub/Sub 订阅服务。 */
    private final PublishSubscribeService subscribeService;

    /** 从 {@link ConnectionManager} 获取 {@link PublishSubscribeService}，并绑定 {@link SubscriptionListener}。 */
    public RedissonReactiveSubscription(ConnectionManager connectionManager, SubscriptionListener subscriptionListener) {
        this.subscribeService = connectionManager.getSubscribeService();
        this.subscriptionListener = new RedisPubSubListener() {

            @Override
            public void onStatus(PubSubType type, CharSequence channel) {
                // 频道订阅成功：通知 Spring SubscriptionListener。
                if (type == PubSubType.SUBSCRIBE) {
                    subscriptionListener.onChannelSubscribed(channel.toString().getBytes(StandardCharsets.UTF_8), 1L);
                } else if (type == PubSubType.PSUBSCRIBE) {
                    subscriptionListener.onPatternSubscribed(channel.toString().getBytes(StandardCharsets.UTF_8), 1L);
                } else if (type == PubSubType.UNSUBSCRIBE) {
                    subscriptionListener.onChannelUnsubscribed(channel.toString().getBytes(StandardCharsets.UTF_8), 1L);
                } else if (type == PubSubType.PUNSUBSCRIBE) {
                    subscriptionListener.onPatternUnsubscribed(channel.toString().getBytes(StandardCharsets.UTF_8), 1L);
                }
            }

            @Override
            public void onPatternMessage(CharSequence pattern, CharSequence channel, Object message) {
            }

            @Override
            public void onMessage(CharSequence channel, Object msg) {
            }
        };
    }

    /** SUBSCRIBE：订阅一个或多个频道并记录连接条目。 */
    @Override
    public Mono<Void> subscribe(ByteBuffer... channels) {
        monosListener.acquire();
        return Mono.defer(() -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (ByteBuffer channel : channels) {
                ChannelName cn = toChannelName(channel);
                CompletableFuture<List<PubSubConnectionEntry>> f = subscribeService.subscribe(ByteArrayCodec.INSTANCE, cn, subscriptionListener);
                f = f.whenComplete((res, e) -> RedissonReactiveSubscription.this.channels.put(cn, res));
                futures.add(f);
            }

            CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            future = future.whenComplete((r, e) -> {
                monosListener.release();
            });
            return Mono.fromFuture(future);
        });
    }

    /** 将 {@link ByteBuffer} 频道名转为 {@link ChannelName}。 */
    protected ChannelName toChannelName(ByteBuffer channel) {
        return new ChannelName(RedissonBaseReactive.toByteArray(channel));
    }

    /** PSUBSCRIBE：按模式订阅一个或多个 pattern。 */
    @Override
    public Mono<Void> pSubscribe(ByteBuffer... patterns) {
        monosListener.acquire();
        return Mono.defer(() -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (ByteBuffer channel : patterns) {
                ChannelName cn = toChannelName(channel);
                CompletableFuture<Collection<PubSubConnectionEntry>> f = subscribeService.psubscribe(cn, ByteArrayCodec.INSTANCE, subscriptionListener);
                f = f.whenComplete((res, e) -> RedissonReactiveSubscription.this.patterns.put(cn, res));
                futures.add(f);
            }

            CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            future = future.whenComplete((r, e) -> {
                monosListener.release();
            });
            return Mono.fromFuture(future);
        });
    }

    /** 取消当前全部频道订阅。 */
    @Override
    public Mono<Void> unsubscribe() {
        return unsubscribe(channels.keySet().stream().map(b -> ByteBuffer.wrap(b.getName())).distinct().toArray(ByteBuffer[]::new));
    }

    /** UNSUBSCRIBE：取消指定频道订阅并清理空连接条目。 */
    @Override
    public Mono<Void> unsubscribe(ByteBuffer... channels) {
        monosListener.acquire();
        return Mono.defer(() -> {
            List<CompletableFuture<?>> futures = new ArrayList<>(channels.length);
            for (ByteBuffer channel : channels) {
                ChannelName cn = toChannelName(channel);
                CompletableFuture<Codec> f = subscribeService.unsubscribe(cn, PubSubType.UNSUBSCRIBE);
                f = f.whenComplete((res, e) -> {
                    RedissonReactiveSubscription.this.channels.computeIfPresent(cn, (key, entries) -> {
                        entries.removeIf(entry -> !entry.hasListeners(cn));
                        if (entries.isEmpty()) {
                            return null;
                        }
                        return entries;
                    });
                });
                futures.add(f);
            }

            CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            future = future.whenComplete((r, e) -> {
                monosListener.release();
            });
            return Mono.fromFuture(future);
        });
    }

    /** 取消当前全部模式订阅。 */
    @Override
    public Mono<Void> pUnsubscribe() {
        return unsubscribe(patterns.keySet().stream().map(b -> ByteBuffer.wrap(b.getName())).distinct().toArray(ByteBuffer[]::new));
    }

    /** PUNSUBSCRIBE：取消指定模式订阅并清理空连接条目。 */
    @Override
    public Mono<Void> pUnsubscribe(ByteBuffer... patterns) {
        monosListener.acquire();
        return Mono.defer(() -> {
            List<CompletableFuture<?>> futures = new ArrayList<>(patterns.length);
            for (ByteBuffer channel : patterns) {
                ChannelName cn = toChannelName(channel);
                CompletableFuture<Codec> f = subscribeService.unsubscribe(cn, PubSubType.PUNSUBSCRIBE);
                f = f.whenComplete((res, e) -> {
                    RedissonReactiveSubscription.this.patterns.computeIfPresent(cn, (key, entries) -> {
                        entries.removeIf(entry -> !entry.hasListeners(cn));
                        if (entries.isEmpty()) {
                            return null;
                        }
                        return entries;
                    });
                });
                futures.add(f);
            }

            CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            future = future.whenComplete((r, e) -> {
                monosListener.release();
            });
            return Mono.fromFuture(future);
        });
    }

    /** 返回当前已订阅的频道集合。 */
    @Override
    public Set<ByteBuffer> getChannels() {
        return channels.keySet().stream().map(b -> ByteBuffer.wrap(b.getName())).collect(Collectors.toSet());
    }

    /** 返回当前已订阅的模式集合。 */
    @Override
    public Set<ByteBuffer> getPatterns() {
        return patterns.keySet().stream().map(b -> ByteBuffer.wrap(b.getName())).collect(Collectors.toSet());
    }
    
    private final AtomicReference<Flux<Message<ByteBuffer, ByteBuffer>>> flux = new AtomicReference<>();
    private volatile Disposable disposable;

    /** 创建消息流：等待 subscribe 完成后注册 {@link BaseRedisPubSubListener} 并推送消息。 */
    @Override
    public Flux<Message<ByteBuffer, ByteBuffer>> receive() {
        if (flux.get() != null) {
            return flux.get();
        }

        Flux<Message<ByteBuffer, ByteBuffer>> f = Flux.create(emitter -> {
            emitter.onRequest(n -> {

                monosListener.addListener(() -> {
                    BaseRedisPubSubListener listener = new BaseRedisPubSubListener() {
                        @Override
                        public void onPatternMessage(CharSequence pattern, CharSequence channel, Object message) {
                            // 忽略未订阅 pattern 的回调。
                            if (!patterns.containsKey(new ChannelName(pattern.toString()))) {
                                return;
                            }

                            emitter.next(new PatternMessage<>(ByteBuffer.wrap(pattern.toString().getBytes()),
                                                                ByteBuffer.wrap(channel.toString().getBytes()),
                                                                ByteBuffer.wrap((byte[])message)));
                        }

                        @Override
                        public void onMessage(CharSequence channel, Object msg) {
                            // 忽略未订阅频道的回调。
                            if (!channels.containsKey(new ChannelName(channel.toString()))) {
                                return;
                            }

                            emitter.next(new ChannelMessage<>(ByteBuffer.wrap(channel.toString().getBytes()), ByteBuffer.wrap((byte[])msg)));
                        }
                    };

                    disposable = () -> {
                        for (Entry<ChannelName, Collection<PubSubConnectionEntry>> entry : channels.entrySet()) {
                            for (PubSubConnectionEntry pubSubConnectionEntry : entry.getValue()) {
                                pubSubConnectionEntry.removeListener(entry.getKey(), listener);
                            }
                        }
                        for (Entry<ChannelName, Collection<PubSubConnectionEntry>> entry : patterns.entrySet()) {
                            for (PubSubConnectionEntry pubSubConnectionEntry : entry.getValue()) {
                                pubSubConnectionEntry.removeListener(entry.getKey(), listener);
                            }
                        }
                    };

                    for (Entry<ChannelName, Collection<PubSubConnectionEntry>> entry : channels.entrySet()) {
                        for (PubSubConnectionEntry pubSubConnectionEntry : entry.getValue()) {
                            pubSubConnectionEntry.addListener(entry.getKey(), listener);
                        }
                    }
                    for (Entry<ChannelName, Collection<PubSubConnectionEntry>> entry : patterns.entrySet()) {
                            for (PubSubConnectionEntry pubSubConnectionEntry : entry.getValue()) {
                                pubSubConnectionEntry.addListener(entry.getKey(), listener);
                            }
                    }

                    emitter.onDispose(disposable);
                });
            });
        });
        
        if (flux.compareAndSet(null, f)) {
            return f;
        }
        return flux.get();
    }

    /** 取消全部订阅并释放消息监听器。 */
    @Override
    public Mono<Void> cancel() {
        return unsubscribe().then(pUnsubscribe()).then(Mono.fromRunnable(() -> {
            if (disposable != null) {
                disposable.dispose();
            }
        }));
    }
}
