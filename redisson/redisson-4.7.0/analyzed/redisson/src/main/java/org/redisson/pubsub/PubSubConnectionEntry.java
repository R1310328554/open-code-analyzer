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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.redisson.PubSubMessageListener;
import org.redisson.PubSubPatternMessageListener;
import org.redisson.client.*;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.pubsub.PubSubStatusMessage;
import org.redisson.client.protocol.pubsub.PubSubType;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.connection.ServiceManager;
import org.redisson.misc.AsyncSemaphore;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单条 Redis Pub/Sub 连接的订阅状态：维护频道→监听器队列、
 * 剩余可订阅频道配额，以及 SUBSCRIBE/UNSUBSCRIBE 的异步编排。
 * 由 {@link PublishSubscribeService} 创建并复用空闲连接。
 *
 * @author Nikita Koksharov
 *
 */
public class PubSubConnectionEntry {

    /** 本连接剩余可 SUBSCRIBE 的频道槽位（初始为 subscriptionsPerConnection）。 */
    private final AtomicInteger subscribedChannelsAmount;
    /** 底层 Netty Pub/Sub 连接。 */
    private final RedisPubSubConnection conn;

    /** 等待 SUBSCRIBE 成功 ACK 的 per-channel 监听器。 */
    private final Map<ChannelName, SubscribeListener> subscribeChannelListeners = new ConcurrentHashMap<>();
    /** 频道 → 业务 RedisPubSubListener 队列。 */
    private final Map<ChannelName, Queue<RedisPubSubListener<?>>> channelListeners = new ConcurrentHashMap<>();

    /** getListeners 无订阅时返回的空队列占位。 */
    private static final Queue<RedisPubSubListener<?>> EMPTY_QUEUE = new ArrayDeque<>(0);

    private final ServiceManager serviceManager;
    private final PublishSubscribeService subscribeService;
    private final MasterSlaveEntry entry;

    /** SUBSCRIBE/SSUBSCRIBE/PSUBSCRIBE 到对应 UNSUBSCRIBE 命令映射。 */
    private static final Map<PubSubType, PubSubType> SUBSCRIBE2UNSUBSCRIBE = new HashMap<>();

    static {
        SUBSCRIBE2UNSUBSCRIBE.put(PubSubType.SUBSCRIBE, PubSubType.UNSUBSCRIBE);
        SUBSCRIBE2UNSUBSCRIBE.put(PubSubType.SSUBSCRIBE, PubSubType.SUNSUBSCRIBE);
        SUBSCRIBE2UNSUBSCRIBE.put(PubSubType.PSUBSCRIBE, PubSubType.PUNSUBSCRIBE);
    }

    /** 绑定连接、主从条目，并初始化频道配额计数器。 */
    public PubSubConnectionEntry(RedisPubSubConnection conn, ConnectionManager connectionManager, MasterSlaveEntry entry) {
        super();
        this.conn = conn;
        this.entry = entry;
        this.serviceManager = connectionManager.getServiceManager();
        this.subscribeService = connectionManager.getSubscribeService();
        this.subscribedChannelsAmount = new AtomicInteger(serviceManager.getConfig().getSubscriptionsPerConnection());
    }

    public MasterSlaveEntry getEntry() {
        return entry;
    }

    /** 返回指定频道上注册的监听器数量。 */
    public int countListeners(ChannelName channelName) {
        return channelListeners.getOrDefault(channelName, EMPTY_QUEUE).size();
    }

    public boolean hasListeners(ChannelName channelName) {
        return channelListeners.containsKey(channelName);
    }

    public Queue<RedisPubSubListener<?>> getListeners(ChannelName channelName) {
        return channelListeners.getOrDefault(channelName, EMPTY_QUEUE);
    }

    /** 向频道追加监听器并同步注册到底层 connection。 */
    public void addListener(ChannelName channelName, RedisPubSubListener<?> listener) {
        if (listener == null) {
            return;
        }

        channelListeners.compute(channelName, (k, queue) -> {
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
            }

            queue.add(listener);
            conn.addListener(channelName, listener);
            return queue;
        });
    }

    /** 按用户 EventListener 引用查找并移除包装监听器。 */
    public boolean removeListener(ChannelName channelName, EventListener msgListener) {
        Queue<RedisPubSubListener<?>> listeners = channelListeners.get(channelName);
        for (RedisPubSubListener<?> listener : listeners) {
            if (listener instanceof PubSubMessageListener) {
                if (((PubSubMessageListener<?>) listener).getListener() == msgListener) {
                    removeListener(channelName, listener);
                    return true;
                }
            }
            if (listener instanceof PubSubPatternMessageListener) {
                if (((PubSubPatternMessageListener<?>) listener).getListener() == msgListener) {
                    removeListener(channelName, listener);
                    return true;
                }
            }
        }
        return false;
    }

    /** 按 identityHashCode 移除监听器。 */
    public boolean removeListener(ChannelName channelName, int listenerId) {
        Queue<RedisPubSubListener<?>> listeners = channelListeners.getOrDefault(channelName, EMPTY_QUEUE);
        for (RedisPubSubListener<?> listener : listeners) {
            if (listener instanceof PubSubMessageListener) {
                if (hasId(((PubSubMessageListener<?>) listener).getListener(), listenerId)) {
                    removeListener(channelName, listener);
                    return true;
                }
            }
            if (listener instanceof PubSubPatternMessageListener) {
                if (hasId(((PubSubPatternMessageListener<?>) listener).getListener(), listenerId)) {
                    removeListener(channelName, listener);
                    return true;
                }
            }
            if (hasId(listener, listenerId)) {
                removeListener(channelName, listener);
                return true;
            }
        }
        return false;
    }

    /** 比较监听器 identityHashCode。 */
    private boolean hasId(EventListener listener, int listenerId) {
        return System.identityHashCode(listener) == listenerId;
    }

    /** 从 map 与 connection 双向移除监听器；队列为空时删除频道键。 */
    public void removeListener(ChannelName channelName, RedisPubSubListener<?> listener) {
        channelListeners.computeIfPresent(channelName, (k, queue) -> {
            if (queue.remove(listener) && queue.isEmpty()) {
                return null;
            }
            return queue;
        });

        conn.removeListener(channelName, listener);
    }

    /** CAS 占用一个订阅槽位；返回剩余数，-1 表示已满。 */
    public int tryAcquire() {
        while (true) {
            int value = subscribedChannelsAmount.get();
            if (value == 0) {
                return -1;
            }

            if (subscribedChannelsAmount.compareAndSet(value, value - 1)) {
                return value - 1;
            }
        }
    }

    /** 释放一个订阅槽位（UNSUBSCRIBE 后）。 */
    public int release() {
        return subscribedChannelsAmount.incrementAndGet();
    }

    /** 是否尚未占用任何频道槽位（可整连接复用）。 */
    public boolean isFree() {
        return subscribedChannelsAmount.get() == serviceManager.getConfig().getSubscriptionsPerConnection();
    }

    /**
     * 批量 SUBSCRIBE：先 addListeners 等待 ACK，再发 Redis 命令；
     * 失败时自动 UNSUBSCRIBE 已订阅频道并 completeExceptionally。
     */
    public void subscribe(Codec codec, List<ChannelName> channelNames, CompletableFuture<PubSubConnectionEntry> pm,
                          PubSubType type, AsyncSemaphore lock, RedisPubSubListener<?>[] listeners) {
        CompletableFuture<PubSubConnectionEntry> pp = new CompletableFuture<>();
        pp.whenComplete((r, e) -> {
            if (e != null) {
                // 订阅失败：回滚已 SUBSCRIBE 的频道
                PubSubType unsubscribeType = SUBSCRIBE2UNSUBSCRIBE.get(type);

                List<CompletableFuture<?>> futures = new ArrayList<>(channelNames.size());
                for (ChannelName channelName : channelNames) {
                    CompletableFuture<?> f = subscribeService.unsubscribe(channelName, this, unsubscribeType);
                    futures.add(f);
                }
                CompletableFuture<Void> ff = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                ff.whenComplete((rr, ee) -> {
                    pm.completeExceptionally(e);
                });
                return;
            }

            pm.complete(r);
        });

        CompletableFuture<Void> subscribeFuture = addListeners(channelNames, pp, type, lock, listeners);
        CompletableFuture<Void> promise = new CompletableFuture<>();
        promise.whenComplete((r, ex) -> {
            if (ex != null) {
                subscribeFuture.completeExceptionally(ex);
            }
        });

        ChannelFuture future;
        if (PubSubType.SUBSCRIBE == type) {
            future = conn.subscribe(promise, codec, channelNames.toArray(new ChannelName[0]));
        } else if (PubSubType.SSUBSCRIBE == type) {
            future = conn.ssubscribe(promise, codec, channelNames.toArray(new ChannelName[0]));
        } else {
            future = conn.psubscribe(promise, codec, channelNames.toArray(new ChannelName[0]));
        }
        future.addListener((ChannelFutureListener) future1 -> {
            if (!future1.isSuccess()) {
                subscribeFuture.completeExceptionally(future1.cause());
                return;
            }

            serviceManager.newTimeout(t -> {
                subscribeFuture.completeExceptionally(new RedisTimeoutException(
                        "Subscription response timeout after " + serviceManager.getConfig().getTimeout() + "ms. " +
                                "Check network and/or increase 'timeout' parameter."));
            }, serviceManager.getConfig().getTimeout(), TimeUnit.MILLISECONDS);
        });
    }

    /** 获取或创建等待 SUBSCRIBE 状态 ACK 的 SubscribeListener。 */
    private SubscribeListener getSubscribeFuture(ChannelName channel, PubSubType type) {
        return subscribeChannelListeners.computeIfAbsent(channel, k -> {
            SubscribeListener listener = new SubscribeListener(channel, type);
            conn.addListener(channel, listener);
            return listener;
        });
    }

    /** 发送 UNSUBSCRIBE 并在 ACK 后清理本 entry 上该频道全部监听器。 */
    public void unsubscribe(PubSubType commandType, ChannelName channel, RedisPubSubListener<?> listener) {
        AtomicBoolean executed = new AtomicBoolean();
        conn.addListener(channel, new BaseRedisPubSubListener() {
            @Override
            public void onStatus(PubSubType type, CharSequence ch) {
                if (type == commandType && channel.equals(ch)) {
                    executed.set(true);

                    conn.removeListener(channel, this);
                    removeListeners(channel);
                    if (listener != null) {
                        listener.onStatus(type, ch);
                    }
                }
            }
        });

        ChannelFuture future = conn.unsubscribe(commandType, channel);
        future.addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {
                return;
            }

            // 超时未收到 ACK 时模拟 status 消息推进状态机
            serviceManager.newTimeout(timeout -> {
                if (executed.get()) {
                    return;
                }
                conn.onMessage(new PubSubStatusMessage(commandType, channel));
            }, serviceManager.getConfig().getTimeout(), TimeUnit.MILLISECONDS);
        });
    }

    /** UNSUBSCRIBE 成功后移除频道全部 listener 与 SubscribeListener。 */
    private void removeListeners(ChannelName channel) {
        conn.removeDisconnectListener(channel);

        SubscribeListener s = subscribeChannelListeners.remove(channel);
        conn.removeListener(channel, s);

        Queue<RedisPubSubListener<?>> queue = channelListeners.remove(channel);
        if (queue == null) {
            return;
        }
        for (RedisPubSubListener<?> listener : queue) {
            conn.removeListener(channel, listener);
        }
    }

    public RedisPubSubConnection getConnection() {
        return conn;
    }

    @Override
    public String toString() {
        return "PubSubConnectionEntry [subscribedChannelsAmount=" + subscribedChannelsAmount + ", conn=" + conn + "]";
    }

    /**
     * 批量注册监听器并等待全部 SUBSCRIBE ACK；
     * 若 promise 已被其他线程完成则回滚并 UNSUBSCRIBE 空频道。
     */
    public CompletableFuture<Void> addListeners(List<ChannelName> channelNames,
                                                CompletableFuture<PubSubConnectionEntry> promise,
                                                PubSubType type, AsyncSemaphore lock,
                                                RedisPubSubListener<?>... listeners) {
        List<CompletableFuture<Void>> futures = new ArrayList<>(channelNames.size());
        for (ChannelName channelName : channelNames) {
            for (RedisPubSubListener<?> listener : listeners) {
                addListener(channelName, listener);
            }

            SubscribeListener list = getSubscribeFuture(channelName, type);
            CompletableFuture<Void> subscribeFuture = list.getSuccessFuture();
            futures.add(subscribeFuture);
        }

        CompletableFuture<Void> subscribeFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        subscribeFuture.whenComplete((res, e) -> {
            if (e != null) {
                promise.completeExceptionally(e);
                lock.release();
                return;
            }

            // 竞态：其他订阅先完成，需撤销本次 listener
            if (!promise.complete(this)) {
                List<CompletableFuture<Void>> ffs = new ArrayList<>();
                for (ChannelName channelName : channelNames) {
                    for (RedisPubSubListener<?> listener : listeners) {
                        removeListener(channelName, listener);
                    }
                    if (!hasListeners(channelName)) {
                        CompletableFuture<Void> f = subscribeService.unsubscribeLocked(type, channelName, this);
                        ffs.add(f);
                    }
                }

                CompletableFuture<Void> ff = CompletableFuture.allOf(ffs.toArray(new CompletableFuture[0]));
                ff.thenAccept(r -> {
                    lock.release();
                });
            } else {
                lock.release();
            }
        });
        return subscribeFuture;
    }

    public CompletableFuture<Void> addListeners(ChannelName channelName,
                                                PubSubType type,
                                                RedisPubSubListener<?>... listeners) {
        for (RedisPubSubListener<?> listener : listeners) {
            addListener(channelName, listener);
        }
        SubscribeListener list = getSubscribeFuture(channelName, type);
        return list.getSuccessFuture();
    }

    /** 移除监听器；频道无 listener 时触发 UNSUBSCRIBE。 */
    public CompletableFuture<Void> release(PubSubType type, ChannelName channelName, RedisPubSubListener<?>... listeners) {
        List<CompletableFuture<Void>> ffs = new ArrayList<>();
        for (RedisPubSubListener<?> listener : listeners) {
            removeListener(channelName, listener);
        }
        if (!hasListeners(channelName)) {
            CompletableFuture<Void> f = subscribeService.unsubscribeLocked(type, channelName, this);
            ffs.add(f);
        }

        return CompletableFuture.allOf(ffs.toArray(new CompletableFuture[0]));
    }

}
