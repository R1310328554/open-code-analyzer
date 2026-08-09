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

import io.netty.util.Timeout;
import org.redisson.PubSubPatternStatusListener;
import org.redisson.PubSubStatusListener;
import org.redisson.api.RFuture;
import org.redisson.api.listener.FlushListener;
import org.redisson.api.listener.TrackingListener;
import org.redisson.client.*;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.pubsub.PubSubType;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.config.ReadMode;
import org.redisson.config.ShardedSubscriptionMode;
import org.redisson.connection.ClientConnectionsEntry;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.misc.AsyncSemaphore;
import org.redisson.misc.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Redisson 全局 Pub/Sub 调度中心：管理订阅连接池、per-channel 异步锁、
 * 锁/信号量/CountDownLatch 专用 {@link PublishSubscribe} 子系统，
 * 以及 CLIENT TRACKING 与 keyspace 多节点订阅。
 * <p>
 * 核心索引：{@link #name2entry}（频道→连接集合）、
 * {@link #name2PubSubConnection}（PubSubKey→主连接）、
 * {@link #entry2PubSubConnection}（主从条目→空闲连接队列）。
 *
 * @author Nikita Koksharov
 *
 */
public class PublishSubscribeService {

    /** 频道名 + 主从条目的复合键，用于集群内定位订阅连接。 */
    public static class PubSubKey {

        /** Redis 频道（或模式）名。 */
        private final ChannelName channelName;
        /** 所属 MasterSlave 拓扑条目。 */
        private final MasterSlaveEntry entry;

        public PubSubKey(ChannelName channelName, MasterSlaveEntry entry) {
            this.channelName = channelName;
            this.entry = entry;
        }

        public ChannelName getChannelName() {
            return channelName;
        }

        public MasterSlaveEntry getEntry() {
            return entry;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PubSubKey key = (PubSubKey) o;
            return Objects.equals(channelName, key.channelName) && Objects.equals(entry, key.entry);
        }

        @Override
        public int hashCode() {
            return Objects.hash(channelName, entry);
        }

        @Override
        public String toString() {
            return "PubSubKey{" +
                    "channelName=" + channelName +
                    ", entry=" + entry +
                    '}';
        }
    }

    /** 某 MasterSlave 条目下可复用的空闲 PubSub 连接队列。 */
    public static class PubSubEntry {

        /** 尚有剩余订阅槽位的连接，按 FIFO 复用。 */
        Queue<PubSubConnectionEntry> entries = new ConcurrentLinkedQueue<>();

        public Queue<PubSubConnectionEntry> getEntries() {
            return entries;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(PublishSubscribeService.class);

    /** 连接与槽位路由。 */
    private final ConnectionManager connectionManager;

    /** 主从/集群配置（订阅超时、重试等）。 */
    private final MasterSlaveServersConfig config;

    /** 按 channelName hash 分片的订阅变更锁（50 路）。 */
    private final AsyncSemaphore[] locks = new AsyncSemaphore[50];

    /** 保护空闲 PubSub 连接队列的全局锁。 */
    private final AsyncSemaphore freePubSubLock = new AsyncSemaphore(1);

    /** 频道 → 持有该频道订阅的连接集合。 */
    private final Map<ChannelName, Collection<PubSubConnectionEntry>> name2entry = new ConcurrentHashMap<>();
    /** (频道, 主从条目) → 主 PubSub 连接条目。 */
    private final ConcurrentMap<PubSubKey, PubSubConnectionEntry> name2PubSubConnection = new ConcurrentHashMap<>();
    /** 主从条目 → 空闲可复用连接池。 */
    private final ConcurrentMap<MasterSlaveEntry, PubSubEntry> entry2PubSubConnection = new ConcurrentHashMap<>();
    /** (频道, 客户端连接池条目) → 绑定连接（CLIENT TRACKING 等）。 */
    private final Map<Tuple<ChannelName, ClientConnectionsEntry>, PubSubConnectionEntry> key2connection = new ConcurrentHashMap<>();

    /** 分布式信号量 Pub/Sub 门面。 */
    private final SemaphorePubSub semaphorePubSub = new SemaphorePubSub(this);

    /** CountDownLatch Pub/Sub 门面。 */
    private final CountDownLatchPubSub countDownLatchPubSub = new CountDownLatchPubSub(this);

    /** 分布式锁 Pub/Sub 门面。 */
    private final LockPubSub lockPubSub = new LockPubSub(this);

    /** 已开启 CLIENT TRACKING 的 PubSub 连接集合。 */
    private final Set<PubSubConnectionEntry> trackedEntries = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** Redis 7+ 分片订阅（SSUBSCRIBE）是否可用。 */
    private boolean shardingSupported = false;
    /** PSUBSCRIBE 模式订阅是否可用。 */
    private boolean patternSupported = true;

    /** 初始化 50 路 channel 锁与配置引用。 */
    public PublishSubscribeService(ConnectionManager connectionManager) {
        super();
        this.connectionManager = connectionManager;
        this.config = connectionManager.getServiceManager().getConfig();
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new AsyncSemaphore(1);
        }
    }

    /** @return 锁专用 Pub/Sub */
    public LockPubSub getLockPubSub() {
        return lockPubSub;
    }

    /** @return CountDownLatch Pub/Sub */
    public CountDownLatchPubSub getCountDownLatchPubSub() {
        return countDownLatchPubSub;
    }

    /** @return 信号量 Pub/Sub */
    public SemaphorePubSub getSemaphorePubSub() {
        return semaphorePubSub;
    }

    /** 统计多个频道上的监听器总数（取每频道首个连接）。 */
    public int countListeners(List<ChannelName> channelNames) {
        int result = 0;
        for (ChannelName channelName : channelNames) {
            Collection<PubSubConnectionEntry> entries = name2entry.getOrDefault(channelName, Collections.emptySet());
            Iterator<PubSubConnectionEntry> it = entries.iterator();
            if (it.hasNext()) {
                result += it.next().countListeners(channelName);
            }
        }
        return result;
    }

    public boolean hasEntry(ChannelName channelName) {
        return name2entry.containsKey(channelName);
    }

    /**
     * 模式订阅 PSUBSCRIBE；keyspace 多实体模式下向所有 MasterSlave 条目各订一次，
     * 并通过 statusCounter 合并 onStatus 回调。
     */
    public CompletableFuture<Collection<PubSubConnectionEntry>> psubscribe(ChannelName channelName, Codec codec, RedisPubSubListener<?>... listeners) {
        if (isMultiEntity(channelName)) {
            Collection<MasterSlaveEntry> entrySet = connectionManager.getEntrySet();

            AtomicInteger statusCounter = new AtomicInteger(entrySet.size());
            RedisPubSubListener[] ls = Arrays.stream(listeners).map(l -> {
                if (l instanceof PubSubPatternStatusListener) {
                    return new PubSubPatternStatusListener((PubSubPatternStatusListener) l) {
                        @Override
                        public void onStatus(PubSubType type, CharSequence channel) {
                            if (statusCounter.get() == 0 || statusCounter.decrementAndGet() == 0) {
                                super.onStatus(type, channel);
                            }
                        }
                    };
                }
                return l;
            }).toArray(RedisPubSubListener[]::new);

            List<CompletableFuture<PubSubConnectionEntry>> futures = new ArrayList<>();
            for (MasterSlaveEntry entry : entrySet) {
                CompletableFuture<PubSubConnectionEntry> future =
                        subscribe(PubSubType.PSUBSCRIBE, codec, ChannelName.newList(channelName), entry, entry.getEntry(), ls);
                futures.add(future);
            }
            CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            return future.thenApply(r -> {
                return futures.stream().map(v -> v.getNow(null)).collect(Collectors.toList());
            });
        }

        MasterSlaveEntry entry = getEntry(channelName);
        if (entry == null) {
            int slot = connectionManager.calcSlot(channelName.getName());
            return connectionManager.getServiceManager().createNodeNotFoundFuture(channelName.toString(), slot);
        }

        CompletableFuture<PubSubConnectionEntry> f = subscribe(PubSubType.PSUBSCRIBE, codec, ChannelName.newList(channelName), entry, null, listeners);
        return f.thenApply(res -> new ArrayList<>(Collections.singletonList(res)));
    }

    /** 非单节点配置且为 keyspace 频道时需多节点订阅。 */
    public boolean isMultiEntity(ChannelName channelName) {
        return !connectionManager.getServiceManager().getCfg().isSingleConfig() && channelName.isKeyspace();
    }

    /** FlushListener identityHashCode → 各节点 entryListener id 集合。 */
    private final Map<Integer, Collection<Integer>> flushListeners = new ConcurrentHashMap<>();

    /** 订阅 __redis__:invalidate 频道以接收 CLIENT TRACKING 刷盘通知。 */
    public CompletableFuture<Integer> subscribe(CommandAsyncExecutor commandExecutor, FlushListener listener) {
        int listenerId = System.identityHashCode(listener);

        List<CompletableFuture<PubSubConnectionEntry>> ffs = new ArrayList<>();
        for (MasterSlaveEntry entry : connectionManager.getEntrySet()) {
            RedisPubSubListener<Object> entryListener = (channel, msg) -> {
                if (msg == null
                        && channel.equals(ChannelName.TRACKING.toString())) {
                    listener.onFlush(entry.getClient().getAddr());
                }
            };
            int entryListenerId = System.identityHashCode(entryListener);

            Collection<Integer> listeners = flushListeners.computeIfAbsent(listenerId, k -> new HashSet<>());
            listeners.add(entryListenerId);

            CompletableFuture<PubSubConnectionEntry> future = subscribe(PubSubType.SUBSCRIBE, StringCodec.INSTANCE,
                    ChannelName.newList(ChannelName.TRACKING), entry, entry.getEntry(), entryListener);
            ffs.add(future);
        }

        return registerClientTrackingListener(commandExecutor, ffs, listenerId, null);
    }

    /** 订阅 tracking 频道接收键变更字符串通知。 */
    public CompletableFuture<Integer> subscribe(CommandAsyncExecutor commandExecutor, TrackingListener listener) {
        int listenerId = System.identityHashCode(listener);

        List<CompletableFuture<PubSubConnectionEntry>> ffs = new ArrayList<>();
        for (MasterSlaveEntry entry : connectionManager.getEntrySet()) {
            RedisPubSubListener<Object> entryListener = (channel, msg) -> {
                if (msg != null
                        && channel.equals(ChannelName.TRACKING.toString())) {
                    listener.onChange((String) msg);
                }
            };
            int entryListenerId = System.identityHashCode(entryListener);

            Collection<Integer> listeners = flushListeners.computeIfAbsent(listenerId, k -> new HashSet<>());
            listeners.add(entryListenerId);

            CompletableFuture<PubSubConnectionEntry> future = subscribe(PubSubType.SUBSCRIBE, StringCodec.INSTANCE,
                    ChannelName.newList(ChannelName.TRACKING), entry, entry.getEntry(), entryListener);
            ffs.add(future);
        }

        return registerClientTrackingListener(commandExecutor, ffs, listenerId, null);
    }

    /**
     * 全部节点 SUBSCRIBE 成功后，对未 tracking 的连接执行 CLIENT TRACKING ON REDIRECT。
     */
    private CompletableFuture<Integer> registerClientTrackingListener(CommandAsyncExecutor commandExecutor,
                                                                      List<CompletableFuture<PubSubConnectionEntry>> ffs,
                                                                      int listenerId,
                                                                      String key) {
        CompletableFuture<Void> future = CompletableFuture.allOf(ffs.toArray(new CompletableFuture[0]));
        return future.thenCompose(r -> {
            List<PubSubConnectionEntry> ees = ffs.stream()
                    .map(v -> v.join())
                    .filter(e -> !trackedEntries.contains(e))
                    .collect(Collectors.toList());
            if (ees.isEmpty()) {
                return CompletableFuture.completedFuture(listenerId);
            }

            trackedEntries.addAll(ees);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (PubSubConnectionEntry ee : ees) {
                RedisPubSubConnection c = ee.getConnection();
                RFuture<Long> idFuture = c.async(RedisCommands.CLIENT_ID);
                CompletionStage<Void> f = idFuture.thenCompose(id -> {
                    if (key != null) {
                        return commandExecutor.readAsync(c.getRedisClient(), key, StringCodec.INSTANCE,
                                RedisCommands.CLIENT_TRACKING, "ON", "REDIRECT", id);
                    }
                    return commandExecutor.readAsync(c.getRedisClient(), StringCodec.INSTANCE,
                            RedisCommands.CLIENT_TRACKING, "ON", "REDIRECT", id);
                });
                futures.add(f.toCompletableFuture());
            }

            CompletableFuture<Void> f = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            return f.thenApply(r2 -> listenerId);
        });
    }

    /** 移除 Flush/Tracking 监听器并 UNSUBSCRIBE 对应 entryListener。 */
    public CompletableFuture<Void> removeFlushListenerAsync(int listenerId) {
        Collection<Integer> ids = flushListeners.remove(listenerId);
        if (ids == null) {
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Integer id : ids) {
            CompletableFuture<Void> f = removeListenerAsync(PubSubType.UNSUBSCRIBE, ChannelName.newList(ChannelName.TRACKING), id);
            futures.add(f);
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /** 按 key 定向订阅 tracking 变更（过滤 msg 等于 key）。 */
    public CompletableFuture<Integer> subscribe(String key, Codec codec,
                                                CommandAsyncExecutor commandExecutor, TrackingListener listener) {
        MasterSlaveEntry entry = connectionManager.getEntry(key);

        RedisPubSubListener<Object> redisPubSubListener = (channel, msg) -> {
            if (channel.equals(ChannelName.TRACKING.toString())
                    && key.equals(msg)) {
                listener.onChange((String) msg);
            }
        };

        int listenerId = System.identityHashCode(redisPubSubListener);

        Collection<ClientConnectionsEntry> entries = entry.getAllEntries();

        if (config.getReadMode() != ReadMode.MASTER_SLAVE) {
            entries = entry.getAllEntries().stream()
                    .filter(e -> !e.isFreezed())
                    .collect(Collectors.toList());
        }

        List<CompletableFuture<PubSubConnectionEntry>> ffs = new ArrayList<>();
        for (ClientConnectionsEntry ee : entries) {
            CompletableFuture<PubSubConnectionEntry> future =
                    subscribe(PubSubType.SUBSCRIBE, codec, ChannelName.newList(ChannelName.TRACKING), entry, ee, redisPubSubListener);
            ffs.add(future);
        }

        return registerClientTrackingListener(commandExecutor, ffs, listenerId, key);
    }

    /** 对涉及的全部 channel 锁 acquire，返回汇总信号量与锁集合。 */
    private Tuple<AsyncSemaphore, Set<AsyncSemaphore>> acquire(List<ChannelName> channelNames) {
        AsyncSemaphore result = new AsyncSemaphore(0);
        Set<AsyncSemaphore> locks = new HashSet<>();
        for (ChannelName channelName : channelNames) {
            AsyncSemaphore lock = getSemaphore(channelName);
            locks.add(lock);
        }

        Set<CompletableFuture<Void>> lockFutures = new HashSet<>();
        for (AsyncSemaphore lock : locks) {
            CompletableFuture<Void> f = lock.acquire();
            lockFutures.add(f);
        }

        CompletableFuture<Void> lockFuture = CompletableFuture.allOf(lockFutures.toArray(new CompletableFuture[0]));
        lockFuture.thenAccept(r -> {
            result.release();
        });

        return new Tuple<>(result, locks);
    }

    /** 单频道 SUBSCRIBE 便捷重载。 */
    public CompletableFuture<List<PubSubConnectionEntry>> subscribe(Codec codec, ChannelName channelName, RedisPubSubListener<?>... listeners) {
        return subscribe(codec, ChannelName.newList(channelName), listeners);
    }

    /**
     * 批量 SUBSCRIBE；多实体 keyspace 时 fan-out 到所有节点。
     * 单槽位模式解析 slot 对应 MasterSlaveEntry 后订阅。
     */
    public CompletableFuture<List<PubSubConnectionEntry>> subscribe(Codec codec, List<ChannelName> channelNames, RedisPubSubListener<?>... listeners) {
        if (isMultiEntity(channelNames.get(0))) {
            Collection<MasterSlaveEntry> entrySet = connectionManager.getEntrySet();

            AtomicInteger statusCounter = new AtomicInteger(entrySet.size());
            RedisPubSubListener[] ls = Arrays.stream(listeners).map(l -> {
                if (l instanceof PubSubStatusListener) {
                    return new PubSubStatusListener(((PubSubStatusListener) l).getListener(), ((PubSubStatusListener) l).getNames()) {
                        @Override
                        public void onStatus(PubSubType type, CharSequence channel) {
                            if (statusCounter.get() == 0 || statusCounter.decrementAndGet() == 0) {
                                super.onStatus(type, channel);
                            }
                        }
                    };
                }
                return l;
            }).toArray(RedisPubSubListener[]::new);

            List<CompletableFuture<PubSubConnectionEntry>> futures = new ArrayList<>();
            for (MasterSlaveEntry entry : entrySet) {
                CompletableFuture<PubSubConnectionEntry> future =
                        subscribe(PubSubType.SUBSCRIBE, codec, new ArrayList<>(channelNames), entry, entry.getEntry(), ls);
                futures.add(future);
            }
            CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            return future.thenApply(r -> {
                return futures.stream().map(v -> v.getNow(null)).collect(Collectors.toList());
            });
        }

        MasterSlaveEntry entry = getEntry(channelNames.get(0));
        if (entry == null) {
            int slot = connectionManager.calcSlot(channelNames.get(0).getName());
            return connectionManager.getServiceManager().createNodeNotFoundFuture(channelNames.get(0).toString(), slot);
        }
        CompletableFuture<PubSubConnectionEntry> f = subscribe(PubSubType.SUBSCRIBE, codec, new ArrayList<>(channelNames), entry, null, listeners);
        return f.thenApply(res -> new ArrayList<>(Collections.singletonList(res)));
    }

    /** 分片频道 SSUBSCRIBE（Redis 7 集群）。 */
    public CompletableFuture<PubSubConnectionEntry> ssubscribe(Codec codec, List<ChannelName> channelNames, RedisPubSubListener<?>... listeners) {
        MasterSlaveEntry entry = getEntry(channelNames.get(0));
        if (entry == null) {
            int slot = connectionManager.calcSlot(channelNames.get(0).getName());
            return connectionManager.getServiceManager().createNodeNotFoundFuture(channelNames.get(0).toString(), slot);
        }
        return subscribe(PubSubType.SSUBSCRIBE, codec, new ArrayList<>(channelNames), entry, null, listeners);
    }

    /**
     * 带 subscriptionTimeout 的订阅入口：先 acquire 多 channel 锁，
     * 再调用 subscribeNoTimeout 并设置 promise 超时。
     */
    private CompletableFuture<PubSubConnectionEntry> subscribe(PubSubType type, Codec codec, List<ChannelName> channelNames,
                                                               MasterSlaveEntry entry, ClientConnectionsEntry clientEntry,
                                                               RedisPubSubListener<?>... listeners) {
        CompletableFuture<PubSubConnectionEntry> promise = new CompletableFuture<>();

        Tuple<AsyncSemaphore, Set<AsyncSemaphore>> locks = acquire(channelNames);
        AsyncSemaphore lock = locks.getT1();

        int timeout = config.getSubscriptionTimeout();
        long start = System.nanoTime();
        Timeout lockTimeout = connectionManager.getServiceManager().newTimeout(t -> {
            promise.completeExceptionally(new RedisTimeoutException(
                    "Unable to acquire subscription lock after " + timeout + "ms. " +
                            "Try to increase 'subscriptionTimeout', 'subscriptionsPerConnection', 'subscriptionConnectionPoolSize' parameters."));
        }, timeout, TimeUnit.MILLISECONDS);
        lock.acquire().thenAccept(r -> {
            if (!lockTimeout.cancel() || promise.isDone()) {
                lock.release();
                return;
            }

            long newTimeout = timeout - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            subscribeNoTimeout(codec, channelNames, entry, clientEntry, promise, type, lock, new AtomicInteger(), listeners);
            timeout(promise, newTimeout);
        });
        lock.acquire().thenAccept(rr -> {
            locks.getT2().forEach(l -> l.release());
        });
        return promise;
    }

    /** 内部锁/信号量用：无全局超时，按 shardingSupported 选 SUBSCRIBE 或 SSUBSCRIBE。 */
    CompletableFuture<PubSubConnectionEntry> subscribeNoTimeout(Codec codec, String channelName,
                                                                AsyncSemaphore semaphore, RedisPubSubListener<?>... listeners) {
        MasterSlaveEntry entry = getEntry(new ChannelName(channelName));
        if (entry == null) {
            semaphore.release();
            int slot = connectionManager.calcSlot(channelName);
            return connectionManager.getServiceManager().createNodeNotFoundFuture(channelName, slot);
        }

        PubSubType type;
        if (shardingSupported) {
            type = PubSubType.SSUBSCRIBE;
        } else {
            type = PubSubType.SUBSCRIBE;
        }

        CompletableFuture<PubSubConnectionEntry> promise = new CompletableFuture<>();
        subscribeNoTimeout(codec, ChannelName.newList(channelName), entry, null, promise,
                type, semaphore, new AtomicInteger(), listeners);
        return promise;
    }

    /** 按 channelName hash 选取 50 路锁之一。 */
    AsyncSemaphore getSemaphore(ChannelName channelName) {
        return locks[Math.abs(channelName.hashCode() % locks.length)];
    }

    /** 使用配置默认 subscriptionTimeout。 */
    void timeout(CompletableFuture<?> promise) {
        timeout(promise, config.getSubscriptionTimeout());
    }

    /** 超时未完成则 completeExceptionally(RedisTimeoutException)。 */
    void timeout(CompletableFuture<?> promise, long timeout) {
        Timeout task = connectionManager.getServiceManager().newTimeout(t -> {
            promise.completeExceptionally(new RedisTimeoutException(
                    "Unable to acquire subscription lock after " + timeout + "ms. " +
                            "Try to increase 'subscriptionTimeout', 'subscriptionsPerConnection', 'subscriptionConnectionPoolSize' parameters. "));
        }, timeout, TimeUnit.MILLISECONDS);
        promise.whenComplete((r, e) -> {
            task.cancel();
        });
    }

    /** 连接获取失败时按 retryAttempts/retryDelay 重试 subscribeNoTimeout。 */
    private void trySubscribe(Codec codec, List<ChannelName> channelNames,
                              CompletableFuture<PubSubConnectionEntry> promise, PubSubType type,
                              AsyncSemaphore lock, AtomicInteger attempts, RedisPubSubListener<?>... listeners) {
        ChannelName channelName = channelNames.get(0);
        if (attempts.get() == config.getRetryAttempts()) {
            lock.release();
            MasterSlaveEntry entry = getEntry(channelName);
            if (entry == null) {
                RedisNodeNotFoundException ex = new RedisNodeNotFoundException("Node for name: " + channelName + " hasn't been discovered yet. Check cluster slots coverage using CLUSTER NODES command. Increase value of retryAttempts and/or retryInterval settings.");
                promise.completeExceptionally(ex);
                return;
            }

            promise.completeExceptionally(new RedisTimeoutException(
                    "Unable to acquire connection for subscription after " + attempts.get() + " attempts. " +
                            "Increase 'subscriptionsPerConnection' and/or 'subscriptionConnectionPoolSize' parameters."));
            return;
        }

        attempts.incrementAndGet();

        MasterSlaveEntry entry = getEntry(channelName);
        if (entry == null) {
            Duration timeout = config.getRetryDelay().calcDelay(attempts.get());
            connectionManager.getServiceManager().newTimeout(tt -> {
                trySubscribe(codec, channelNames, promise, type, lock, attempts, listeners);
            }, timeout.toMillis(), TimeUnit.MILLISECONDS);
            return;
        }

        subscribeNoTimeout(codec, channelNames, entry, null, promise, type, lock, attempts, listeners);
    }

    /**
     * 核心订阅逻辑：先 addListeners 复用已有连接；否则从空闲池取连接或 connect 新连接，
     * 再调用 PubSubConnectionEntry.subscribe 发送 Redis SUBSCRIBE。
     */
    private void subscribeNoTimeout(Codec codec, List<ChannelName> channelNames, MasterSlaveEntry entry,
                                    ClientConnectionsEntry clientEntry, CompletableFuture<PubSubConnectionEntry> promise,
                                    PubSubType type, AsyncSemaphore lock, AtomicInteger attempts, RedisPubSubListener<?>... listeners) {
        CompletableFuture<Boolean> future = addListeners(channelNames, entry, clientEntry, type, listeners, null, () -> {}, promise, lock);
        future.thenAccept(r1 -> {
            if (r1) {
                return;
            }

            freePubSubLock.acquire().thenAccept(c -> {
                if (promise.isDone()) {
                    lock.release();
                    freePubSubLock.release();
                    return;
                }

                PubSubEntry freePubSubConnections = entry2PubSubConnection.getOrDefault(entry, new PubSubEntry());

                // 尝试复用同 MasterSlave 条目下的空闲 PubSub 连接
                PubSubConnectionEntry freeEntry = freePubSubConnections.getEntries().peek();
                if (freeEntry != null && clientEntry != null) {
                    if (!clientEntry.getClient().equals(freeEntry.getConnection().getRedisClient())) {
                        freeEntry = null;
                    }
                }

                if (freeEntry == null) {
                    freePubSubLock.release();
                    connect(codec, channelNames, entry, clientEntry, promise, type, lock, attempts, listeners);
                    return;
                }

                int remainFreeAmount = freeEntry.tryAcquire();
                if (remainFreeAmount == -1) {
                    throw new IllegalStateException();
                }

                PubSubConnectionEntry fe = freeEntry;
                CompletableFuture<Boolean> listenersFuture = addListeners(channelNames, entry, clientEntry, type, listeners,
                        freeEntry, () -> {
                            fe.release();
                            freePubSubLock.release();
                        }, promise, lock);
                listenersFuture.thenAccept(r2 -> {
                    if (r2) {
                        return;
                    }

                    for (ChannelName channelName : channelNames) {
                        Collection<PubSubConnectionEntry> coll = name2entry.computeIfAbsent(channelName, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
                        coll.add(fe);
                    }

                    if (remainFreeAmount == 0) {
                        freePubSubConnections.getEntries().poll();
                    }
                    freePubSubLock.release();

                    fe.subscribe(codec, channelNames, promise, type, lock, listeners);
                });

            });
        });
    }

    /**
     * 若频道已在 name2PubSubConnection/key2connection 中注册，则向旧 entry 追加 listener
     * 并从待订阅列表移除；返回 true 表示已全部复用完成。
     */
    private CompletableFuture<Boolean> addListeners(List<ChannelName> channelNames, MasterSlaveEntry entry,
                                                    ClientConnectionsEntry clientEntry, PubSubType type,
                                                    RedisPubSubListener<?>[] listeners, PubSubConnectionEntry freeEntry,
                                                    Runnable releaser, CompletableFuture<PubSubConnectionEntry> promise,
                                                    AsyncSemaphore lock) {
        Map<CompletableFuture<Void>, Tuple<ChannelName, PubSubConnectionEntry>> releaseFutures = new HashMap<>();
        AtomicReference<PubSubConnectionEntry> ref = new AtomicReference<>();
        for (ChannelName channelName : channelNames.toArray(new ChannelName[0])) {
            PubSubConnectionEntry oldEntry = null;
            if (clientEntry != null) {
                Tuple<ChannelName, ClientConnectionsEntry> key = new Tuple<>(channelName, clientEntry);
                if (freeEntry == null) {
                    oldEntry = key2connection.get(key);
                } else {
                    oldEntry = key2connection.putIfAbsent(key, freeEntry);
                }
                if (channelName.isTracking()) {
                    clientEntry.getTrackedConnectionsHolder().incUsage();
                }
            }

            PubSubKey key = new PubSubKey(channelName, entry);
            PubSubConnectionEntry oe;
            if (freeEntry == null) {
                oe = name2PubSubConnection.get(key);
            } else {
                oe = name2PubSubConnection.putIfAbsent(key, freeEntry);
            }
            if (clientEntry == null) {
                oldEntry = oe;
            }

            if (oldEntry != null) {
                ref.compareAndSet(null, oldEntry);
                channelNames.remove(channelName);
                CompletableFuture<Void> f = oldEntry.addListeners(channelName, type, listeners);
                releaseFutures.put(f, new Tuple<>(channelName, oldEntry));
            }
        }

        CompletableFuture<Void> ff = CompletableFuture.allOf(releaseFutures.keySet().toArray(new CompletableFuture[0]));
        return ff.handle((r, ex) -> {

            if (ex != null) {
                releaser.run();

                promise.completeExceptionally(ex);

                CompletableFuture<Void>[] fff = releaseFutures.values().stream()
                        .map(t -> t.getT2().release(type, t.getT1(), listeners))
                        .toArray(CompletableFuture[]::new);
                CompletableFuture<Void> f1 = CompletableFuture.allOf(fff);
                f1.whenComplete((r1, e) -> {
                    lock.release();
                });
                return true;
            }

            if (channelNames.isEmpty()) {
                releaser.run();

                if (!promise.complete(ref.get())) {
                    CompletableFuture<Void>[] fff = releaseFutures.values().stream()
                            .map(t -> t.getT2().release(type, t.getT1(), listeners))
                            .toArray(CompletableFuture[]::new);
                    CompletableFuture<Void> f1 = CompletableFuture.allOf(fff);
                    f1.whenComplete((r1, e) -> {
                        lock.release();
                    });
                } else {
                    lock.release();
                }
                return true;
            }
            return false;
        });
    }

    /** 按 channel 名计算 slot 并返回写 MasterSlaveEntry。 */
    private MasterSlaveEntry getEntry(ChannelName channelName) {
        int slot = connectionManager.calcSlot(channelName.getName());
        return connectionManager.getWriteEntry(slot);
    }

    /** 从 MasterSlaveEntry 申请新 PubSub 连接，创建 PubSubConnectionEntry 并 subscribe。 */
    private void connect(Codec codec, List<ChannelName> channelNames,
                         MasterSlaveEntry msEntry, ClientConnectionsEntry clientEntry,
                         CompletableFuture<PubSubConnectionEntry> promise,
                         PubSubType type, AsyncSemaphore lock, AtomicInteger attempts,
                         RedisPubSubListener<?>... listeners) {

        Duration timeout = config.getRetryDelay().calcDelay(attempts.get());

        CompletableFuture<RedisPubSubConnection> connFuture = msEntry.nextPubSubConnection(clientEntry);
        connectionManager.getServiceManager().newTimeout(t -> {
            if (!connFuture.cancel(false)
                    && !connFuture.isCompletedExceptionally()) {
                return;
            }

            trySubscribe(codec, channelNames, promise, type, lock, attempts, listeners);
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);

        promise.whenComplete((res, e) -> {
            if (e != null) {
                connFuture.completeExceptionally(e);
            }
        });

        connFuture.thenAccept(conn -> {
            freePubSubLock.acquire().thenAccept(c -> {
                PubSubConnectionEntry entry = new PubSubConnectionEntry(conn, connectionManager, msEntry);
                int remainFreeAmount = entry.tryAcquire();


                CompletableFuture<Boolean> listenerFuture = addListeners(channelNames, msEntry, clientEntry, type, listeners, entry,
                        () -> {
                            msEntry.returnPubSubConnection(entry.getConnection());
                            freePubSubLock.release();
                        }, promise, lock);
                listenerFuture.thenAccept(r -> {
                    if (r) {
                        return;
                    }

                    for (ChannelName channelName : channelNames) {
                        Collection<PubSubConnectionEntry> coll = name2entry.computeIfAbsent(channelName, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
                        coll.add(entry);
                    }

                    if (remainFreeAmount > 0) {
                        PubSubEntry psEntry = entry2PubSubConnection.computeIfAbsent(msEntry, e -> new PubSubEntry());
                        psEntry.getEntries().add(entry);
                    }
                    freePubSubLock.release();

                    entry.subscribe(codec, channelNames, promise, type, lock, listeners);
                });
            });
        });
    }

    /** 按 shardingSupported 选择 UNSUBSCRIBE/SUNSUBSCRIBE 并 unsubscribeLocked。 */
    CompletableFuture<Void> unsubscribeLocked(ChannelName channelName) {
        Collection<PubSubConnectionEntry> coll = name2entry.get(channelName);
        if (coll == null || coll.isEmpty()) {
            RedisException ex = new RedisException("Channel: " + channelName + " is not registered");
            CompletableFuture<Void> promise = new CompletableFuture<>();
            promise.completeExceptionally(ex);
            return promise;
        }

        PubSubType topicType = PubSubType.UNSUBSCRIBE;
        if (shardingSupported) {
            topicType = PubSubType.SUNSUBSCRIBE;
        }

        return unsubscribeLocked(topicType, channelName, coll.iterator().next());
    }

    /** 从索引 remove 后发送 UNSUBSCRIBE，ACK 后 release 连接回池或归还连接管理器。 */
    CompletableFuture<Void> unsubscribeLocked(PubSubType topicType, ChannelName channelName, PubSubConnectionEntry ce) {
        remove(channelName, ce);

        if (connectionManager.getServiceManager().isShuttingDown()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> result = new CompletableFuture<>();
        BaseRedisPubSubListener listener = new BaseRedisPubSubListener() {

            @Override
            public void onStatus(PubSubType type, CharSequence channel) {
                if (type == topicType && channel.equals(channelName)) {
                    freePubSubLock.acquire().thenAccept(c -> {
                        try {
                            release(ce);
                        } catch (Exception e) {
                            result.completeExceptionally(e);
                        } finally {
                            freePubSubLock.release();
                        }

                        result.complete(null);
                    });
                }
            }

        };

        ce.unsubscribe(topicType, channelName, listener);
        return result;
    }

    /** 清理 name2PubSubConnection、key2connection、name2entry 及 tracking 引用计数。 */
    private void remove(ChannelName channelName, PubSubConnectionEntry entry) {
        name2PubSubConnection.remove(new PubSubKey(channelName, entry.getEntry()));

        ClientConnectionsEntry e = entry.getEntry().getEntry(entry.getConnection().getRedisClient());
        if (e != null) {
            Tuple<ChannelName, ClientConnectionsEntry> key = new Tuple<>(channelName, e);
            key2connection.remove(key);
            if (e.getTrackedConnectionsHolder().decUsage() == 0) {
                e.getTrackedConnectionsHolder().reset();
                trackedEntries.remove(entry);
            }
        }

        name2entry.computeIfPresent(channelName, (name, entries) -> {
            entries.remove(entry);
            if (entries.isEmpty()) {
                return null;
            }
            return entries;
        });
    }

    /** 递增连接槽位；完全空闲则归还连接，否则放回 entry2PubSubConnection 队列。 */
    private void release(PubSubConnectionEntry entry) {
        entry.release();
        if (entry.isFree()) {
            PubSubEntry ee = entry2PubSubConnection.get(entry.getEntry());
            if (ee != null) {
                ee.getEntries().remove(entry);
            }
            entry.getEntry().returnPubSubConnection(entry.getConnection());
            return;
        }

        PubSubEntry ee = entry2PubSubConnection.computeIfAbsent(entry.getEntry(), e -> new PubSubEntry());
        if (entry.getConnection().isClosed()) {
            ee.getEntries().remove(entry);
        } else if (!ee.getEntries().contains(entry)) {
            ee.getEntries().add(entry);
        }
    }

    /** MasterSlave 条目下线时清理其全部 PubSub 状态。 */
    public void remove(MasterSlaveEntry entry) {
        entry2PubSubConnection.remove(entry);
        name2entry.values().removeIf(v -> {
            v.removeIf(e -> e.getEntry().equals(entry));
            return v.isEmpty();
        });
    }

    /** 在 per-channel 锁保护下 UNSUBSCRIBE 并返回原频道 codec。 */
    public CompletableFuture<Codec> unsubscribe(ChannelName channelName, PubSubType topicType) {
        Collection<PubSubConnectionEntry> coll = name2entry.get(channelName);
        if (coll == null || coll.isEmpty()) {
            RedisException ex = new RedisException("Channel: " + channelName + " is not registered");
            CompletableFuture<Codec> promise = new CompletableFuture<>();
            promise.completeExceptionally(ex);
            return promise;
        }

        return unsubscribe(channelName, coll.iterator().next(), topicType);
    }

    CompletableFuture<Codec> unsubscribe(ChannelName channelName, PubSubConnectionEntry entry, PubSubType topicType) {
        if (connectionManager.getServiceManager().isShuttingDown()) {
            return CompletableFuture.completedFuture(null);
        }

        AsyncSemaphore lock = getSemaphore(channelName);
        CompletableFuture<Void> f = lock.acquire();
        return f.thenCompose(v -> {
            Codec entryCodec;
            if (topicType == PubSubType.PUNSUBSCRIBE) {
                entryCodec = entry.getConnection().getPatternChannels().get(channelName);
            } else if (topicType == PubSubType.SUNSUBSCRIBE) {
                entryCodec = entry.getConnection().getShardedChannels().get(channelName);
            } else {
                entryCodec = entry.getConnection().getChannels().get(channelName);
            }

            CompletableFuture<Void> result = unsubscribeLocked(topicType, channelName, entry);
            return result.whenComplete((r, e) -> {
                lock.release();
            }).thenApply(r -> {
                return entryCodec;
            });
        });
    }

    /** 集群 slot 迁移后，对该 slot 全部订阅先 UNSUBSCRIBE 再 SUBSCRIBE 重建。 */
    public void reattachPubSub(int slot) {
        name2PubSubConnection.entrySet().stream()
                .filter(e -> connectionManager.calcSlot(e.getKey().getChannelName().getName()) == slot)
                .forEach(entry -> {
                    PubSubConnectionEntry pubSubEntry = entry.getValue();

                    Codec codec = pubSubEntry.getConnection().getChannels().get(entry.getKey().getChannelName());
                    if (codec != null) {
                        Queue<RedisPubSubListener<?>> listeners = pubSubEntry.getListeners(entry.getKey().getChannelName());
                        unsubscribe(entry.getKey().getChannelName(), pubSubEntry, PubSubType.UNSUBSCRIBE);
                        subscribe(codec, ChannelName.newList(entry.getKey().getChannelName()), listeners.toArray(new RedisPubSubListener[0]));
                    }

                    Codec scodec = pubSubEntry.getConnection().getShardedChannels().get(entry.getKey().getChannelName());
                    if (scodec != null) {
                        Queue<RedisPubSubListener<?>> listeners = pubSubEntry.getListeners(entry.getKey().getChannelName());
                        unsubscribe(entry.getKey().getChannelName(), pubSubEntry, PubSubType.SUNSUBSCRIBE);
                        ssubscribe(codec, ChannelName.newList(entry.getKey().getChannelName()), listeners.toArray(new RedisPubSubListener[0]));
                    }

                    Codec patternCodec = pubSubEntry.getConnection().getPatternChannels().get(entry.getKey().getChannelName());
                    if (patternCodec != null) {
                        Queue<RedisPubSubListener<?>> listeners = pubSubEntry.getListeners(entry.getKey().getChannelName());
                        unsubscribe(entry.getKey().getChannelName(), pubSubEntry, PubSubType.PUNSUBSCRIBE);
                        psubscribe(entry.getKey().getChannelName(), patternCodec, listeners.toArray(new RedisPubSubListener[0]));
                    }
                });
    }

    /** 单条 PubSub 连接重连后，按 channels/sharded/pattern 三套 map 重挂 listener。 */
    public void reattachPubSub(RedisPubSubConnection redisPubSubConnection) {
        MasterSlaveEntry en = connectionManager.getEntry(redisPubSubConnection.getRedisClient());
        if (en == null) {
            return;
        }

        reattachPubSubListeners(redisPubSubConnection.getChannels().keySet(), en, PubSubType.UNSUBSCRIBE);
        reattachPubSubListeners(redisPubSubConnection.getShardedChannels().keySet(), en, PubSubType.SUNSUBSCRIBE);
        reattachPubSubListeners(redisPubSubConnection.getPatternChannels().keySet(), en, PubSubType.PUNSUBSCRIBE);
    }

    /** 逐频道 unsubscribe 再 subscribe，失败 1 秒后重试。 */
    private void reattachPubSubListeners(Set<ChannelName> channels, MasterSlaveEntry en, PubSubType topicType) {
        for (ChannelName channelName : channels) {
            PubSubConnectionEntry entry = name2PubSubConnection.get(new PubSubKey(channelName, en));
            if (entry == null) {
                continue;
            }
            Collection<RedisPubSubListener<?>> listeners = entry.getListeners(channelName);
            CompletableFuture<Codec> subscribeCodecFuture = unsubscribe(channelName, entry, topicType);
            if (listeners.isEmpty()) {
                continue;
            }

            subscribeCodecFuture.whenComplete((subscribeCodec, e) -> {
                if (e != null) {
                    log.error(e.getMessage(), e);
                    return;
                }
                if (subscribeCodec == null) {
                    return;
                }

                if (topicType == PubSubType.PUNSUBSCRIBE) {
                    psubscribe(en, channelName, listeners, subscribeCodec);
                } else if (topicType == PubSubType.SUNSUBSCRIBE) {
                    ssubscribe(channelName, listeners, subscribeCodec);
                } else {
                    subscribe(channelName, listeners, subscribeCodec);
                }
            });
        }
    }

    /** 重连后普通 SUBSCRIBE 重订阅，失败定时重试。 */
    private void subscribe(ChannelName channelName, Collection<RedisPubSubListener<?>> listeners,
                           Codec subscribeCodec) {
        if (connectionManager.getServiceManager().isShuttingDown()) {
            log.warn("listeners of '{}' channel haven't been resubscribed due to Redisson shutdown process", channelName);
            return;
        }

        MasterSlaveEntry entry = getEntry(channelName);
        if (isMultiEntity(channelName)) {
            entry = connectionManager.getEntrySet()
                    .stream()
                    .filter(e -> !name2PubSubConnection.containsKey(new PubSubKey(channelName, e)))
                    .findFirst()
                    .orElse(null);
        }

        CompletableFuture<PubSubConnectionEntry> subscribeFuture;
        if (entry != null) {
            subscribeFuture = subscribe(PubSubType.SUBSCRIBE, subscribeCodec, ChannelName.newList(channelName), entry, null, listeners.toArray(new RedisPubSubListener[0]));
        } else {
            subscribeFuture = subscribe(subscribeCodec, ChannelName.newList(channelName), listeners.toArray(new RedisPubSubListener[0])).thenApply(r -> r.iterator().next());
        }
        subscribeFuture.whenComplete((res, e) -> {
            if (e != null) {
                connectionManager.getServiceManager().newTimeout(task -> {
                    subscribe(channelName, listeners, subscribeCodec);
                }, 1, TimeUnit.SECONDS);
                return;
            }

            log.info("listeners of '{}' channel have been resubscribed to '{}'", channelName, res);
        });
    }

    private void ssubscribe(ChannelName channelName, Collection<RedisPubSubListener<?>> listeners,
                            Codec subscribeCodec) {
        if (connectionManager.getServiceManager().isShuttingDown()) {
            log.warn("listeners of '{}' channel haven't been resubscribed due to Redisson shutdown process", channelName);
            return;
        }

        CompletableFuture<PubSubConnectionEntry> subscribeFuture =
                ssubscribe(subscribeCodec, ChannelName.newList(channelName), listeners.toArray(new RedisPubSubListener[0]));
        subscribeFuture.whenComplete((res, e) -> {
            if (e != null) {
                connectionManager.getServiceManager().newTimeout(task -> {
                    ssubscribe(channelName, listeners, subscribeCodec);
                }, 1, TimeUnit.SECONDS);
                return;
            }

            log.info("listeners of '{}' sharded-channel have been resubscribed to '{}'", channelName, res);
        });
    }

    private void psubscribe(MasterSlaveEntry oldEntry, ChannelName channelName, Collection<RedisPubSubListener<?>> listeners,
                            Codec subscribeCodec) {
        if (connectionManager.getServiceManager().isShuttingDown()) {
            log.warn("listeners of '{}' channel-pattern haven't been resubscribed due to Redisson shutdown process", channelName);
            return;
        }

        MasterSlaveEntry entry = getEntry(channelName);
        if (isMultiEntity(channelName)) {
            entry = connectionManager.getEntrySet()
                    .stream()
                    .filter(e -> !name2PubSubConnection.containsKey(new PubSubKey(channelName, e))
                            && (!connectionManager.getServiceManager().getCfg().isClusterConfig() || e != oldEntry))
                    .findFirst()
                    .orElse(null);
        }
        if (entry == null) {
            connectionManager.getServiceManager().newTimeout(task -> {
                psubscribe(oldEntry, channelName, listeners, subscribeCodec);
            }, 1, TimeUnit.SECONDS);
            return;
        }

        CompletableFuture<PubSubConnectionEntry> subscribeFuture =
                subscribe(PubSubType.PSUBSCRIBE, subscribeCodec, ChannelName.newList(channelName), entry, null, listeners.toArray(new RedisPubSubListener[0]));
        subscribeFuture.whenComplete((res, e) -> {
            if (e != null) {
                connectionManager.getServiceManager().newTimeout(task -> {
                    psubscribe(oldEntry, channelName, listeners, subscribeCodec);
                }, 1, TimeUnit.SECONDS);
                return;
            }

            log.info("listeners of '{}' channel-pattern have been resubscribed to '{}'", channelName, res);
        });
    }

    /** 异步按 EventListener 引用移除监听器，无 listener 时 UNSUBSCRIBE。 */
    public CompletableFuture<Void> removeListenerAsync(PubSubType type, List<ChannelName> channelNames, EventListener listener) {
        return removeListenerAsync(type, channelNames, (channelName, entry) -> {
            entry.removeListener(channelName, listener);
        });
    }

    public CompletableFuture<Void> removeListenerAsync(PubSubType type, List<ChannelName> channelNames, Integer... listenerIds) {
        return removeListenerAsync(type, channelNames, (channelName, entry) -> {
            for (int id : listenerIds) {
                entry.removeListener(channelName, id);
            }
        });
    }

    private CompletableFuture<Void> removeListenerAsync(PubSubType type, List<ChannelName> names, BiConsumer<ChannelName, PubSubConnectionEntry> consumer) {
        if (connectionManager.getServiceManager().isShuttingDown()) {
            return CompletableFuture.completedFuture(null);
        }

        List<ChannelName> channelNames = names.stream().filter(cn -> name2entry.containsKey(cn)).collect(Collectors.toList());

        Tuple<AsyncSemaphore, Set<AsyncSemaphore>> locks = acquire(channelNames);
        AsyncSemaphore semaphore = locks.getT1();

        CompletableFuture<Void> sf = semaphore.acquire();
        int timeout = config.getSubscriptionTimeout();

        Exception stackTrace = new Exception("Stack trace");
        Timeout r = connectionManager.getServiceManager().newTimeout(t -> {
            RedisTimeoutException ee = new RedisTimeoutException("Remove listeners operation timeout: (" + timeout + "ms) for "
                    + channelNames + " topic");
            ee.addSuppressed(stackTrace);
            sf.completeExceptionally(ee);
        }, timeout, TimeUnit.MILLISECONDS);

        CompletableFuture<Void> result = sf.thenCompose(res -> {
            r.cancel();

            if (connectionManager.getServiceManager().isShuttingDown()) {
                semaphore.release();
                return CompletableFuture.completedFuture(null);
            }

            Map<ChannelName, Collection<PubSubConnectionEntry>> name2entries = channelNames.stream()
                    .filter(cn -> {
                        Collection<PubSubConnectionEntry> entries = name2entry.get(cn);
                        return entries != null && !entries.isEmpty();
                    }).collect(Collectors.toMap(cn -> cn, cn -> name2entry.get(cn)));

            if (name2entries.isEmpty()) {
                semaphore.release();
                return CompletableFuture.completedFuture(null);
            }

            List<CompletableFuture<?>> futures = new ArrayList<>();
            name2entries.forEach((channelName, entries) -> {

                for (PubSubConnectionEntry entry : entries) {
                    consumer.accept(channelName, entry);

                    CompletableFuture<Void> f;
                    if (!entry.hasListeners(channelName)) {
                        f = unsubscribeLocked(type, channelName, entry)
                                .exceptionally(ex -> null);
                    } else {
                        f = CompletableFuture.completedFuture(null);
                    }
                    futures.add(f);
                }
            });

            CompletableFuture<Void> ff = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            return ff.whenComplete((v, e) -> semaphore.release());
        });

        semaphore.acquire().thenAccept(rr -> {
            locks.getT2().forEach(l -> l.release());
        });

        return result;
    }

    /** 移除频道上全部 listener 并 UNSUBSCRIBE。 */
    public CompletableFuture<Void> removeAllListenersAsync(PubSubType type, ChannelName... channelNames) {
        List<CompletableFuture<Void>> fs = new ArrayList<>();
        for (ChannelName channelName : channelNames) {
            if (!name2entry.containsKey(channelName)) {
                continue;
            }

            AsyncSemaphore semaphore = getSemaphore(channelName);

            CompletableFuture<Void> sf = semaphore.acquire();
            int timeout = config.getSubscriptionTimeout();
            connectionManager.getServiceManager().newTimeout(t -> {
                sf.completeExceptionally(new RedisTimeoutException("Remove listeners operation timeout: (" + timeout + "ms) for " + channelName + " topic"));
            }, timeout, TimeUnit.MILLISECONDS);

            CompletableFuture<Void> f = sf.thenCompose(r -> {
                Collection<PubSubConnectionEntry> entries = name2entry.getOrDefault(channelName, Collections.emptySet());
                if (entries.isEmpty()) {
                    semaphore.release();
                    return CompletableFuture.completedFuture(null);
                }

                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (PubSubConnectionEntry entry : entries) {
                    if (entry.hasListeners(channelName)) {
                        CompletableFuture<Void> ff = unsubscribeLocked(type, channelName, entry);
                        futures.add(ff);
                    }
                }

                if (!futures.isEmpty()) {
                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete((res, e) -> {
                        semaphore.release();
                    });
                }

                semaphore.release();
                return CompletableFuture.completedFuture(null);
            });
            fs.add(f);
        }
        return CompletableFuture.allOf(fs.toArray(new CompletableFuture[0]));
    }

    /** 探测 PUBSUB NUMPAT，失败则禁用模式订阅。 */
    public void checkPatternSupport(RedisConnection connection) {
        try {
            connection.sync(RedisCommands.PUBSUB_NUMPAT);
        } catch (Exception e) {
            setPatternSupported(false);
        }
    }

    /** AUTO 模式探测 PUBSUB SHARDNUMSUB；ON 强制启用分片订阅。 */
    public void checkShardingSupport(ShardedSubscriptionMode mode, RedisConnection connection) {
        if (mode == ShardedSubscriptionMode.AUTO) {
            try {
                connection.sync(RedisCommands.PUBSUB_SHARDNUMSUB, 0);
                setShardingSupported(true);
            } catch (Exception e) {
                // skip
            }
        } else if (mode == ShardedSubscriptionMode.ON) {
            setShardingSupported(true);
        }
    }

    public boolean isPatternSupported() {
        return patternSupported;
    }
    public void setPatternSupported(boolean patternSupported) {
        this.patternSupported = patternSupported;
    }

    public void setShardingSupported(boolean shardingSupported) {
        this.shardingSupported = shardingSupported;
    }
    public boolean isShardingSupported() {
        return shardingSupported;
    }

    /** 分片模式下返回 SPUBLISH，否则 PUBLISH。 */
    public String getPublishCommand() {
        if (shardingSupported) {
            return RedisCommands.SPUBLISH.getName();
        }
        return RedisCommands.PUBLISH.getName();
    }

    @Override
    public String toString() {
        return "PublishSubscribeService [name2PubSubConnection=" + name2PubSubConnection + ", entry2PubSubConnection=" + entry2PubSubConnection + "]";
    }

}
