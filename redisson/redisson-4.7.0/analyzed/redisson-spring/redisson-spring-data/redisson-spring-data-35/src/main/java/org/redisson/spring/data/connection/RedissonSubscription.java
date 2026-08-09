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
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.pubsub.PubSubType;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.ReclosableLatch;
import org.redisson.pubsub.PubSubConnectionEntry;
import org.redisson.pubsub.PublishSubscribeService;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.SubscriptionListener;
import org.springframework.data.redis.connection.util.AbstractSubscription;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Spring Data Redis Pub/Sub {@link AbstractSubscription} 的 Redisson 实现。
 * <p>通过 {@link PublishSubscribeService} 管理频道/模式订阅，
将 Redisson 消息转为 {@link DefaultMessage} 回调 {@link MessageListener}；
若监听器为 {@link SubscriptionListener}，则同步订阅/取消订阅状态事件。
 * <p>spring-data-32 起以 {@link CompletableFuture} 追踪各频道/模式订阅状态，
并对 {@code SynchronizingMessageListener} 使用 {@link ReclosableLatch} 等待首次取消订阅。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonSubscription extends AbstractSubscription {

    /** 已完成的占位 Future，用于已订阅频道的回调合并。 */
    private static final CompletableFuture<Void> COMPLETED = new CompletableFuture<>();

    /** 可重入闭锁：全部频道与模式取消订阅后 open，供 SynchronizingMessageListener 同步。 */
    private final ReclosableLatch latch = new ReclosableLatch();

    /** 频道名 → 订阅完成 Future，供重复订阅与回调去重。 */
    private final Map<ChannelName, CompletableFuture<Void>> subscribed = new ConcurrentHashMap<>();
    /** 模式名 → PSUBSCRIBE 完成 Future。 */
    private final Map<ChannelName, CompletableFuture<Void>> psubscribed = new ConcurrentHashMap<>();

    /** 异步 Redis 命令执行器。 */
    private final CommandAsyncExecutor commandExecutor;
    /** Redisson Pub/Sub 订阅服务。 */
    private final PublishSubscribeService subscribeService;
    
    /** 绑定异步命令执行器与 Spring 消息监听器。 */
    public RedissonSubscription(CommandAsyncExecutor commandExecutor, MessageListener listener) {
        super(listener, null, null);
        this.commandExecutor = commandExecutor;
        this.subscribeService = commandExecutor.getConnectionManager().getSubscribeService();
    }

    /** SUBSCRIBE：仅订阅尚未注册的频道，并可选等待 SynchronizingMessageListener 同步点。 */
    @Override
    protected void doSubscribe(byte[]... channels) {
        // 是否已有活跃订阅（决定是否需要 latch 等待）。
        boolean hasSubscriptionsBefore = !(subscribed.isEmpty() & psubscribed.isEmpty());

        // 过滤已订阅频道，收集本次需新订阅的条目。
        Map<ChannelName, CompletableFuture<Void>> tosubscribe = getNonSubscribed(channels, subscribed, (l, ch) -> {
            ((SubscriptionListener) getListener()).onChannelSubscribed(ch, 1);
        });
        if (tosubscribe.isEmpty()) {
            return;
        }

        List<CompletableFuture<?>> list = new ArrayList<>();
        for (ChannelName channel : tosubscribe.keySet()) {
            CompletableFuture<List<PubSubConnectionEntry>> f = subscribeService.subscribe(ByteArrayCodec.INSTANCE, channel, new BaseRedisPubSubListener() {
                @Override
                public void onMessage(CharSequence ch, Object message) {
                    // 忽略非目标频道的回调（连接复用时可能收到其他频道消息）。
                    if (!Arrays.equals(((ChannelName) ch).getName(), channel.getName())) {
                        return;
                    }

                    byte[] m = toBytes(message);
                    DefaultMessage msg = new DefaultMessage(((ChannelName) ch).getName(), m);
                    getListener().onMessage(msg, null);
                }

                @Override
                public void onStatus(PubSubType type, CharSequence ch) {
                    if (!Arrays.equals(((ChannelName) ch).getName(), channel.getName())) {
                        return;
                    }

                    // SUBSCRIBE 确认：完成对应频道的 CompletableFuture。
                    if (getListener() instanceof SubscriptionListener
                            && type == PubSubType.SUBSCRIBE) {
                        CompletableFuture<Void> callback = subscribed.getOrDefault(channel, COMPLETED);
                        callback.complete(null);
                    }
                    super.onStatus(type, ch);

                    // 频道取消后，若频道与模式均已清空则 open latch。
                    if (type == PubSubType.UNSUBSCRIBE) {
                        subscribed.remove(channel);
                        if (subscribed.isEmpty() && psubscribed.isEmpty()) {
                            latch.open();
                        }
                    }
                }

            });
            list.add(f);
        }
        for (CompletableFuture<?> future : list) {
            commandExecutor.get(future);
        }
        if (getListener() instanceof SubscriptionListener) {
            for (ChannelName channel : tosubscribe.keySet()) {
                ((SubscriptionListener) getListener()).onChannelSubscribed(channel.getName(), 1);
            }
        }

        if (hasSubscriptionsBefore) {
            return;
        }

        // RedisMessageListenerContainer 同步修复：等待首次 UNSUBSCRIBE。
        // fix for RedisMessageListenerContainer
        if (getListener().getClass().getName().equals("org.springframework.data.redis.listener.SynchronizingMessageListener")) {
            try {
                // 等待同步点后关闭 latch 以便下次复用。
                latch.await();
                latch.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** 返回尚未订阅的频道/模式；已订阅则异步回调 {@link SubscriptionListener}。 */
    private Map<ChannelName, CompletableFuture<Void>> getNonSubscribed(byte[][] channels,
                                                                       Map<ChannelName, CompletableFuture<Void>> subscribed,
                                                                       BiConsumer<SubscriptionListener, byte[]> consumer) {
        Map<ChannelName, CompletableFuture<Void>> tosubscribe = new HashMap<>();
        for (byte[] ch : channels) {
            CompletableFuture<Void> f = new CompletableFuture<>();
            ChannelName n = new ChannelName(ch);
            // putIfAbsent 成功表示新订阅，否则合并到已有 Future。
            CompletableFuture<Void> cf = subscribed.putIfAbsent(n, f);
            if (cf == null) {
                tosubscribe.put(n, f);
            } else {
                if (getListener() instanceof SubscriptionListener) {
                    if (cf.isDone()) {
                        commandExecutor.getServiceManager().getExecutor().submit(() -> {
                            consumer.accept((SubscriptionListener) getListener(), ch);
                        });
                    } else {
                        cf.thenAccept(r -> {
                            consumer.accept((SubscriptionListener) getListener(), ch);
                        });
                    }
                }
            }
        }

        return tosubscribe;
    }

    /** UNSUBSCRIBE：取消指定频道；{@link SubscriptionListener} 时回调 onChannelUnsubscribed。 */
    @Override
    protected void doUnsubscribe(boolean all, byte[]... channels) {
        for (byte[] channel : channels) {
            CompletableFuture<Codec> f = subscribeService.unsubscribe(new ChannelName(channel), PubSubType.UNSUBSCRIBE);
            if (getListener() instanceof SubscriptionListener) {
                f.whenComplete((r, e) -> {
                    if (r != null) {
                        ((SubscriptionListener) getListener()).onChannelUnsubscribed(channel, 1);
                    }
                });
            }
        }
    }

    /** PSUBSCRIBE：按模式订阅，逻辑同 {@link #doSubscribe}。 */
    @Override
    protected void doPsubscribe(byte[]... patterns) {
        boolean hasSubscriptionsBefore = !(subscribed.isEmpty() & psubscribed.isEmpty());

        Map<ChannelName, CompletableFuture<Void>> tosubscribe = getNonSubscribed(patterns, psubscribed, (l, ch) -> {
            ((SubscriptionListener) getListener()).onPatternSubscribed(ch, 1);
        });

        if (tosubscribe.isEmpty()) {
            return;
        }

        List<CompletableFuture<?>> list = new ArrayList<>();
        for (ChannelName channel : tosubscribe.keySet()) {
            CompletableFuture<Collection<PubSubConnectionEntry>> f = subscribeService.psubscribe(channel, ByteArrayCodec.INSTANCE, new BaseRedisPubSubListener() {
                @Override
                public void onPatternMessage(CharSequence pattern, CharSequence ch, Object message) {
                    // 忽略非目标 pattern 的回调。
                    if (!Arrays.equals(((ChannelName) pattern).getName(), channel.getName())) {
                        return;
                    }

                    byte[] m = toBytes(message);
                    DefaultMessage msg = new DefaultMessage(((ChannelName)ch).getName(), m);
                    getListener().onMessage(msg, ((ChannelName)pattern).getName());
                }

                @Override
                public void onStatus(PubSubType type, CharSequence pattern) {
                    if (!Arrays.equals(((ChannelName) pattern).getName(), channel.getName())) {
                        return;
                    }

                    // PSUBSCRIBE 确认：完成对应模式的 CompletableFuture。
                    if (getListener() instanceof SubscriptionListener
                            && type == PubSubType.PSUBSCRIBE) {
                        CompletableFuture<Void> callback = psubscribed.getOrDefault(channel, COMPLETED);
                        callback.complete(null);
                    }
                    super.onStatus(type, pattern);
                    // 模式取消后，若频道与模式均已清空则 open latch。
                    if (type == PubSubType.PUNSUBSCRIBE) {
                        psubscribed.remove(channel);
                        if (subscribed.isEmpty() && psubscribed.isEmpty()) {
                            latch.open();
                        }
                    }
                }
            });
            list.add(f);
        }
        for (CompletableFuture<?> future : list) {
            commandExecutor.get(future);
        }
        if (getListener() instanceof SubscriptionListener) {
            for (ChannelName channel : tosubscribe.keySet()) {
                ((SubscriptionListener) getListener()).onPatternSubscribed(channel.getName(), 1);
            }
        }

        if (hasSubscriptionsBefore) {
            return;
        }

        // fix for RedisMessageListenerContainer
        if (getListener().getClass().getName().equals("org.springframework.data.redis.listener.SynchronizingMessageListener")) {
            try {
                latch.await();
                latch.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** 将 String 或 byte[] 载荷统一为字节数组。 */
    private byte[] toBytes(Object message) {
        if (message instanceof String) {
            return  ((String) message).getBytes();
        }
        return (byte[]) message;
    }

    /** PUNSUBSCRIBE：取消指定模式；{@link SubscriptionListener} 时回调 onPatternUnsubscribed。 */
    @Override
    protected void doPUnsubscribe(boolean all, byte[]... patterns) {
        for (byte[] pattern : patterns) {
            CompletableFuture<Codec> f = subscribeService.unsubscribe(new ChannelName(pattern), PubSubType.PUNSUBSCRIBE);
            if (getListener() instanceof SubscriptionListener) {
                f.whenComplete((r, e) -> {
                    if (r != null) {
                        ((SubscriptionListener) getListener()).onPatternUnsubscribed(pattern, 1);
                    }
                });
            }
        }
    }

    /** 关闭时取消所有频道与模式订阅。 */
    @Override
    protected void doClose() {
        doUnsubscribe(false, getChannels().toArray(new byte[getChannels().size()][]));
        doPUnsubscribe(false, getPatterns().toArray(new byte[getPatterns().size()][]));
    }

}
