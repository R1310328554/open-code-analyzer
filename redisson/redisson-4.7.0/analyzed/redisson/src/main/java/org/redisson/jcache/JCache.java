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
package org.redisson.jcache;

import io.netty.buffer.ByteBuf;
import org.redisson.*;
import org.redisson.api.*;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.decoder.MapScanResult;
import org.redisson.client.protocol.decoder.MapValueDecoder;
import org.redisson.codec.BaseEventCodec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.connection.ServiceManager;
import org.redisson.connection.decoder.MapGetAllDecoder;
import org.redisson.iterator.RedissonBaseMapIterator;
import org.redisson.jcache.JMutableEntry.Action;
import org.redisson.jcache.configuration.JCacheConfiguration;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.misc.Hash;
import org.redisson.reactive.ReactiveProxyBuilder;
import org.redisson.rx.RxProxyBuilder;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.event.*;
import javax.cache.integration.*;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Redisson 对 JSR-107（JCache）规范的核心实现。
 * <p>
 * 底层以 Redis Hash + 超时 ZSET 存储 entry，通过 Pub/Sub 频道分发
 * CREATED/UPDATED/REMOVED/EXPIRED 事件；支持 read/write-through、
 * EntryProcessor、ExpiryPolicy、统计与集群同步监听器。
 * <p>
 * {@code atomicExecution} 为 true 时使用 Lua 原子脚本（生产模式）；
 * TCK 模式下为 false，对单 key 加分布式锁以符合一致性测试。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key
 * @param <V> value
 */
public class JCache<K, V> extends RedissonObject implements Cache<K, V>, CacheAsync<K, V> {

    /** TCK 未设置 agentId 时为 true，走 Lua 原子路径无需 per-key 锁。 */
    final boolean atomicExecution = System.getProperty("org.jsr107.tck.management.agentId") == null;

    /** 所属 CacheManager，用于统计 Bean 与生命周期。 */
    final JCacheManager cacheManager;
    /** 完整 JCache 配置（过期策略、监听器、Loader/Writer 等）。 */
    final JCacheConfiguration<K, V> config;
    /** 监听器配置 → (topicListenerId → channelName) 注册表。 */
    final ConcurrentMap<CacheEntryListenerConfiguration<K, V>, Map<Integer, String>> listeners = new ConcurrentHashMap<>();
    /** 底层 Redisson 客户端（可与 Manager 共享或独立）。 */
    final Redisson redisson;

    /** read-through / loadAll 使用的 CacheLoader。 */
    private CacheLoader<K, V> cacheLoader;
    /** write-through 使用的 CacheWriter。 */
    private CacheWriter<K, V> cacheWriter;
    /** Cache 是否已 close。 */
    private AtomicBoolean closed = new AtomicBoolean();
    /** 是否持有独立 Redisson 实例（close 时需 shutdown）。 */
    private boolean hasOwnRedisson;

    /* 原子执行模式下无需 per-key 分布式锁。 */

    /**
     * 构造 JCache：初始化 Loader/Writer、注册过期调度与配置中的监听器。
     */
    JCache(JCacheManager cacheManager, Redisson redisson, String name, JCacheConfiguration<K, V> config, boolean hasOwnRedisson) {
        super(redisson.getConfig().getCodec(), redisson.getCommandExecutor(), name);

        this.hasOwnRedisson = hasOwnRedisson;
        this.redisson = redisson;

        Factory<CacheLoader<K, V>> cacheLoaderFactory = config.getCacheLoaderFactory();
        if (cacheLoaderFactory != null) {
            cacheLoader = cacheLoaderFactory.create();
        }
        Factory<CacheWriter<? super K, ? super V>> cacheWriterFactory = config.getCacheWriterFactory();
        if (config.getCacheWriterFactory() != null) {
            cacheWriter = (CacheWriter<K, V>) cacheWriterFactory.create();
        }

        this.cacheManager = cacheManager;
        this.config = config;

        redisson.getEvictionScheduler().scheduleJCache(getRawName(), getTimeoutSetName(), getExpiredChannelName());

        for (CacheEntryListenerConfiguration<K, V> listenerConfig : config.getCacheEntryListenerConfigurations()) {
            registerCacheEntryListener(listenerConfig, false);
        }
    }

    /** 已 close 则抛 {@link IllegalStateException}。 */
    void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException();
        }
    }

    /** 本 Cache 超时 ZSET 键名（hash tag 与 rawName 一致）。 */
    String getTimeoutSetName() {
        return "jcache_timeout_set:{" + getRawName() + "}";
    }

    /** 指定 hash 名对应的超时 ZSET 键名。 */
    String getTimeoutSetName(String name) {
        return prefixName("jcache_timeout_set", name);
    }

    /** 集群同步信号量键名，syncId 为 publish 时生成的 double。 */
    String getSyncName(Object syncId) {
        return "jcache_sync:" + syncId + ":{" + getRawName() + "}";
    }

    /** CREATED 事件的同步 Pub/Sub 频道。 */
    String getCreatedSyncChannelName() {
        return "jcache_created_sync_channel:{" + getRawName() + "}";
    }

    /** 指定 hash 名的 CREATED 同步频道。 */
    String getCreatedSyncChannelName(String name) {
        return prefixName("jcache_created_sync_channel", name);
    }

    /** UPDATED 事件的同步 Pub/Sub 频道。 */
    String getUpdatedSyncChannelName() {
        return "jcache_updated_sync_channel:{" + getRawName() + "}";
    }

    /** 指定 hash 名的 UPDATED 同步频道。 */
    String getUpdatedSyncChannelName(String name) {
        return prefixName("jcache_updated_sync_channel", name);
    }

    /** REMOVED 事件的同步 Pub/Sub 频道。 */
    String getRemovedSyncChannelName() {
        return "jcache_removed_sync_channel:{" + getRawName() + "}";
    }

    /** 指定 hash 名的 REMOVED 同步频道。 */
    String getRemovedSyncChannelName(String name) {
        return prefixName("jcache_removed_sync_channel", name);
    }

    /** CREATED 事件异步 Pub/Sub 频道。 */
    String getCreatedChannelName() {
        return "jcache_created_channel:{" + getRawName() + "}";
    }

    /** 指定 hash 名的 CREATED 频道。 */
    String getCreatedChannelName(String name) {
        return prefixName("jcache_created_channel", name);
    }

    /** UPDATED 事件 Pub/Sub 频道。 */
    String getUpdatedChannelName() {
        return "jcache_updated_channel:{" + getRawName() + "}";
    }

    /** 指定 hash 名的 UPDATED 频道。 */
    String getUpdatedChannelName(String name) {
        return prefixName("jcache_updated_channel", name);
    }

    /** EXPIRED 事件 Pub/Sub 频道（由 EvictionTask 发布）。 */
    String getExpiredChannelName() {
        return "jcache_expired_channel:{" + getRawName() + "}";
    }

    /** 指定 hash 名的 REMOVED 频道。 */
    String getRemovedChannelName(String name) {
        return prefixName("jcache_removed_channel", name);
    }

    /** REMOVED 事件 Pub/Sub 频道。 */
    String getRemovedChannelName() {
        return "jcache_removed_channel:{" + getRawName() + "}";
    }

    /** Redis 计数器：需要 oldValue 的 UPDATE 监听器数量。 */
    String getOldValueListenerCounter() {
        return "jcache_old_value_listeners:{" + getRawName() + "}";
    }

    /** 指定 hash 名的 oldValue 监听器计数键。 */
    String getOldValueListenerCounter(String name) {
        return prefixName("jcache_old_value_listeners", name);
    }

    /** 统计启用时返回 {@link System#nanoTime()}，否则 0。 */
    long currentNanoTime() {
        if (config.isStatisticsEnabled()) {
            return System.nanoTime();
        }
        return 0;
    }

    /** JCache 规范：key 不可为 null。 */
    void checkKey(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }
    }

    /** 将底层异常包装为 {@link CacheException} 的 CompletionStage 处理器。 */
    <V> CompletionStage<V> handleException(CompletionStage<V> f) {
        return f.handle((r, e) -> {
            if (e != null) {
                if (e instanceof CompletionException) {
                    e = e.getCause();
                }
                if (e instanceof CacheException) {
                    throw new CompletionException(e);
                }
                throw new CompletionException(new CacheException(e));
            }
            return r;
        });
    }

    /** 阻塞等待 {@link RFuture} 并解包 cause 为 RuntimeException。 */
    <V> V sync(RFuture<V> result) {
        try {
            return result.toCompletableFuture().join();
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    /** 同步 get：加 per-key 锁后委托 {@link #getAsync}。 */
    @Override
    public V get(K key) {
        RLock lock = getLockedLock(key);
        try {
            RFuture<V> result = getAsync(key);
            return sync(result);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 异步 get：命中更新统计；miss 且 read-through 时异步 loadValue。
     * atomic 模式走 {@link #getValue}，否则 {@link #getValueLocked}。
     */
    @Override
    public RFuture<V> getAsync(K key) {
        checkNotClosed();
        checkKey(key);

        long startTime = currentNanoTime();
        RFuture<V> future;
        if (atomicExecution) {
            future = getValue(key);
        } else {
            V value = getValueLocked(key);
            future = new CompletableFutureWrapper<>(value);
        }

        CompletableFuture<V> result = new CompletableFuture<>();
        future.whenComplete((value, e) -> {
            if (e != null) {
                result.completeExceptionally(new CacheException(e));
                return;
            }

            if (value == null) {
                cacheManager.getStatBean(this).addMisses(1);
                if (config.isReadThrough()) {
                    redisson.getServiceManager().getExecutor().execute(() -> {
                        try {
                            V val = loadValue(key);
                            result.complete(val);
                        } catch (CacheException ex) {
                            result.completeExceptionally(ex);
                        } catch (Exception ex) {
                            result.completeExceptionally(new CacheException(ex));
                        }
                    });
                    return;
                }
            } else {
                cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                cacheManager.getStatBean(this).addHits(1);
            }
            result.complete(value);
        });
        return new CompletableFutureWrapper<>(result);
    }

    /** TCK 模式：Lua 读 value 并处理 access 过期/刷新，必要时 waitSync。 */
    V getValueLocked(K key) {

        V value = evalWrite(getRawName(), codec, RedisCommands.EVAL_MAP_VALUE,
                "local value = redis.call('hget', KEYS[1], ARGV[3]); "
              + "if value == false then "
                  + "return nil; "
              + "end; "

              + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[3]); "
              + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[2]) then "
                  + "return nil; "
              + "end; "
              + "return value; ",
              Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getRemovedChannelName()),
              0, System.currentTimeMillis(), encodeMapKey(key));

        if (value != null) {
            Long accessTimeout = getAccessTimeout();
            if (accessTimeout == -1) {
                return value;
            }

            List<Object> result = new ArrayList<Object>(3);
            result.add(value);
            double syncId = ThreadLocalRandom.current().nextDouble();
            Long syncs = evalWrite(getRawName(), codec, RedisCommands.EVAL_LONG,
                "if ARGV[1] == '0' then "
                  + "redis.call('hdel', KEYS[1], ARGV[3]); "
                  + "redis.call('zrem', KEYS[2], ARGV[3]); "
                  + "local value = redis.call('hget', KEYS[1], ARGV[3]); "
                  + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value)); "
                  + "redis.call('publish', KEYS[3], msg); "
                  + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value), ARGV[4]); "
                  + "local syncs = redis.call('publish', KEYS[4], syncMsg); "
                  + "return syncs;"
              + "elseif ARGV[1] ~= '-1' then "
                  + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[3]); "
                  + "return 0;"
              + "end; ",
             Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getRemovedChannelName(),
                     getRemovedSyncChannelName()),
             accessTimeout, System.currentTimeMillis(), encodeMapKey(key), syncId);

            result.add(syncs);
            result.add(syncId);

            waitSync(result);
            return value;
        }

        return value;
    }

    /** 原子模式：异步 Lua 读取 entry，按 ExpiryPolicy 处理 access 超时。 */
    RFuture<V> getValue(K key) {
        Long accessTimeout = getAccessTimeout();

        if (accessTimeout == -1) {
            String name = getRawName(key);
            return commandExecutor.evalReadAsync(name, codec, RedisCommands.EVAL_MAP_VALUE,
                    "local value = redis.call('hget', KEYS[1], ARGV[3]); "
                  + "if value == false then "
                      + "return nil; "
                  + "end; "

                  + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[3]); "
                  + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[2]) then "
                      + "return nil; "
                  + "end; "

                  + "return value; ",
                 Arrays.asList(name, getTimeoutSetName(name)),
                 accessTimeout, System.currentTimeMillis(), encodeMapKey(key));
        }

        String name = getRawName(key);
        return commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_MAP_VALUE,
                "local value = redis.call('hget', KEYS[1], ARGV[3]); "
              + "if value == false then "
                  + "return nil; "
              + "end; "

              + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[3]); "
              + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[2]) then "
                  + "return nil; "
              + "end; "

              + "if ARGV[1] == '0' then "
                  + "redis.call('hdel', KEYS[1], ARGV[3]); "
                  + "redis.call('zrem', KEYS[2], ARGV[3]); "
                  + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value)); "
                  + "redis.call('publish', KEYS[3], msg); "
              + "elseif ARGV[1] ~= '-1' then "
                  + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[3]); "
              + "end; "

              + "return value; ",
             Arrays.<Object>asList(name, getTimeoutSetName(name), getRemovedChannelName(name)),
             accessTimeout, System.currentTimeMillis(), encodeMapKey(key));
    }

    /** 根据 ExpiryPolicy 计算 access 过期时间戳；无策略或永不过期为 -1。 */
    Long getAccessTimeout(long baseTime) {
        if (config.getExpiryPolicy().getExpiryForAccess() == null) {
            return -1L;
        }
        long accessTimeout = config.getExpiryPolicy().getExpiryForAccess().getAdjustedTime(baseTime);

        if (config.getExpiryPolicy().getExpiryForAccess().isZero()) {
            accessTimeout = 0L;
        } else if (accessTimeout == Long.MAX_VALUE) {
            accessTimeout = -1L;
        }
        return accessTimeout;
    }

    /** 以当前时间为基准的 access 超时时间戳。 */
    Long getAccessTimeout() {
        return getAccessTimeout(System.currentTimeMillis());
    }

    /** 批量 CacheLoader.loadAll 并 putAll，更新 get 统计。 */
    Map<K, V> loadValues(Iterable<? extends K> keys) {
        Map<K, V> loaded;
        try {
            loaded = cacheLoader.loadAll(keys);
        } catch (Exception ex) {
            throw new CacheLoaderException(ex);
        }
        if (loaded != null) {
            loaded = loaded.entrySet()
                    .stream()
                    .filter(e -> e.getValue() != null)
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

            long startTime = currentNanoTime();
            putAllValues(loaded);
            cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
        }
        return loaded;
    }

    /** read-through：CacheLoader.load 单 key 并写入 Cache。 */
    V loadValue(K key) {
        V value = null;
        try {
            value = cacheLoader.load(key);
        } catch (Exception ex) {
            throw new CacheLoaderException(ex);
        }
        if (value != null) {
            long startTime = currentNanoTime();
            if (atomicExecution) {
                putValue(key, value);
            } else {
                putValueLocked(key, value);
            }
            cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
        }
        return value;
    }

    /** 同步 write 命令封装，异常转为 CacheException。 */
    <T, R> R write(String key, RedisCommand<T> command, Object... params) {
        RFuture<R> future = commandExecutor.writeAsync(key, command, params);
        try {
            return get(future);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    /** 同步 evalWrite Lua 脚本。 */
    <T, R> R evalWrite(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params) {
        RFuture<R> future = commandExecutor.evalWriteAsync(key, codec, evalCommandType, script, keys, params);
        try {
            return get(future);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    /** 同步 evalRead Lua 脚本。 */
    <T, R> R evalRead(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params) {
        RFuture<R> future = commandExecutor.evalReadAsync(key, codec, evalCommandType, script, keys, params);
        try {
            return get(future);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    /** TCK 模式：Lua 写入/更新 entry，发布事件并 waitSync。 */
    private boolean putValueLocked(K key, Object value) {
        double syncId = ThreadLocalRandom.current().nextDouble();

        if (containsKey(key)) {
            Long updateTimeout = getUpdateTimeout();
            List<Object> res = evalWrite(getRawName(), codec, RedisCommands.EVAL_LIST,
                    "if ARGV[2] == '0' then "
                          + "redis.call('hdel', KEYS[1], ARGV[4]); "
                          + "redis.call('zrem', KEYS[2], ARGV[4]); "
                          + "local value = redis.call('hget', KEYS[1], ARGV[4]);"
                          + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                          + "redis.call('publish', KEYS[4], msg); "
                          + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value), ARGV[6]); "
                          + "local syncs = redis.call('publish', KEYS[7], syncMsg); "
                          + "return {0, syncs};"
                      + "elseif ARGV[2] ~= '-1' then "
                          + "local oldValueRequired = tonumber(redis.call('get', KEYS[9])); "
                          + "local msg, syncMsg; "
                          + "if oldValueRequired == nil or oldValueRequired < 1 then "
                              + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                              + "redis.call('zadd', KEYS[2], ARGV[2], ARGV[4]); "
                              + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1); "
                              + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1, ARGV[6]); "
                          + "else "
                              + "local value = redis.call('hget', KEYS[1], ARGV[4]);"
                              + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                              + "redis.call('zadd', KEYS[2], ARGV[2], ARGV[4]); "
                              + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value)); "
                              + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value), ARGV[6]); "
                          + "end; "
                          + "redis.call('publish', KEYS[5], msg); "
                          + "local syncs = redis.call('publish', KEYS[8], syncMsg); "
                          + "return {1, syncs};"
                      + "else "
                          + "local oldValueRequired = tonumber(redis.call('get', KEYS[9])); "
                          + "local msg, syncMsg; "
                          + "if oldValueRequired == nil or oldValueRequired < 1 then "
                              + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                              + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1); "
                              + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1, ARGV[6]); "
                          + "else "
                              + "local value = redis.call('hget', KEYS[1], ARGV[4]);"
                              + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                              + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value)); "
                              + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value), ARGV[6]); "
                          + "end; "
                          + "redis.call('publish', KEYS[5], msg); "
                          + "local syncs = redis.call('publish', KEYS[8], syncMsg); "
                          + "return {1, syncs};"
                      + "end; ",
                 Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getCreatedChannelName(), getRemovedChannelName(), getUpdatedChannelName(),
                         getCreatedSyncChannelName(), getRemovedSyncChannelName(), getUpdatedSyncChannelName(), getOldValueListenerCounter()),
                 0, updateTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value), syncId);

            res.add(syncId);
            waitSync(res);

            return (Long) res.get(0) == 1;
        }

        Long creationTimeout = getCreationTimeout();
        if (creationTimeout == 0) {
            return false;
        }
        List<Object> res = evalWrite(getRawName(), codec, RedisCommands.EVAL_LIST,
                    "if ARGV[1] ~= '-1' then "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                      + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[4]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5]); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], ARGV[6]); "
                      + "local syncs = redis.call('publish', KEYS[6], syncMsg); "
                      + "return {1, syncs};"
                  + "else "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5]); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], ARGV[6]); "
                      + "local syncs = redis.call('publish', KEYS[6], syncMsg); "
                      + "return {1, syncs};"
                  + "end; ",
             Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getCreatedChannelName(), getRemovedChannelName(), getUpdatedChannelName(),
                     getCreatedSyncChannelName(), getRemovedSyncChannelName(), getUpdatedSyncChannelName()),
             creationTimeout, 0, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value), syncId);

        res.add(syncId);
        waitSync(res);

        return (Long) res.get(0) == 1;

    }


    /** 原子模式批量 put，返回新增/更新计数。 */
    RFuture<Long> putAllValues(Map<? extends K, ? extends V> map) {
        double syncId = ThreadLocalRandom.current().nextDouble();
        RFuture<List<Object>> res = putAllOperation(commandExecutor, syncId, null, getRawName(), map);

        RFuture<Long> result = handlePutAllResult(syncId, res);
        return result;
    }

    /** 解析 putAll Lua 结果并在有 sync 时 acquire 信号量。 */
    RFuture<Long> handlePutAllResult(double syncId, CompletionStage<List<Object>> res) {
        if (atomicExecution) {
            CompletionStage<Long> f = res.thenCompose(r -> {
                Long added = (Long) r.get(0);
                Long syncs = (Long) r.get(1);
                if (syncs > 0) {
                    RSemaphore semaphore = redisson.getSemaphore(getSyncName(syncId));
                    return semaphore.acquireAsync(syncs.intValue()).thenCompose(obj1 -> {
                        return semaphore.deleteAsync().thenApply(obj -> added);
                    });
                }
                return CompletableFuture.completedFuture(added);
            });
            f = handleException(f);
            return new CompletableFutureWrapper<>(f);
        } else {
            List<Object> r = res.toCompletableFuture().join();

            Long added = (Long) r.get(0);
            Long syncs = (Long) r.get(1);
            if (syncs > 0) {
                RSemaphore semaphore = redisson.getSemaphore(getSyncName(syncId));
                try {
                    semaphore.acquire(syncs.intValue());
                    semaphore.delete();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            return new CompletableFutureWrapper<>(added);
        }
    }

    /** putAll 核心 Lua：逐 key 判断存在/过期并 hset/zadd/发布事件。 */
    RFuture<List<Object>> putAllOperation(CommandAsyncExecutor commandExecutor, double syncId,
                                          MasterSlaveEntry msEntry, String name, Map<? extends K, ? extends V> map) {
        Long creationTimeout = getCreationTimeout();
        Long updateTimeout = getUpdateTimeout();

        List<Object> params = new ArrayList<>();
        params.add(creationTimeout);
        params.add(updateTimeout);
        params.add(System.currentTimeMillis());
        params.add(syncId);

        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            params.add(encodeMapKey(entry.getKey()));
            params.add(encodeMapValue(entry.getValue()));
        }

        String script = "local added = 0; "
            + "local syncs = 0; "
            + "for i = 5, #ARGV, 2 do " +
                "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[i]);" +
                "local exists = redis.call('hexists', KEYS[1], ARGV[i]) == 1;" +
                "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[3]) then " +
                    "exists = false;" +
                "end;" +
                "if exists then "
                + "if ARGV[2] == '0' then "
                    + "redis.call('hdel', KEYS[1], ARGV[i]); "
                    + "redis.call('zrem', KEYS[2], ARGV[i]); "
                    + "local value = redis.call('hget', KEYS[1], ARGV[i]);"
                    + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[i]), ARGV[i], string.len(tostring(value)), tostring(value)); "
                    + "redis.call('publish', KEYS[4], msg); "
                    + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[i]), ARGV[i], string.len(tostring(value)), tostring(value), ARGV[4]); "
                    + "syncs = syncs + redis.call('publish', KEYS[7], syncMsg); "
                + "elseif ARGV[2] ~= '-1' then "
                    + "local oldValueRequired = tonumber(redis.call('get', KEYS[9])); "
                    + "local msg, syncMsg; "
                    + "if oldValueRequired == nil or oldValueRequired < 1 then "
                        + "redis.call('hset', KEYS[1], ARGV[i], ARGV[i+1]); "
                        + "redis.call('zadd', KEYS[2], ARGV[2], ARGV[i]); "
                        + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1], -1); "
                        + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1], -1, ARGV[4]); "
                    + "else "
                        + "local value = redis.call('hget', KEYS[1], ARGV[i]);"
                        + "redis.call('hset', KEYS[1], ARGV[i], ARGV[i+1]); "
                        + "redis.call('zadd', KEYS[2], ARGV[2], ARGV[i]); "
                        + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1], string.len(tostring(value)), tostring(value)); "
                        + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1], string.len(tostring(value)), tostring(value), ARGV[4]); "
                    + "end; "
                    + "redis.call('publish', KEYS[5], msg); "
                    + "syncs = syncs + redis.call('publish', KEYS[8], syncMsg); "
                    + "added = added + 1;"
                + "else "
                    + "local oldValueRequired = tonumber(redis.call('get', KEYS[9])); "
                    + "local msg, syncMsg; "
                    + "if oldValueRequired == nil or oldValueRequired < 1 then "
                        + "redis.call('hset', KEYS[1], ARGV[i], ARGV[i+1]); "
                        + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1], -1); "
                        + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1], -1, ARGV[4]); "
                    + "else "
                        + "local value = redis.call('hget', KEYS[1], ARGV[i]);"
                        + "redis.call('hset', KEYS[1], ARGV[i], ARGV[i+1]); "
                        + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1], string.len(tostring(value)), tostring(value)); "
                        + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1], string.len(tostring(value)), tostring(value), ARGV[4]); "
                    + "end; "
                    + "redis.call('publish', KEYS[5], msg); "
                    + "syncs = syncs + redis.call('publish', KEYS[8], syncMsg); "
                    + "added = added + 1;"
                + "end; "
            + "else "
                + "if ARGV[1] == '0' then "
                + "elseif ARGV[1] ~= '-1' then "
                    + "redis.call('hset', KEYS[1], ARGV[i], ARGV[i+1]); "
                    + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[i]); "
                    + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1]); "
                    + "redis.call('publish', KEYS[3], msg); "
                    + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1], ARGV[4]); "
                    + "syncs = syncs + redis.call('publish', KEYS[6], syncMsg); "
                    + "added = added + 1;"
                + "else "
                    + "redis.call('hset', KEYS[1], ARGV[i], ARGV[i+1]); "
                    + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1]); "
                    + "redis.call('publish', KEYS[3], msg); "
                    + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[i]), ARGV[i], string.len(ARGV[i+1]), ARGV[i+1], ARGV[4]); "
                    + "syncs = syncs + redis.call('publish', KEYS[6], syncMsg); "
                    + "added = added + 1;"
                + "end; "
            + "end; "
          + "end; "
          + "return {added, syncs};";

        if (msEntry == null) {
            return commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_LIST, script,
                    Arrays.asList(name, getTimeoutSetName(name), getCreatedChannelName(name), getRemovedChannelName(name), getUpdatedChannelName(name),
                            getCreatedSyncChannelName(name), getRemovedSyncChannelName(name), getUpdatedSyncChannelName(name), getOldValueListenerCounter(name)),
                    params.toArray());
        }

        return commandExecutor.evalWriteAsync(msEntry, codec, RedisCommands.EVAL_LIST, script,
                    Arrays.asList(name, getTimeoutSetName(name), getCreatedChannelName(name), getRemovedChannelName(name), getUpdatedChannelName(name),
                            getCreatedSyncChannelName(name), getRemovedSyncChannelName(name), getUpdatedSyncChannelName(name), getOldValueListenerCounter(name)),
                    params.toArray());
    }

    /** 原子模式单 key put，返回是否实际写入（非仅 touch）。 */
    RFuture<Boolean> putValue(K key, Object value) {
        double syncId = ThreadLocalRandom.current().nextDouble();
        Long creationTimeout = getCreationTimeout();
        Long updateTimeout = getUpdateTimeout();

        String name = getRawName(key);
        RFuture<List<Object>> res = commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_LIST,
                "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[4]);" +
                "local exists = redis.call('hexists', KEYS[1], ARGV[4]) == 1;" +
                "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[3]) then " +
                    "exists = false;" +
                "end;" +
                "if exists then "
                  + "if ARGV[2] == '0' then "
                      + "redis.call('hdel', KEYS[1], ARGV[4]); "
                      + "redis.call('zrem', KEYS[2], ARGV[4]); "
                      + "local value = redis.call('hget', KEYS[1], ARGV[4]);"
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                      + "redis.call('publish', KEYS[4], msg); "
                      + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value), ARGV[6]); "
                      + "local syncs = redis.call('publish', KEYS[7], syncMsg); "
                      + "return {0, syncs};"
                  + "elseif ARGV[2] ~= '-1' then "
                      + "local oldValueRequired = tonumber(redis.call('get', KEYS[9])); "
                      + "local msg, syncMsg; "
                      + "if oldValueRequired == nil or oldValueRequired < 1 then "
                          + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                          + "redis.call('zadd', KEYS[2], ARGV[2], ARGV[4]); "
                          + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1); "
                          + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1, ARGV[6]); "
                      + "else "
                          + "local value = redis.call('hget', KEYS[1], ARGV[4]);"
                          + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                          + "redis.call('zadd', KEYS[2], ARGV[2], ARGV[4]); "
                          + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value)); "
                          + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value), ARGV[6]); "
                      + "end; "
                      + "redis.call('publish', KEYS[5], msg); "
                      + "local syncs = redis.call('publish', KEYS[8], syncMsg); "
                      + "return {1, syncs};"
                  + "else "
                      + "local oldValueRequired = tonumber(redis.call('get', KEYS[9])); "
                      + "local msg, syncMsg; "
                      + "if oldValueRequired == nil or oldValueRequired < 1 then "
                          + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                          + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1); "
                          + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1, ARGV[6]); "
                      + "else "
                          + "local value = redis.call('hget', KEYS[1], ARGV[4]);"
                          + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                          + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value)); "
                          + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value), ARGV[6]); "
                      + "end; "
                      + "redis.call('publish', KEYS[5], msg); "
                      + "local syncs = redis.call('publish', KEYS[8], syncMsg); "
                      + "return {1, syncs};"
                  + "end; "
              + "else "
                  + "if ARGV[1] == '0' then "
                      + "return {0};"
                  + "elseif ARGV[1] ~= '-1' then "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                      + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[4]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5]); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], ARGV[6]); "
                      + "local syncs = redis.call('publish', KEYS[6], syncMsg); "
                      + "return {1, syncs};"
                  + "else "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5]); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], ARGV[6]); "
                      + "local syncs = redis.call('publish', KEYS[6], syncMsg); "
                      + "return {1, syncs};"
                  + "end; "
              + "end; ",
             Arrays.asList(name, getTimeoutSetName(name), getCreatedChannelName(name), getRemovedChannelName(name), getUpdatedChannelName(name),
                     getCreatedSyncChannelName(name), getRemovedSyncChannelName(name), getUpdatedSyncChannelName(name), getOldValueListenerCounter(name)),
             creationTimeout, updateTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value), syncId);

        RFuture<Boolean> result = waitSync(syncId, res);

        return result;
    }

    /** 异步等待同步监听器 ack（RSemaphore acquire）。 */
    RFuture<Boolean> waitSync(double syncId, RFuture<List<Object>> res) {
        if (atomicExecution) {
            CompletionStage<Boolean> f = res.thenCompose(r -> {
                r.add(syncId);

                if (r.size() < 2) {
                    return CompletableFuture.completedFuture((Long) r.get(0) >= 1);
                }

                Long syncs = (Long) r.get(r.size() - 2);
                if (syncs != null && syncs > 0) {
                    RSemaphore semaphore = redisson.getSemaphore(getSyncName(syncId));
                    return semaphore.acquireAsync(syncs.intValue()).thenCompose(obj1 -> {
                        return semaphore.deleteAsync().thenApply(obj -> (Long) r.get(0) >= 1);
                    });
                } else {
                    return CompletableFuture.completedFuture((Long) r.get(0) >= 1);
                }
            });
            f = handleException(f);
            return new CompletableFutureWrapper<>(f);
        } else {
            List<Object> r = res.toCompletableFuture().join();

            r.add(syncId);
            waitSync(r);
            return new CompletableFutureWrapper<>((Long) r.get(0) >= 1);
        }
    }

    /** 计算 update 过期时间戳。 */
    Long getUpdateTimeout(long baseTime) {
        if (config.getExpiryPolicy().getExpiryForUpdate() == null) {
            return -1L;
        }

        long updateTimeout = config.getExpiryPolicy().getExpiryForUpdate().getAdjustedTime(baseTime);
        if (config.getExpiryPolicy().getExpiryForUpdate().isZero()) {
            updateTimeout = 0L;
        } else if (updateTimeout == Long.MAX_VALUE) {
            updateTimeout = -1L;
        }
        return updateTimeout;
    }

    /** 当前时刻的 update 过期时间戳。 */
    Long getUpdateTimeout() {
        return getUpdateTimeout(System.currentTimeMillis());
    }

    /** 计算 creation 过期时间戳；零时长映射为 0。 */
    Long getCreationTimeout(long baseTime) {
        if (config.getExpiryPolicy().getExpiryForCreation() == null) {
            return -1L;
        }
        long creationTimeout = config.getExpiryPolicy().getExpiryForCreation().getAdjustedTime(baseTime);
        if (config.getExpiryPolicy().getExpiryForCreation().isZero()) {
            creationTimeout = 0L;
        } else if (creationTimeout == Long.MAX_VALUE) {
            creationTimeout = -1L;
        }
        return creationTimeout;
    }

    /** 当前时刻的 creation 过期时间戳。 */
    Long getCreationTimeout() {
        return getCreationTimeout(System.currentTimeMillis());
    }

    /** 原子模式 putIfAbsent Lua。 */
    RFuture<Boolean> putIfAbsentValue(K key, Object value) {
        Long creationTimeout = getCreationTimeout();
        if (creationTimeout == 0) {
            return new CompletableFutureWrapper<>(false);
        }

        String name = getRawName(key);
        return commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_BOOLEAN,
                "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[2]);" +
                "local exists = redis.call('hexists', KEYS[1], ARGV[2]) == 1;" +
                "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[4]) then " +
                    "exists = false;" +
                "end;" +
                "if exists then "
                  + "return 0; "
              + "else "
                  + "if ARGV[1] ~= '-1' then "
                      + "redis.call('hset', KEYS[1], ARGV[2], ARGV[3]); "
                      + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[2]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[2]), ARGV[2], string.len(ARGV[3]), ARGV[3]); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "return 1;"
                  + "else "
                      + "redis.call('hset', KEYS[1], ARGV[2], ARGV[3]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[2]), ARGV[2], string.len(ARGV[3]), ARGV[3]); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "return 1;"
                  + "end; "
              + "end; ",
             Arrays.asList(name, getTimeoutSetName(name), getCreatedChannelName(name)),
             creationTimeout, encodeMapKey(key), encodeMapValue(value), System.currentTimeMillis());
    }

    /** TCK 模式 putIfAbsent。 */
    private boolean putIfAbsentValueLocked(K key, Object value) {
        if (containsKey(key)) {
            return false;
        }

        Long creationTimeout = getCreationTimeout();
        if (creationTimeout == 0) {
            return false;
        }
        return evalWrite(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                    "if ARGV[1] ~= '-1' then "
                      + "redis.call('hset', KEYS[1], ARGV[2], ARGV[3]); "
                      + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[2]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[2]), ARGV[2], string.len(ARGV[3]), ARGV[3]); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "return 1;"
                  + "else "
                      + "redis.call('hset', KEYS[1], ARGV[2], ARGV[3]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[2]), ARGV[2], string.len(ARGV[3]), ARGV[3]); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "return 1;"
                  + "end; ",
             Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getCreatedChannelName()),
             creationTimeout, encodeMapKey(key), encodeMapValue(value));
    }

    /**  per-key 分布式锁 Redis 键名（hash128 + rawName）。 */
    private String getLockName(Object key) {
        ByteBuf keyState = encodeMapKey(key);
        try {
            return "{" + getRawName() + "}:" + Hash.hash128toBase64(keyState) + ":key";
        } finally {
            keyState.release();
        }
    }

    /** 同步 getAll：非 atomic 且无 read-through 时可短路空结果。 */
    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        checkNotClosed();
        if (keys == null) {
            throw new NullPointerException();
        }
        for (K key : keys) {
            checkKey(key);
        }

        if (!atomicExecution && !config.isReadThrough()) {
            long startTime = currentNanoTime();
            boolean exists = false;
            for (K key : keys) {
                if (containsKey(key)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                return Collections.emptyMap();
            }
        }

        RFuture<Map<K, V>> result = getAllAsync(keys);
        return sync(result);
    }

    /** 异步 hmget + 过期过滤；miss 键可 read-through 批量加载。 */
    @Override
    public RFuture<Map<K, V>> getAllAsync(Set<? extends K> keys) {
        checkNotClosed();
        long startTime = currentNanoTime();
        Long accessTimeout = getAccessTimeout();

        List<Object> args = new ArrayList<>(keys.size() + 2);
        args.add(accessTimeout);
        args.add(System.currentTimeMillis());
        encodeMapKeys(args, keys);

        RFuture<Map<K, V>> res = getAllOperation(commandExecutor, getRawName(), null, new ArrayList<>(keys), accessTimeout, args);

        return handleGetAllResult(startTime, res);
    }

    /** getAll Lua：分批 hmget、过期判定与 access 刷新/删除。 */
    RFuture<Map<K, V>> getAllOperation(CommandAsyncExecutor commandExecutor, String name, MasterSlaveEntry entry, List<Object> keys, Long accessTimeout, List<Object> args) {
        String script;
        if (accessTimeout == -1) {
            script = "local expireHead = redis.call('zrange', KEYS[2], 0, 0, 'withscores');"
                  + "local accessTimeout = ARGV[1]; "
                  + "local currentTime = tonumber(ARGV[2]); "
                  + "local hasExpire = #expireHead == 2 and tonumber(expireHead[2]) <= currentTime; "
                  + "local map = {};"
                  + "for i=3, #ARGV, 5000 do "
                     + "local m = redis.call('hmget', KEYS[1], unpack(ARGV, i, math.min(i+4999, #ARGV))); "
                     + "for k,v in ipairs(m) do "
                     +     "table.insert(map, v) "
                     + "end; "
                  + "end; "
                  + "local result = {};"
                  + "for i, value in ipairs(map) do "
                      + "if value ~= false then "
                          + "local key = ARGV[i+2]; "

                          + "if hasExpire then "
                              + "local expireDateScore = redis.call('zscore', KEYS[2], key); "
                              + "if expireDateScore ~= false and tonumber(expireDateScore) <= currentTime then "
                                  + "value = false; "
                              + "end; "
                          + "end; "
                      + "end; "

                      + "table.insert(result, value); "
                  + "end; "
                  + "return result;";
        } else {
            script = "local expireHead = redis.call('zrange', KEYS[2], 0, 0, 'withscores');"
                          + "local accessTimeout = ARGV[1]; "
                          + "local currentTime = tonumber(ARGV[2]); "
                          + "local hasExpire = #expireHead == 2 and tonumber(expireHead[2]) <= currentTime; "
                          + "local map = {};"
                          + "for i=3, #ARGV, 5000 do "
                             + "local m = redis.call('hmget', KEYS[1], unpack(ARGV, i, math.min(i+4999, #ARGV))); "
                             + "for k,v in ipairs(m) do "
                             +     "table.insert(map, v) "
                             + "end; "
                          + "end; "

                          + "local result = {};"
                          + "for i, value in ipairs(map) do "
                              + "if value ~= false then "
                                  + "local key = ARGV[i+2]; "

                                  + "if hasExpire then "
                                      + "local expireDateScore = redis.call('zscore', KEYS[2], key); "
                                      + "if expireDateScore ~= false and tonumber(expireDateScore) <= currentTime then "
                                          + "value = false; "
                                      + "end; "
                                  + "end; "

                                  + "if accessTimeout == '0' then "
                                      + "redis.call('hdel', KEYS[1], key); "
                                      + "redis.call('zrem', KEYS[2], key); "
                                      + "local msg = struct.pack('Lc0Lc0', string.len(key), key, string.len(value), value); "
                                      + "redis.call('publish', KEYS[3], msg); "
                                  + "elseif accessTimeout ~= '-1' then "
                                      + "redis.call('zadd', KEYS[2], accessTimeout, key); "
                                  + "end; "
                              + "end; "

                              + "table.insert(result, value); "
                          + "end; "
                          + "return result;";
        }

        if (entry == null) {
            return commandExecutor.evalReadAsync(name, codec, new RedisCommand<Map<Object, Object>>("EVAL",
                                new MapValueDecoder(new MapGetAllDecoder(keys, 0, true))),
                        script, Arrays.asList(name, getTimeoutSetName(name), getRemovedChannelName(name)), args.toArray());
        }
        return commandExecutor.evalReadAsync(entry, codec, new RedisCommand<Map<Object, Object>>("EVAL",
                            new MapValueDecoder(new MapGetAllDecoder(keys, 0, true))),
                    script, Arrays.asList(name, getTimeoutSetName(name), getRemovedChannelName(name)), args.toArray());
    }

    /** 汇总 getAll 结果、统计 hit/miss 并触发 read-through。 */
    RFuture<Map<K, V>> handleGetAllResult(long startTime, RFuture<Map<K, V>> res) {
        CompletableFuture<Map<K, V>> result = new CompletableFuture<>();
        res.whenComplete((r, ex) -> {
            if (ex != null) {
                result.completeExceptionally(new CacheException(ex));
                return;
            }

            Map<K, V> notNullEntries = r.entrySet().stream()
                                .filter(e -> e.getValue() != null)
                                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));

            cacheManager.getStatBean(this).addHits(notNullEntries.size());

            int nullValues = r.size() - notNullEntries.size();
            if (config.isReadThrough() && nullValues > 0) {
                cacheManager.getStatBean(this).addMisses(nullValues);
                commandExecutor.getServiceManager().getExecutor().execute(() -> {
                    try {
                        Set<K> nullKeys = r.entrySet().stream()
                                .filter(e -> e.getValue() == null)
                                .map(e -> e.getKey())
                                .collect(Collectors.toSet());

                        Map<K, V> loadedMap = loadValues(nullKeys);
                        notNullEntries.putAll(loadedMap);
                    } catch (Exception exc) {
                        result.completeExceptionally(exc);
                        return;
                    }
                    cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);

                    result.complete(notNullEntries);
                });
            } else {
                cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                result.complete(notNullEntries);
            }
        });
        return new CompletableFutureWrapper<>(result);
    }

    /** 同步 containsKey。 */
    @Override
    public boolean containsKey(K key) {
        RFuture<Boolean> future = containsKeyAsync(key);
        return sync(future);
    }

    /** 异步 hexists + 过期 ZSCORE 检查。 */
    @Override
    public RFuture<Boolean> containsKeyAsync(K key) {
        checkNotClosed();
        checkKey(key);

        String name = getRawName(key);
        CompletionStage<Boolean> future = commandExecutor.evalReadAsync(name, codec, RedisCommands.EVAL_BOOLEAN,
                  "if redis.call('hexists', KEYS[1], ARGV[2]) == 0 then "
                    + "return 0;"
                + "end;"

                + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[2]); "
                + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[1]) then "
                    + "return 0; "
                + "end; "
                + "return 1;",
             Arrays.<Object>asList(name, getTimeoutSetName(name)),
             System.currentTimeMillis(), encodeMapKey(key));
        future = handleException(future);
        return new CompletableFutureWrapper<>(future);
    }

    /**
     * 异步批量 load：对每个 key 加锁，不存在或 replace 时 CacheLoader.load。
     * 完成后回调 completionListener。
     */
    @Override
    @SuppressWarnings("NestedTryDepth")
    public void loadAll(final Set<? extends K> keys, final boolean replaceExistingValues, final CompletionListener completionListener) {
        checkNotClosed();
        if (keys == null) {
            throw new NullPointerException();
        }

        for (K key : keys) {
            checkKey(key);
        }

        if (cacheLoader == null) {
            if (completionListener != null) {
                completionListener.onCompletion();
            }
            return;
        }

        commandExecutor.getServiceManager().getExecutor().execute(() -> {
            for (K key : keys) {
                try {
                    if (!containsKey(key) || replaceExistingValues) {
                        RLock lock = getLockedLock(key);
                        try {
                            if (!containsKey(key)|| replaceExistingValues) {
                                V value;
                                try {
                                    value = cacheLoader.load(key);
                                } catch (Exception ex) {
                                    throw new CacheLoaderException(ex);
                                }
                                if (value != null) {
                                    if (atomicExecution) {
                                        putValue(key, value);
                                    } else {
                                        putValueLocked(key, value);
                                    }
                                }
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                } catch (Exception e) {
                    if (completionListener != null) {
                        completionListener.onException(e);
                    }
                    throw e;
                }
            }
            if (completionListener != null) {
                completionListener.onCompletion();
            }
        });
    }

    /** atomic 模式返回 DUMMY_LOCK；否则对 key 加 Redisson 锁。 */
    RLock getLockedLock(K key) {
        if (atomicExecution) {
            /* 原子模式下无需加锁。 */
            return ServiceManager.DUMMY_LOCK;
        }

        String lockName = getLockName(key);
        RLock lock = redisson.getLock(lockName);
        try {
            lock.lock();
        } catch (Exception e) {
            throw new CacheException(e);
        }
        return lock;
    }

    /** 异步 put：区分 write-through（getAndPut + CacheWriter）与普通 put。 */
    @Override
    public RFuture<Void> putAsync(K key, V value) {
        checkNotClosed();
        checkKey(key);
        if (value == null) {
            throw new NullPointerException();
        }

        long startTime = currentNanoTime();
        if (config.isWriteThrough()) {
            RLock lock = getLockedLock(key);
            try {
                CompletionStage<List<Object>> future;
                if (atomicExecution) {
                    future = getAndPutValue(key, value);
                } else {
                    List<Object> res = getAndPutValueLocked(key, value);
                    future = new CompletableFutureWrapper<>(res);
                }
                CompletionStage<Void> f = future.thenCompose(res -> {
                    if (res.isEmpty()) {
                        cacheManager.getStatBean(this).addPuts(1);
                        cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                        return CompletableFuture.completedFuture(null);
                    }
                    Long added = (Long) res.get(0);
                    if (added == null) {
                        cacheManager.getStatBean(this).addPuts(1);
                        cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                        return CompletableFuture.completedFuture(null);
                    }

                    return writeCache(key, value, startTime, res, added);
                });
                f = handleException(f);
                return new CompletableFutureWrapper<>(f);
            } finally {
                lock.unlock();
            }
        } else {
            RLock lock = getLockedLock(key);
            try {
                RFuture<Boolean> future;
                if (atomicExecution) {
                    future = putValue(key, value);
                } else {
                    boolean res = putValueLocked(key, value);
                    future = new CompletableFutureWrapper<>(res);
                }
                CompletionStage<Void> f = future.handle((r, e) -> {
                    if (e != null) {
                        throw new CompletionException(new CacheException(e));
                    }

                    if (r) {
                        cacheManager.getStatBean(this).addPuts(1);
                    }
                    cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                    return null;
                });
                return new CompletableFutureWrapper<>(f);
            } finally {
                lock.unlock();
            }
        }
    }

    /** write-through 回调：调用 CacheWriter.write/delete，失败时回滚 Redis。 */
    private CompletableFuture<Void> writeCache(K key, V value, long startTime, List<Object> res, Long added) {
        Runnable r;
        CompletableFuture<Void> result = new CompletableFuture<>();
        if (added >= 1) {
            r = () -> {
                try {
                    cacheWriter.write(new JCacheEntry<K, V>(key, value));
                    cacheManager.getStatBean(this).addPuts(1);
                    cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                    result.complete(null);
                } catch (Exception e) {
                    removeValues(key).whenComplete((obj, exc) -> {
                        if (exc != null) {
                            result.completeExceptionally(new CacheWriterException(exc));
                            return;
                        }

                        handleException(result, e);
                    });
                }
            };
        } else {
            r = () -> {
                try {
                    cacheWriter.delete(key);
                    cacheManager.getStatBean(this).addPuts(1);
                    cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                    result.complete(null);
                } catch (Exception e) {
                    if (res.size() == 4 && res.get(1) != null) {
                        putValue(key, res.get(1)).whenComplete((obj, exc) -> {
                            if (exc != null) {
                                result.completeExceptionally(new CacheWriterException(exc));
                                return;
                            }

                            handleException(result, e);
                        });
                        return;
                    }

                    handleException(result, e);
                }
            };
        }

        if (atomicExecution) {
            commandExecutor.getServiceManager().getExecutor().execute(r);
        } else {
            r.run();
        }
        return result;
    }

    /** 同步 put。 */
    @Override
    public void put(K key, V value) {
        RFuture<Void> future = putAsync(key, value);
        sync(future);
    }

    /** 批量删除 key 并发布 REMOVED 事件，返回实际删除数。 */
    RFuture<Long> removeValues(Object... keys) {
        List<Object> params = new ArrayList<>(keys.length + 1);
        params.add(System.currentTimeMillis());
        encodeMapKeys(params, Arrays.asList(keys));
        return removeValuesOperation(commandExecutor, getRawName(), null, params, null);
    }

    /** removeValues 的 Lua 实现。 */
    RFuture<Long> removeValuesOperation(CommandAsyncExecutor commandExecutor, String name, MasterSlaveEntry entry, List<Object> params, Object[] keys) {
        String script = "local counter = 0;"
                        + "for i=2, #ARGV do "
                           +  "local value = redis.call('hget', KEYS[1], ARGV[i]); "
                           +  "if value ~= false then "
                                + "redis.call('hdel', KEYS[1], ARGV[i]); "

                                + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[i]); "
                                + "if not (expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[1])) then "
                                   + "counter = counter + 1;"
                                + "end; "

                                + "redis.call('zrem', KEYS[2], ARGV[i]); "
                                + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[i]), ARGV[i], string.len(tostring(value)), tostring(value)); "
                                + "redis.call('publish', KEYS[3], msg); "
                            + "end;"
                        + "end; "
                        + "return counter;";

        if (entry == null) {
            return commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_LONG, script,
                    Arrays.asList(name, getTimeoutSetName(name), getRemovedChannelName(name), getRemovedSyncChannelName(name)),
                    params.toArray());
        }

        return commandExecutor.evalWriteAsync(entry, codec, RedisCommands.EVAL_LONG, script,
                Arrays.asList(name, getTimeoutSetName(name), getRemovedChannelName(name), getRemovedSyncChannelName(name)),
                params.toArray());
    }

    /** TCK 模式 getAndPut（write-through 路径）。 */
    private List<Object> getAndPutValueLocked(K key, V value) {
        double syncId = ThreadLocalRandom.current().nextDouble();
        if (containsKey(key)) {
            Long updateTimeout = getUpdateTimeout();
            List<Object> result = evalWrite(getRawName(), codec, RedisCommands.EVAL_LIST,
                        "local value = redis.call('hget', KEYS[1], ARGV[4]);"
                      + "if ARGV[2] == '0' then "
                          + "redis.call('hdel', KEYS[1], ARGV[4]); "
                          + "redis.call('zrem', KEYS[2], ARGV[4]); "
                          + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                          + "redis.call('publish', KEYS[3], msg); "
                          + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value), ARGV[6]); "
                          + "local syncs = redis.call('publish', KEYS[6], syncMsg); "
                          + "return {0, value, syncs};"
                      + "elseif ARGV[2] ~= '-1' then "
                          + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                          + "redis.call('zadd', KEYS[2], ARGV[2], ARGV[4]); "
                          + "local oldValueRequired = tonumber(redis.call('get', KEYS[9])); "
                          + "local msg, syncMsg; "
                          + "if oldValueRequired == nil or oldValueRequired < 1 then "
                              + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1); "
                              + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1, ARGV[6]); "
                          + "else "
                              + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value)); "
                              + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value), ARGV[6]); "
                          + "end; "
                          + "redis.call('publish', KEYS[5], msg); "
                          + "local syncs = redis.call('publish', KEYS[8], syncMsg); "
                          + "return {1, value, syncs};"
                      + "else "
                          + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                          + "local oldValueRequired = tonumber(redis.call('get', KEYS[9])); "
                          + "local msg, syncMsg; "
                          + "if oldValueRequired == nil or oldValueRequired < 1 then "
                              + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1); "
                              + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1, ARGV[6]); "
                          + "else "
                              + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value)); "
                              + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value), ARGV[6]); "
                          + "end; "
                          + "redis.call('publish', KEYS[5], msg); "
                          + "local syncs = redis.call('publish', KEYS[8], syncMsg); "
                          + "return {1, value, syncs};"
                      + "end; ",
                 Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getRemovedChannelName(), getCreatedChannelName(), getUpdatedChannelName(),
                         getRemovedSyncChannelName(), getCreatedSyncChannelName(), getUpdatedSyncChannelName(), getOldValueListenerCounter()),
                 0, updateTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value), syncId);

            result.add(syncId);
            waitSync(result);
            return result;
        }

        Long creationTimeout = getCreationTimeout();
        if (creationTimeout == 0) {
            return Collections.emptyList();
        }
        List<Object> result = evalWrite(getRawName(), codec, RedisCommands.EVAL_LIST,
                    "if ARGV[1] ~= '-1' then "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                      + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[4]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5]); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], ARGV[6]); "
                      + "local syncs = redis.call('publish', KEYS[4], syncMsg); "
                      + "return {1, syncs};"
                  + "else "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5]); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], ARGV[6]); "
                      + "local syncs = redis.call('publish', KEYS[4], syncMsg); "
                      + "return {1, syncs};"
                  + "end; ",
             Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getCreatedChannelName(), getCreatedSyncChannelName()),
             creationTimeout, 0, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value), syncId);

        result.add(syncId);
        waitSync(result);
        return result;
    }

    /** 原子模式 getAndPut Lua，返回 {added, oldValue?, syncs}。 */
    CompletionStage<List<Object>> getAndPutValue(K key, V value) {
        Long creationTimeout = getCreationTimeout();

        Long updateTimeout = getUpdateTimeout();

        double syncId = ThreadLocalRandom.current().nextDouble();

        String name = getRawName(key);
        RFuture<List<Object>> future = commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_LIST,
                "local value = redis.call('hget', KEYS[1], ARGV[4]);"
              + "if value ~= false then "
                  + "if ARGV[2] == '0' then "
                      + "redis.call('hdel', KEYS[1], ARGV[4]); "
                      + "redis.call('zrem', KEYS[2], ARGV[4]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value), ARGV[6]); "
                      + "local syncs = redis.call('publish', KEYS[6], syncMsg); "
                      + "return {0, value, syncs};"
                  + "elseif ARGV[2] ~= '-1' then "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                      + "redis.call('zadd', KEYS[2], ARGV[2], ARGV[4]); "
                      + "local oldValueRequired = tonumber(redis.call('get', KEYS[9])); "
                      + "local msg, syncMsg; "
                      + "if oldValueRequired == nil or oldValueRequired < 1 then "
                          + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1); "
                          + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1, ARGV[6]); "
                      + "else "
                          + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value)); "
                          + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value), ARGV[6]); "
                      + "end; "
                      + "redis.call('publish', KEYS[5], msg); "
                      + "local syncs = redis.call('publish', KEYS[8], syncMsg); "
                      + "return {1, value, syncs};"
                  + "else "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                      + "local oldValueRequired = tonumber(redis.call('get', KEYS[9])); "
                      + "local msg, syncMsg; "
                      + "if oldValueRequired == nil or oldValueRequired < 1 then "
                          + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1); "
                          + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], -1, ARGV[6]); "
                      + "else "
                          + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value)); "
                          + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], string.len(tostring(value)), tostring(value), ARGV[6]); "
                      + "end; "
                      + "redis.call('publish', KEYS[5], msg); "
                      + "local syncs = redis.call('publish', KEYS[8], syncMsg); "
                      + "return {1, value, syncs};"
                  + "end; "
              + "else "
                  + "if ARGV[1] == '0' then "
                      + "return {nil};"
                  + "elseif ARGV[1] ~= '-1' then "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                      + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[4]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5]); "
                      + "redis.call('publish', KEYS[4], msg); "
                      + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], ARGV[6]); "
                      + "local syncs = redis.call('publish', KEYS[7], syncMsg); "
                      + "return {1, syncs};"
                  + "else "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[5]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5]); "
                      + "redis.call('publish', KEYS[4], msg); "
                      + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[5]), ARGV[5], ARGV[6]); "
                      + "local syncs = redis.call('publish', KEYS[7], syncMsg); "
                      + "return {1, syncs};"
                  + "end; "
              + "end; ",
             Arrays.<Object>asList(name, getTimeoutSetName(name), getRemovedChannelName(name), getCreatedChannelName(name), getUpdatedChannelName(name),
                     getRemovedSyncChannelName(name), getCreatedSyncChannelName(name), getUpdatedSyncChannelName(name), getOldValueListenerCounter(name)),
             creationTimeout, updateTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value), syncId);

        return future.thenApply(r -> {
            if (!r.isEmpty()) {
                r.add(syncId);
            }
            return r;
        });
    }

    /** 同步 getAndPut。 */
    @Override
    public V getAndPut(K key, V value) {
        RFuture<V> future = getAndPutAsync(key, value);
        return sync(future);
    }

    /** 异步 getAndPut，含 write-through 与统计。 */
    @Override
    public RFuture<V> getAndPutAsync(K key, V value) {
        checkNotClosed();
        checkKey(key);
        if (value == null) {
            throw new NullPointerException();
        }

        long startTime = currentNanoTime();
        if (config.isWriteThrough()) {
            RLock lock = getLockedLock(key);
            try {
                CompletionStage<List<Object>> future;
                if (atomicExecution) {
                    future = getAndPutValue(key, value);
                } else {
                    List<Object> res = getAndPutValueLocked(key, value);
                    future = new CompletableFutureWrapper<>(res);
                }
                CompletionStage<V> f = future.thenCompose(res -> {
                    if (res.isEmpty()) {
                        cacheManager.getStatBean(this).addPuts(1);
                        cacheManager.getStatBean(this).addMisses(1);
                        cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                        cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                        return CompletableFuture.completedFuture((V) null);
                    }
                    Long added = (Long) res.get(0);
                    if (added == null) {
                        cacheManager.getStatBean(this).addPuts(1);
                        cacheManager.getStatBean(this).addHits(1);
                        cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                        cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                        return CompletableFuture.completedFuture((V) res.get(1));
                    }

                    CompletableFuture<Void> ff = writeCache(key, value, startTime, res, added);
                    return ff.thenApply(r -> {
                        V val = getAndPutResult(startTime, res);
                        return val;
                    });
                });
                f = handleException(f);
                return new CompletableFutureWrapper<>(f);
            } finally {
                lock.unlock();
            }
        } else {
            RLock lock = getLockedLock(key);
            try {
                CompletionStage<List<Object>> future;
                if (atomicExecution) {
                    future = getAndPutValue(key, value);
                } else {
                    List<Object> res = getAndPutValueLocked(key, value);
                    future = new CompletableFutureWrapper<>(res);
                }
                CompletionStage<V> f = future.handle((r, e) -> {
                    if (e != null) {
                        throw new CompletionException(new CacheException(e));
                    }

                    V val = getAndPutResult(startTime, r);
                    return val;
                });
                return new CompletableFutureWrapper<>(f);
            } finally {
                lock.unlock();
            }
        }
    }

    /** 从 getAndPut Lua 结果提取旧值并更新 hit/miss/put 统计。 */
    private V getAndPutResult(long startTime, List<Object> result) {
        if (result.size() != 4) {
            cacheManager.getStatBean(this).addPuts(1);
            cacheManager.getStatBean(this).addMisses(1);
            cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
            cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
            return null;
        }
        cacheManager.getStatBean(this).addPuts(1);
        cacheManager.getStatBean(this).addHits(1);
        cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
        cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
        return (V) result.get(1);
    }

    /** 同步 putAll。 */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        RFuture<Void> result = putAllAsync(map);
        sync(result);
    }

    /** 异步 putAll，write-through 时对每个 entry 调 writer。 */
    @Override
    public RFuture<Void> putAllAsync(Map<? extends K, ? extends V> map) {
        checkNotClosed();

        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            K key = entry.getKey();
            checkKey(key);
            V value = entry.getValue();
            if (value == null) {
                throw new NullPointerException();
            }
        }


        long startTime = currentNanoTime();
        RFuture<Long> future = putAllValues(map);
        CompletableFuture<Void> result = new CompletableFuture<>();

        Runnable r = () -> {
            Map<K, Entry<? extends K, ? extends V>> addedEntries = new HashMap<>();
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                addedEntries.put(entry.getKey(), new JCacheEntry<K, V>(entry.getKey(), entry.getValue()));
            }

            try {
                cacheWriter.writeAll(addedEntries.values());
            } catch (Exception e) {
                removeValues(addedEntries.keySet().toArray()).whenComplete((res, ex) -> {
                    if (ex != null) {
                        result.completeExceptionally(new CacheException(ex));
                        return;
                    }

                    handleException(result, e);
                });
                return;
            }

            result.complete(null);
        };

        future.whenComplete((res, ex) -> {
            if (ex != null) {
                result.completeExceptionally(new CacheException(ex));
                return;
            }

            cacheManager.getStatBean(this).addPuts(res);
            for (long i = 0; i < res; i++) {
                cacheManager.getStatBean(this).addPutTime((currentNanoTime() - startTime) / res);
            }
        });

        if (atomicExecution) {
            future.whenComplete((res, ex) -> {
                if (config.isWriteThrough()) {
                    commandExecutor.getServiceManager().getExecutor().execute(r);
                } else {
                    result.complete(null);
                }
            });
        } else {
            if (config.isWriteThrough()) {
                r.run();
            } else {
                result.complete(null);
            }
        }
        return new CompletableFutureWrapper<>(result);
    }

    /** TCK 同步模式：阻塞 acquire 信号量直至监听器处理完毕。 */
    void waitSync(List<Object> result) {
        if (result.size() < 2) {
            return;
        }

        Long syncs = (Long) result.get(result.size() - 2);
        Double syncId = (Double) result.get(result.size() - 1);
        if (syncs != null && syncs > 0) {
            RSemaphore semaphore = redisson.getSemaphore(getSyncName(syncId));
            try {
                semaphore.acquire(syncs.intValue());
                semaphore.delete();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** 同步 putIfAbsent。 */
    @Override
    public boolean putIfAbsent(K key, V value) {
        RFuture<Boolean> result = putIfAbsentAsync(key, value);
        return sync(result);
    }

    /** 异步 putIfAbsent。 */
    @Override
    public RFuture<Boolean> putIfAbsentAsync(K key, V value) {
        checkNotClosed();
        checkKey(key);
        if (value == null) {
            throw new NullPointerException();
        }

        long startTime = currentNanoTime();
        RLock lock = getLockedLock(key);
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        try {
            RFuture<Boolean> future;
            if (atomicExecution) {
                future = putIfAbsentValue(key, value);
            } else {
                boolean r = putIfAbsentValueLocked(key, value);
                future = new CompletableFutureWrapper<>(r);
            }
            future.whenComplete((r, ex) -> {
                if (ex != null) {
                    result.completeExceptionally(new CacheException(ex));
                    return;
                }

                if (r) {
                    cacheManager.getStatBean(this).addPuts(1);
                    if (config.isWriteThrough()) {
                        commandExecutor.getServiceManager().getExecutor().execute(() -> {
                            try {
                                cacheWriter.write(new JCacheEntry<K, V>(key, value));
                            } catch (Exception e) {
                                removeValues(key);
                                handleException(result, e);
                                return;
                            }
                            cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                            result.complete(r);
                        });
                        return;
                    }
                }
                cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                result.complete(r);
            });
        } finally {
            lock.unlock();
        }
        return new CompletableFutureWrapper<>(result);
    }

    /** 将异常包装为 CacheWriterException 或 CacheException 完成 Future。 */
    void handleException(CompletableFuture<?> result, Exception e) {
        if (e instanceof CacheWriterException) {
            result.completeExceptionally(e);
        }
        result.completeExceptionally(new CacheWriterException(e));
    }

    RFuture<Boolean> removeValue(K key) {
        double syncId = ThreadLocalRandom.current().nextDouble();

        String name = getRawName(key);
        RFuture<List<Object>> future = commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_LIST,
                "local value = redis.call('hexists', KEYS[1], ARGV[2]); "
              + "if value == 0 then "
                  + "return {0}; "
              + "end; "

              + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[2]); "
              + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[1]) then "
                  + "return {0}; "
              + "end; "

              + "value = redis.call('hget', KEYS[1], ARGV[2]); "
              + "redis.call('hdel', KEYS[1], ARGV[2]); "
              + "redis.call('zrem', KEYS[2], ARGV[2]); "
              + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[2]), ARGV[2], string.len(tostring(value)), tostring(value)); "
              + "redis.call('publish', KEYS[3], msg); "
              + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[2]), ARGV[2], string.len(tostring(value)), tostring(value), ARGV[3]); "
              + "local syncs = redis.call('publish', KEYS[4], syncMsg); "
              + "return {1, syncs};",
             Arrays.asList(name, getTimeoutSetName(name), getRemovedChannelName(name), getRemovedSyncChannelName(name)),
             System.currentTimeMillis(), encodeMapKey(key), syncId);

        RFuture<Boolean> result = waitSync(syncId, future);
        return result;
    }

    /** 同步 remove。 */
    @Override
    public boolean remove(K key) {
        RFuture<Boolean> future = removeAsync(key);
        return sync(future);
    }

    /** 异步 remove；write-through 时 getAndRemove 后调 CacheWriter.delete。 */
    @Override
    public RFuture<Boolean> removeAsync(K key) {
        checkNotClosed();
        checkKey(key);

        long startTime = System.currentTimeMillis();
        if (config.isWriteThrough()) {
            RLock lock = getLockedLock(key);
            try {
                CompletableFuture<Boolean> result = new CompletableFuture<>();
                CompletionStage<V> future = getAndRemoveValue(key);
                if (atomicExecution) {
                    future.whenComplete((oldValue, ex) -> {
                        if (ex != null) {
                            result.completeExceptionally(new CacheException(ex));
                            return;
                        }

                        commandExecutor.getServiceManager().getExecutor().submit(() -> {
                            try {
                                cacheWriter.delete(key);
                                if (oldValue != null) {
                                    cacheManager.getStatBean(this).addRemovals(1);
                                }
                                cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
                                result.complete(oldValue != null);
                            } catch (Exception e) {
                                if (oldValue != null) {
                                    putValue(key, oldValue);
                                }
                                handleException(result, e);
                            }
                        });
                    });
                } else {
                    V oldValue = future.toCompletableFuture().join();
                    try {
                        cacheWriter.delete(key);
                        if (oldValue != null) {
                            cacheManager.getStatBean(this).addRemovals(1);
                        }
                        cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
                        result.complete(oldValue != null);
                    } catch (Exception e) {
                        if (oldValue != null) {
                            putValue(key, oldValue);
                        }
                        handleException(result, e);
                    }
                }
                return new CompletableFutureWrapper<>(result);
            } finally {
                lock.unlock();
            }
        } else {
            RFuture<Boolean> result = removeValue(key);
            CompletionStage<Boolean> f = result.thenApply(res -> {
                if (res) {
                    cacheManager.getStatBean(this).addRemovals(1);
                }
                cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
                return res;
            });
            return new CompletableFutureWrapper<>(f);
        }
    }

    /** TCK 模式条件 remove（value 匹配）。 */
    private boolean removeValueLocked(K key, V value) {

        Boolean result = evalWrite(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local value = redis.call('hget', KEYS[1], ARGV[3]); "
              + "if value == false then "
                  + "return 0; "
              + "end; "

              + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[3]); "
              + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[2]) then "
                  + "return 0; "
              + "end; "

              + "if ARGV[4] == value then "
                  + "redis.call('hdel', KEYS[1], ARGV[3]); "
                  + "redis.call('zrem', KEYS[2], ARGV[3]); "
                  + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value)); "
                  + "redis.call('publish', KEYS[3], msg); "
                  + "return 1; "
              + "end; "
              + "return nil;",
              Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getRemovedChannelName()),
              0, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value));

        if (result == null) {
            Long accessTimeout = getAccessTimeout();
            if (accessTimeout == -1) {
                return false;
            }
            return evalWrite(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
              "if ARGV[1] == '0' then "
                + "redis.call('hdel', KEYS[1], ARGV[3]); "
                + "redis.call('zrem', KEYS[2], ARGV[3]); "
                + "local value = redis.call('hget', KEYS[1], ARGV[3]); "
                + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value)); "
                + "redis.call('publish', KEYS[3], msg); "
            + "elseif ARGV[1] ~= '-1' then "
                + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[3]); "
            + "end; ",
           Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getRemovedChannelName()),
           accessTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value));
        }

        return result;
    }

    /** 原子模式条件 remove Lua。 */
    RFuture<Boolean> removeValue(K key, V value) {
        Long accessTimeout = getAccessTimeout();

        String name = getRawName(key);
        return commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_BOOLEAN,
                "local value = redis.call('hget', KEYS[1], ARGV[3]); "
              + "if value == false then "
                  + "return 0; "
              + "end; "

              + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[3]); "
              + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[2]) then "
                  + "return 0; "
              + "end; "

              + "if ARGV[4] == value then "
                  + "redis.call('hdel', KEYS[1], ARGV[3]); "
                  + "redis.call('zrem', KEYS[2], ARGV[3]); "
                  + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value)); "
                  + "redis.call('publish', KEYS[3], msg); "
                  + "return 1; "
              + "end; "

              + "if ARGV[1] == '0' then "
                  + "redis.call('hdel', KEYS[1], ARGV[3]); "
                  + "redis.call('zrem', KEYS[2], ARGV[3]); "
                  + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value)); "
                  + "redis.call('publish', KEYS[3], msg); "
              + "elseif ARGV[1] ~= '-1' then "
                  + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[3]); "
              + "end; "
              + "return 0; ",
             Arrays.asList(name, getTimeoutSetName(name), getRemovedChannelName(name)),
             accessTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value));
    }


    /** 同步条件 remove。 */
    @Override
    public boolean remove(K key, V value) {
        RFuture<Boolean> future = removeAsync(key, value);
        return sync(future);
    }

    /** 异步条件 remove。 */
    @Override
    public RFuture<Boolean> removeAsync(K key, V value) {
        checkNotClosed();
        checkKey(key);
        if (value == null) {
            throw new NullPointerException();
        }

        long startTime = currentNanoTime();
        if (config.isWriteThrough()) {
            RLock lock = getLockedLock(key);
            try {
                if (atomicExecution) {
                    CompletableFuture<Boolean> result = new CompletableFuture<>();
                    RFuture<Boolean> future = removeValue(key, value);
                    future.whenComplete((r, ex) -> {
                        if (ex != null) {
                            result.completeExceptionally(new CacheException(ex));
                            return;
                        }

                        if (r) {
                            commandExecutor.getServiceManager().getExecutor().submit(() -> {
                                try {
                                    cacheWriter.delete(key);
                                } catch (Exception e) {
                                    putValue(key, value);

                                    handleException(result, e);
                                    return;
                                }

                                cacheManager.getStatBean(this).addHits(1);
                                cacheManager.getStatBean(this).addRemovals(1);
                                cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
                                result.complete(r);
                            });
                        } else {
                            cacheManager.getStatBean(this).addMisses(1);
                            cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
                            result.complete(r);
                        }
                    });
                    return new CompletableFutureWrapper<>(result);
                } else {
                    boolean res = removeValueLocked(key, value);
                    if (res) {
                        try {
                            cacheWriter.delete(key);
                        } catch (Exception e) {
                            putValue(key, value).toCompletableFuture().join();
                            if (e instanceof CacheWriterException) {
                                throw e;
                            }
                            throw new CacheWriterException(e);
                        }
                        cacheManager.getStatBean(this).addHits(1);
                        cacheManager.getStatBean(this).addRemovals(1);
                        cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
                    } else {
                        cacheManager.getStatBean(this).addMisses(1);
                        cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
                    }
                    return new CompletableFutureWrapper<>(res);
                }
            } finally {
                lock.unlock();
            }
        } else {
            RLock lock = getLockedLock(key);
            try {
                RFuture<Boolean> future;
                if (atomicExecution) {
                    future = removeValue(key, value);
                } else {
                    boolean res = removeValueLocked(key, value);
                    future = new CompletableFutureWrapper<>(res);
                }
                CompletionStage<Boolean> f = future.handle((r, ex) -> {
                    if (ex != null) {
                        throw new CompletionException(new CacheException(ex));
                    }

                    if (r) {
                        cacheManager.getStatBean(this).addHits(1);
                        cacheManager.getStatBean(this).addRemovals(1);
                    } else {
                        cacheManager.getStatBean(this).addMisses(1);
                    }
                    cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
                    return r;
                });
                return new CompletableFutureWrapper<>(f);
            } finally {
                lock.unlock();
            }
        }
    }

    /** 批量 getAndRemove，返回 key→旧值映射。 */
    CompletionStage<Map<K, V>> getAndRemoveValues(Collection<K> keys) {
        double syncId = ThreadLocalRandom.current().nextDouble();

        RFuture<List<Object>> future = getAndRemoveValuesOperation(commandExecutor, null, getRawName(), (Collection<Object>) keys, syncId);

        if (atomicExecution) {
            return future.thenCompose(r -> {
                long nullsAmount = (long) r.get(1);
                if (nullsAmount == keys.size()) {
                    return CompletableFuture.completedFuture(Collections.emptyMap());
                }

                long syncs = (long) r.get(0);
                if (syncs > 0) {
                    RSemaphore semaphore = redisson.getSemaphore(getSyncName(syncId));
                    return semaphore.acquireAsync((int) syncs).thenCompose(obj1 -> {
                        return semaphore.deleteAsync().thenCompose(obj -> {
                                    return getAndRemoveValuesResult(keys, r, nullsAmount);
                                });
                    });
                } else {
                    return getAndRemoveValuesResult(keys, r, nullsAmount);
                }
            });
        } else {
            List<Object> r = future.toCompletableFuture().join();

            long nullsAmount = (long) r.get(1);
            if (nullsAmount == keys.size()) {
                return CompletableFuture.completedFuture(Collections.emptyMap());
            }

            long syncs = (long) r.get(0);
            if (syncs > 0) {
                RSemaphore semaphore = redisson.getSemaphore(getSyncName(syncId));
                try {
                    semaphore.acquire((int) syncs);
                    semaphore.delete();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            return getAndRemoveValuesResult(keys, r, nullsAmount);
        }
    }

    /** 批量 getAndRemove Lua。 */
    RFuture<List<Object>> getAndRemoveValuesOperation(CommandAsyncExecutor commandExecutor, MasterSlaveEntry entry, String name, Collection<Object> keys, double syncId) {
        List<Object> params = new ArrayList<>();
        params.add(System.currentTimeMillis());
        params.add(syncId);

        encodeMapKeys(params, keys);

        String script = "local syncs = 0; "
          + "local values = {}; "
          + "local result = {}; "
          + "local nulls = {}; "

          + "for i = 3, #ARGV, 1 do "
              + "local value = redis.call('hget', KEYS[1], ARGV[i]); "
              + "if value == false then "
                  + "table.insert(nulls, i-3); "
              + "else "
                  + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[i]); "
                  + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[1]) then "
                      + "table.insert(nulls, i-3); "
                  + "else "
                      + "redis.call('hdel', KEYS[1], ARGV[i]); "
                      + "redis.call('zrem', KEYS[2], ARGV[i]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[i]), ARGV[i], string.len(tostring(value)), tostring(value)); "
                      + "redis.call('publish', KEYS[3], msg); "
                      + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[i]), ARGV[i], string.len(tostring(value)), tostring(value), ARGV[2]); "
                      + "syncs = syncs + redis.call('publish', KEYS[4], syncMsg); "
                      + "table.insert(values, value); "
                  + "end; "
              + "end; "
          + "end; "

          + "table.insert(result, syncs); "
          + "table.insert(result, #nulls); "
          + "for i = 1, #nulls, 1 do "
              + "table.insert(result, nulls[i]); "
          + "end; "
          + "for i = 1, #values, 1 do "
              + "table.insert(result, values[i]); "
          + "end; "
          + "return result; ";

        if (entry == null) {
            return commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_MAP_VALUE_LIST, script,
                    Arrays.asList(name, getTimeoutSetName(name), getRemovedChannelName(name), getRemovedSyncChannelName(name)),
                    params.toArray());
        }

        return commandExecutor.evalWriteAsync(entry, codec, RedisCommands.EVAL_MAP_VALUE_LIST, script,
                Arrays.asList(name, getTimeoutSetName(name), getRemovedChannelName(name), getRemovedSyncChannelName(name)),
                params.toArray());
    }

    /** 解析批量 getAndRemove 的 Lua 返回列表为 Map。 */
    private CompletionStage<Map<K, V>> getAndRemoveValuesResult(Collection<K> keys, List<Object> r, long nullsAmount) {
        Map<K, V> res = new HashMap<>();
        fillMap(keys, r, res, nullsAmount, 0);
        return CompletableFuture.completedFuture(res);
    }

    /** 按 null 索引列表将 Lua 结果填充为 key→value Map。 */
    void fillMap(Collection<K> keys, List<Object> r, Map<K, V> res, long nullsAmount, int baseIndex) {
        List<Long> list = (List<Long>) (Object) r.subList(baseIndex + 2, baseIndex + (int) nullsAmount + 2);
        HashSet<Long> nullIndexes = new HashSet<>(list);
        long i = 0;
        for (K key : keys) {
            if (nullIndexes.contains(i)) {
                continue;
            }
            V value = (V) r.get((int) (baseIndex + i + nullsAmount + 2));
            res.put(key, value);
            i++;
        }
    }

    /** 单 key getAndRemove Lua。 */
    CompletionStage<V> getAndRemoveValue(K key) {
        double syncId = ThreadLocalRandom.current().nextDouble();

        String name = getRawName(key);
        RFuture<List<Object>> future = commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_MAP_VALUE_LIST,
                "local value = redis.call('hget', KEYS[1], ARGV[2]); "
              + "if value == false then "
                  + "return {nil}; "
              + "end; "

              + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[2]); "
              + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[1]) then "
                  + "return {nil}; "
              + "end; "

              + "redis.call('hdel', KEYS[1], ARGV[2]); "
              + "redis.call('zrem', KEYS[2], ARGV[2]); "
              + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[2]), ARGV[2], string.len(tostring(value)), tostring(value)); "
              + "redis.call('publish', KEYS[3], msg); "
              + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[2]), ARGV[2], string.len(tostring(value)), tostring(value), ARGV[3]); "
              + "local syncs = redis.call('publish', KEYS[4], syncMsg); "
              + "return {value, syncs}; ",
                Arrays.asList(name, getTimeoutSetName(name), getRemovedChannelName(name), getRemovedSyncChannelName(name)),
                System.currentTimeMillis(), encodeMapKey(key), syncId);

        if (atomicExecution) {
            return future.thenCompose(r -> {
                if (r.size() < 2) {
                    return CompletableFuture.completedFuture((V) null);
                }

                Long syncs = (Long) r.get(1);
                if (syncs != null && syncs > 0) {
                    RSemaphore semaphore = redisson.getSemaphore(getSyncName(syncId));
                    return semaphore.acquireAsync(syncs.intValue()).thenCompose(obj1 -> {
                        return semaphore.deleteAsync().thenApply(obj -> (V) r.get(0));
                    });
                } else {
                    return CompletableFuture.completedFuture((V) r.get(0));
                }
            });
        } else {
            List<Object> r = future.toCompletableFuture().join();

            if (r.size() < 2) {
                return CompletableFuture.completedFuture((V) null);
            }

            Long syncs = (Long) r.get(1);
            if (syncs != null && syncs > 0) {
                RSemaphore semaphore = redisson.getSemaphore(getSyncName(syncId));
                try {
                    semaphore.acquire(syncs.intValue());
                    semaphore.delete();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            return CompletableFuture.completedFuture((V) r.get(0));
        }
    }

    /** 同步 getAndRemove。 */
    @Override
    public V getAndRemove(K key) {
        RFuture<V> future = getAndRemoveAsync(key);
        return sync(future);
    }

    /** 异步 getAndRemove。 */
    @Override
    public RFuture<V> getAndRemoveAsync(K key) {
        checkNotClosed();
        checkKey(key);

        long startTime = currentNanoTime();
        CompletableFuture<V> result = new CompletableFuture<>();
        RLock lock = getLockedLock(key);
        try {
            CompletionStage<V> future = getAndRemoveValue(key);
            future.whenComplete((value, e) -> {
                if (e != null) {
                    result.completeExceptionally(new CacheException(e));
                    return;
                }

                if (value != null) {
                    cacheManager.getStatBean(this).addHits(1);
                    cacheManager.getStatBean(this).addRemovals(1);
                } else {
                    cacheManager.getStatBean(this).addMisses(1);
                }

                if (config.isWriteThrough()) {
                    commandExecutor.getServiceManager().getExecutor().submit(() -> {
                        try {
                            cacheWriter.delete(key);
                        } catch (Exception ex) {
                            if (value != null) {
                                putValue(key, value);
                            }

                            handleException(result, ex);
                            return;
                        }
                        cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                        cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
                        result.complete(value);
                    });
                } else {
                    cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                    cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
                    result.complete(value);
                }
            });
        } finally {
            lock.unlock();
        }
        return new CompletableFutureWrapper<>(result);
    }

    /** TCK 模式 compare-and-replace（oldValue 匹配）。 */
    private long replaceValueLocked(K key, V oldValue, V newValue) {
        Long res = evalWrite(getRawName(), codec, RedisCommands.EVAL_LONG,
                "local value = redis.call('hget', KEYS[1], ARGV[4]); "
              + "if value == false then "
                  + "return 0; "
              + "end; "

              + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[4]); "
              + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[3]) then "
                  + "return 0; "
              + "end; "

              + "if ARGV[5] == value then "
                  + "return 1;"
              + "end; "
              + "return -1;",
              Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getRemovedChannelName(), getUpdatedChannelName()),
              0, 0, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(oldValue), encodeMapValue(newValue));

       if (res == 1) {
           Long updateTimeout = getUpdateTimeout();
           double syncId = ThreadLocalRandom.current().nextDouble();
           Long syncs = evalWrite(getRawName(), codec, RedisCommands.EVAL_LONG,
                   "if ARGV[2] == '0' then "
                           + "redis.call('hdel', KEYS[1], ARGV[4]); "
                           + "redis.call('zrem', KEYS[2], ARGV[4]); "
                           + "local value = redis.call('hget', KEYS[1], ARGV[4]); "
                           + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                           + "redis.call('publish', KEYS[3], msg); "
                           + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value), ARGV[7]); "
                           + "return redis.call('publish', KEYS[5], syncMsg); "
                       + "elseif ARGV[2] ~= '-1' then "
                           + "local oldValueRequired = tonumber(redis.call('get', KEYS[7])); "
                           + "local msg, syncMsg; "
                           + "if oldValueRequired == nil or oldValueRequired < 1 then "
                               + "redis.call('hset', KEYS[1], ARGV[4], ARGV[6]); "
                               + "redis.call('zadd', KEYS[2], ARGV[2], ARGV[4]); "
                               + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[6]), ARGV[6], -1); "
                               + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[4]), ARGV[4], string.len(ARGV[6]), ARGV[6], -1, ARGV[7]); "
                           + "else "
                               + "local value = redis.call('hget', KEYS[1], ARGV[4]); "
                               + "redis.call('hset', KEYS[1], ARGV[4], ARGV[6]); "
                               + "redis.call('zadd', KEYS[2], ARGV[2], ARGV[4]); "
                               + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[6]), ARGV[6], string.len(tostring(value)), tostring(value)); "
                               + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(ARGV[6]), ARGV[6], string.len(tostring(value)), tostring(value), ARGV[7]); "
                           + "end; "
                           + "redis.call('publish', KEYS[4], msg); "
                           + "return redis.call('publish', KEYS[6], syncMsg); "
                       + "else "
                           + "redis.call('hset', KEYS[1], ARGV[4], ARGV[6]); "
                           + "local msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[6]), ARGV[6], -1); "
                           + "redis.call('publish', KEYS[4], msg); "
                           + "local syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[4]), ARGV[4], string.len(ARGV[6]), ARGV[6], -1, ARGV[7]); "
                           + "return redis.call('publish', KEYS[6], syncMsg); "
                       + "end; ",
                       Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getRemovedChannelName(), getUpdatedChannelName(),
                               getRemovedSyncChannelName(), getUpdatedSyncChannelName(), getOldValueListenerCounter()),
                       0, updateTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(oldValue), encodeMapValue(newValue), syncId);

           List<Object> result = Arrays.<Object>asList(syncs, syncId);
           waitSync(result);

           return res;
       } else if (res == 0) {
           return res;
       }

       Long accessTimeout = getAccessTimeout();
       if (accessTimeout == -1) {
           return -1;
       }

       double syncId = ThreadLocalRandom.current().nextDouble();
       List<Object> result = evalWrite(getRawName(), codec, RedisCommands.EVAL_LIST,
                "if ARGV[1] == '0' then "
                  + "redis.call('hdel', KEYS[1], ARGV[4]); "
                  + "redis.call('zrem', KEYS[2], ARGV[4]); "
                  + "local value = redis.call('hget', KEYS[1], ARGV[4]); "
                  + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(value), value); "
                  + "redis.call('publish', KEYS[3], msg); "
                  + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[4]), ARGV[4], string.len(value), value, ARGV[7]); "
                  + "local syncs = redis.call('publish', KEYS[4], syncMsg); "
                  + "return {-1, syncs}; "
              + "elseif ARGV[1] ~= '-1' then "
                  + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[3]); "
                  + "return {0};"
              + "end; ",
             Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getRemovedChannelName(), getRemovedSyncChannelName()),
             accessTimeout, 0, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(oldValue), encodeMapValue(newValue), syncId);

       result.add(syncId);
       waitSync(result);
       return (Long) result.get(0);
    }


    RFuture<Long> replaceValue(K key, V oldValue, V newValue) {
        Long accessTimeout = getAccessTimeout();

        Long updateTimeout = getUpdateTimeout();

        String name = getRawName(key);
        return commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_LONG,
                "local value = redis.call('hget', KEYS[1], ARGV[4]); "
              + "if value == false then "
                  + "return 0; "
              + "end; "

              + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[4]); "
              + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[3]) then "
                  + "return 0; "
              + "end; "

              + "if ARGV[5] == value then "
                  + "if ARGV[2] == '0' then "
                      + "redis.call('hdel', KEYS[1], ARGV[4]); "
                      + "redis.call('zrem', KEYS[2], ARGV[4]); "
                      + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                      + "redis.call('publish', KEYS[3], msg); "
                  + "elseif ARGV[2] ~= '-1' then "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[6]); "
                      + "redis.call('zadd', KEYS[2], ARGV[2], ARGV[4]); "
                      + "local oldValueRequired = tonumber(redis.call('get', KEYS[5])); "
                      + "local msg; "
                      + "if oldValueRequired == nil or oldValueRequired < 1 then "
                          + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[6]), ARGV[6], -1); "
                      + "else "
                          + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[6]), ARGV[6], string.len(tostring(value)), tostring(value)); "
                      + "end; "
                      + "redis.call('publish', KEYS[4], msg); "
                  + "else "
                      + "redis.call('hset', KEYS[1], ARGV[4], ARGV[6]); "
                      + "local oldValueRequired = tonumber(redis.call('get', KEYS[5])); "
                      + "local msg; "
                      + "if oldValueRequired == nil or oldValueRequired < 1 then "
                          + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[4]), ARGV[4], string.len(ARGV[6]), ARGV[6], -1); "
                      + "else "
                          + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(ARGV[6]), ARGV[6], string.len(tostring(value)), tostring(value)); "
                      + "end; "
                      + "redis.call('publish', KEYS[4], msg); "
                  + "end; "
                  + "return 1;"
              + "end; "

              + "if ARGV[1] == '0' then "
                  + "redis.call('hdel', KEYS[1], ARGV[4]); "
                  + "redis.call('zrem', KEYS[2], ARGV[4]); "
                  + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[4]), ARGV[4], string.len(value), value); "
                  + "redis.call('publish', KEYS[3], msg); "
              + "elseif ARGV[1] ~= '-1' then "
                  + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[4]); "
                  + "return 0;"
              + "end; "
              + "return -1; ",
             Arrays.<Object>asList(name, getTimeoutSetName(name), getRemovedChannelName(name), getUpdatedChannelName(name), getOldValueListenerCounter(name)),
             accessTimeout, updateTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(oldValue), encodeMapValue(newValue));

    }

    /** 同步 CAS replace。 */
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        RFuture<Boolean> future = replaceAsync(key, oldValue, newValue);
        return sync(future);
    }

    /** 异步 CAS replace。 */
    @Override
    public RFuture<Boolean> replaceAsync(K key, V oldValue, V newValue) {
        checkNotClosed();
        checkKey(key);
        if (oldValue == null) {
            throw new NullPointerException();
        }
        if (newValue == null) {
            throw new NullPointerException();
        }

        long startTime = currentNanoTime();
        RLock lock = getLockedLock(key);
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        try {
            RFuture<Long> future;
            if (atomicExecution) {
                future = replaceValue(key, oldValue, newValue);
            } else {
                long res = replaceValueLocked(key, oldValue, newValue);
                future = new CompletableFutureWrapper<>(res);
            }
            future.whenComplete((res, ex) -> {
                if (ex != null) {
                    result.completeExceptionally(new CacheException(ex));
                    return;
                }

                if (res == 1) {
                    if (config.isWriteThrough()) {
                        commandExecutor.getServiceManager().getExecutor().submit(() -> {
                            try {
                                cacheWriter.write(new JCacheEntry<K, V>(key, newValue));
                            } catch (Exception e) {
                                removeValues(key);

                                handleException(result, e);
                                return;
                            }

                            cacheManager.getStatBean(this).addHits(1);
                            cacheManager.getStatBean(this).addPuts(1);
                            cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                            cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                            result.complete(true);
                        });
                    } else {
                        cacheManager.getStatBean(this).addHits(1);
                        cacheManager.getStatBean(this).addPuts(1);
                        cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                        cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                        result.complete(true);
                    }
                } else {
                    if (res == 0) {
                        cacheManager.getStatBean(this).addMisses(1);
                    } else {
                        cacheManager.getStatBean(this).addHits(1);
                    }
                    cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                    cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                    result.complete(false);
                }
            });
        } finally {
            lock.unlock();
        }
        return new CompletableFutureWrapper<>(result);
    }

    /** TCK 模式 replace（仅当 key 存在）。 */
    private boolean replaceValueLocked(K key, V value) {

        if (containsKey(key)) {
            double syncId = ThreadLocalRandom.current().nextDouble();
            Long updateTimeout = getUpdateTimeout();
            Long syncs = evalWrite(getRawName(), codec, RedisCommands.EVAL_LONG,
                    "if ARGV[1] == '0' then "
                  + "redis.call('hdel', KEYS[1], ARGV[3]); "
                  + "redis.call('zrem', KEYS[2], ARGV[3]); "
                  + "local value = redis.call('hget', KEYS[1], ARGV[3]); "
                  + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value)); "
                  + "redis.call('publish', KEYS[3], msg); "
                  + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value), ARGV[5]); "
                  + "return redis.call('publish', KEYS[5], syncMsg); "
              + "elseif ARGV[1] ~= '-1' then "
                  + "local oldValueRequired = tonumber(redis.call('get', KEYS[7])); "
                  + "local msg, syncMsg; "
                  + "if oldValueRequired == nil or oldValueRequired < 1 then "
                      + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                      + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[3]); "
                      + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1); "
                      + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1, ARGV[5]); "
                  + "else "
                      + "local value = redis.call('hget', KEYS[1], ARGV[3]); "
                      + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                      + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[3]); "
                      + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                      + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value), ARGV[5]); "
                  + "end; "
                  + "redis.call('publish', KEYS[4], msg); "
                  + "return redis.call('publish', KEYS[6], syncMsg); "
              + "else "
                  + "local oldValueRequired = tonumber(redis.call('get', KEYS[7])); "
                  + "local msg, syncMsg; "
                  + "if oldValueRequired == nil or oldValueRequired < 1 then "
                      + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                      + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1); "
                      + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1, ARGV[5]); "
                  + "else "
                      + "local value = redis.call('hget', KEYS[1], ARGV[3]); "
                      + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                      + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                      + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value), ARGV[5]); "
                  + "end; "
                  + "redis.call('publish', KEYS[4], msg); "
                  + "return redis.call('publish', KEYS[6], syncMsg); "
              + "end; ",
             Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getRemovedChannelName(), getUpdatedChannelName(),
                     getRemovedSyncChannelName(), getUpdatedSyncChannelName(), getOldValueListenerCounter()),
             updateTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value), syncId);

        List<Object> result = Arrays.<Object>asList(syncs, syncId);
        waitSync(result);
            return true;
        }

        return false;

    }


    RFuture<Boolean> replaceValue(K key, V value) {
        Long updateTimeout = getUpdateTimeout();

        String name = getRawName(key);
        return commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_BOOLEAN,
                "local value = redis.call('hget', KEYS[1], ARGV[3]); "
              + "if value == false then "
                  + "return 0; "
              + "end; "

              + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[3]); "
              + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[2]) then "
                  + "return 0; "
              + "end; "

              + "if ARGV[1] == '0' then "
                  + "redis.call('hdel', KEYS[1], ARGV[3]); "
                  + "redis.call('zrem', KEYS[2], ARGV[3]); "
                  + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value)); "
                  + "redis.call('publish', KEYS[3], msg); "
              + "elseif ARGV[1] ~= '-1' then "
                  + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                  + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[3]); "
                  + "local oldValueRequired = tonumber(redis.call('get', KEYS[5])); "
                  + "local msg; "
                  + "if oldValueRequired == nil or oldValueRequired < 1 then "
                      + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1); "
                  + "else "
                      + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                  + "end; "
                  + "redis.call('publish', KEYS[4], msg); "
              + "else "
                  + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                  + "local oldValueRequired = tonumber(redis.call('get', KEYS[5])); "
                  + "local msg; "
                  + "if oldValueRequired == nil or oldValueRequired < 1 then "
                      + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1); "
                  + "else "
                      + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                  + "end; "
                  + "redis.call('publish', KEYS[4], msg); "
              + "end; "
              + "return 1;",
             Arrays.asList(name, getTimeoutSetName(name), getRemovedChannelName(name), getUpdatedChannelName(name), getOldValueListenerCounter(name)),
             updateTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value));

    }

    RFuture<V> getAndReplaceValue(K key, V value) {
        Long updateTimeout = getUpdateTimeout();

        String name = getRawName(key);
        return commandExecutor.evalWriteAsync(name, codec, RedisCommands.EVAL_MAP_VALUE,
                "local value = redis.call('hget', KEYS[1], ARGV[3]); "
              + "if value == false then "
                  + "return nil; "
              + "end; "

              + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[3]); "
              + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[2]) then "
                  + "return nil; "
              + "end; "

              + "if ARGV[1] == '0' then "
                  + "redis.call('hdel', KEYS[1], ARGV[3]); "
                  + "redis.call('zrem', KEYS[2], ARGV[3]); "
                  + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value)); "
                  + "redis.call('publish', KEYS[3], msg); "
              + "elseif ARGV[1] ~= '-1' then "
                  + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                  + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[3]); "
                  + "local oldValueRequired = tonumber(redis.call('get', KEYS[5])); "
                  + "local msg; "
                  + "if oldValueRequired == nil or oldValueRequired < 1 then "
                      + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1); "
                  + "else "
                      + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                  + "end; "
                  + "redis.call('publish', KEYS[4], msg); "
              + "else "
                  + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                  + "local oldValueRequired = tonumber(redis.call('get', KEYS[5])); "
                  + "local msg; "
                  + "if oldValueRequired == nil or oldValueRequired < 1 then "
                      + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1); "
                  + "else "
                      + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                  + "end; "
                  + "redis.call('publish', KEYS[4], msg); "
              + "end; "
              + "return value;",
             Arrays.asList(name, getTimeoutSetName(name), getRemovedChannelName(name), getUpdatedChannelName(name), getOldValueListenerCounter(name)),
             updateTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value));

    }

    /** TCK 模式 getAndReplace。 */
    private V getAndReplaceValueLocked(K key, V value) {
        V oldValue = evalWrite(getRawName(), codec, RedisCommands.EVAL_MAP_VALUE,
                "local value = redis.call('hget', KEYS[1], ARGV[3]); "
              + "if value == false then "
                  + "return nil; "
              + "end; "

              + "local expireDateScore = redis.call('zscore', KEYS[2], ARGV[3]); "
              + "if expireDateScore ~= false and tonumber(expireDateScore) <= tonumber(ARGV[2]) then "
                  + "return nil; "
              + "end; "

              + "return value;", Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getRemovedChannelName(), getUpdatedChannelName()),
              0, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value));

        if (oldValue != null) {
            Long updateTimeout = getUpdateTimeout();
            double syncId = ThreadLocalRandom.current().nextDouble();
            Long syncs = evalWrite(getRawName(), codec, RedisCommands.EVAL_LONG,
                    "if ARGV[1] == '0' then "
                  + "redis.call('hdel', KEYS[1], ARGV[3]); "
                  + "redis.call('zrem', KEYS[2], ARGV[3]); "
                  + "local value = redis.call('hget', KEYS[1], ARGV[3]); "
                  + "local msg = struct.pack('Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value)); "
                  + "redis.call('publish', KEYS[3], msg); "
                  + "local syncMsg = struct.pack('Lc0Lc0d', string.len(ARGV[3]), ARGV[3], string.len(tostring(value)), tostring(value), ARGV[5]); "
                  + "return redis.call('publish', KEYS[5], msg); "
              + "elseif ARGV[1] ~= '-1' then "
                  + "local oldValueRequired = tonumber(redis.call('get', KEYS[7])); "
                  + "local msg, syncMsg; "
                  + "if oldValueRequired == nil or oldValueRequired < 1 then "
                      + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                      + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[3]); "
                      + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1); "
                      + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1, ARGV[5]); "
                  + "else "
                      + "local value = redis.call('hget', KEYS[1], ARGV[3]); "
                      + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                      + "redis.call('zadd', KEYS[2], ARGV[1], ARGV[3]); "
                      + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                      + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value), ARGV[5]); "
                  + "end; "
                  + "redis.call('publish', KEYS[4], msg); "
                  + "return redis.call('publish', KEYS[6], syncMsg); "
              + "else "
                  + "local oldValueRequired = tonumber(redis.call('get', KEYS[7])); "
                  + "local msg, syncMsg; "
                  + "if oldValueRequired == nil or oldValueRequired < 1 then "
                      + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                      + "msg = struct.pack('Lc0Lc0h', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1); "
                      + "syncMsg = struct.pack('Lc0Lc0hd', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], -1, ARGV[5]); "
                  + "else "
                      + "local value = redis.call('hget', KEYS[1], ARGV[3]); "
                      + "redis.call('hset', KEYS[1], ARGV[3], ARGV[4]); "
                      + "msg = struct.pack('Lc0Lc0Lc0', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value)); "
                      + "syncMsg = struct.pack('Lc0Lc0Lc0d', string.len(ARGV[3]), ARGV[3], string.len(ARGV[4]), ARGV[4], string.len(tostring(value)), tostring(value), ARGV[5]); "
                  + "end; "
                  + "redis.call('publish', KEYS[4], msg); "
                  + "return redis.call('publish', KEYS[6], syncMsg); "
              + "end; ",
             Arrays.<Object>asList(getRawName(), getTimeoutSetName(), getRemovedChannelName(), getUpdatedChannelName(),
                     getRemovedSyncChannelName(), getUpdatedSyncChannelName(), getOldValueListenerCounter()),
             updateTimeout, System.currentTimeMillis(), encodeMapKey(key), encodeMapValue(value), syncId);

            List<Object> result = Arrays.<Object>asList(syncs, syncId);
            waitSync(result);
        }
        return oldValue;
    }

    /** 注册需要 oldValue 的 UPDATE 监听器时递增 Redis 计数。 */
    void incrementOldValueListenerCounter(String counterName) {
        evalWrite(getRawName(), codec, RedisCommands.EVAL_INTEGER,
                "return redis.call('incr', KEYS[1]);",
                Arrays.<Object>asList(counterName));
    }

    /** 注销 UPDATE 监听器时递减计数。 */
    private void decrementOldValueListenerCounter(String counterName) {
        evalWrite(getRawName(), codec, RedisCommands.EVAL_INTEGER,
                "return redis.call('decr', KEYS[1]);",
                Arrays.<Object>asList(counterName));
    }

    /** 读取 oldValue 监听器计数。 */
    private Integer getOldValueListenerCount(String counterName) {
        return evalWrite(getRawName(), codec, RedisCommands.EVAL_INTEGER,
                "return tonumber(redis.call('get', KEYS[1]));",
                Arrays.<Object>asList(counterName));
    }

    /** 同步 replace（key 必须存在）。 */
    @Override
    public boolean replace(K key, V value) {
        RFuture<Boolean> future = replaceAsync(key, value);
        return sync(future);
    }

    /** 异步 replace。 */
    @Override
    public RFuture<Boolean> replaceAsync(K key, V value) {
        checkNotClosed();
        checkKey(key);
        if (value == null) {
            throw new NullPointerException();
        }

        long startTime = currentNanoTime();
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        RLock lock = getLockedLock(key);
        try {
            RFuture<Boolean> future;
            if (atomicExecution) {
                future = replaceValue(key, value);
            } else {
                boolean res = replaceValueLocked(key, value);
                future = new CompletableFutureWrapper<>(res);
            }

            future.whenComplete((r, ex) -> {
                if (ex != null) {
                    result.completeExceptionally(new CacheException(ex));
                    return;
                }

                if (r) {
                    if (config.isWriteThrough()) {
                        commandExecutor.getServiceManager().getExecutor().submit(() -> {
                            try {
                                cacheWriter.write(new JCacheEntry<K, V>(key, value));
                            } catch (Exception e) {
                                removeValues(key);

                                handleException(result, e);
                            }

                            cacheManager.getStatBean(this).addHits(1);
                            cacheManager.getStatBean(this).addPuts(1);
                            cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                            result.complete(r);
                        });
                        return;
                    }

                    cacheManager.getStatBean(this).addHits(1);
                    cacheManager.getStatBean(this).addPuts(1);
                } else {
                    cacheManager.getStatBean(this).addMisses(1);
                }
                cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                result.complete(r);
            });
        } finally {
            lock.unlock();
        }

        return new CompletableFutureWrapper<>(result);
    }

    /** 同步 getAndReplace。 */
    @Override
    public V getAndReplace(K key, V value) {
        RFuture<V> future = getAndReplaceAsync(key, value);
        return sync(future);
    }

    /** 异步 getAndReplace。 */
    @Override
    public RFuture<V> getAndReplaceAsync(K key, V value) {
        checkNotClosed();
        checkKey(key);
        if (value == null) {
            throw new NullPointerException();
        }

        long startTime = currentNanoTime();
        CompletableFuture<V> result = new CompletableFuture<>();
        RLock lock = getLockedLock(key);
        try {
            RFuture<V> future;
            if (atomicExecution) {
                future = getAndReplaceValue(key, value);
            } else {
                V res = getAndReplaceValueLocked(key, value);
                future = new CompletableFutureWrapper<>(res);
            }
            future.whenComplete((r, ex) -> {
                if (ex != null) {
                    result.completeExceptionally(new CacheException(ex));
                    return;
                }

                if (r != null) {
                    if (config.isWriteThrough()) {
                        commandExecutor.getServiceManager().getExecutor().submit(() -> {
                            cacheManager.getStatBean(this).addHits(1);
                            cacheManager.getStatBean(this).addPuts(1);
                            try {
                                cacheWriter.write(new JCacheEntry<K, V>(key, value));
                            } catch (Exception e) {
                                removeValues(key);

                                handleException(result, e);
                                return;
                            }

                            cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                            cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                            result.complete(r);
                        });
                        return;
                    }
                    cacheManager.getStatBean(this).addHits(1);
                    cacheManager.getStatBean(this).addPuts(1);
                } else {
                    cacheManager.getStatBean(this).addMisses(1);
                }
                cacheManager.getStatBean(this).addPutTime(currentNanoTime() - startTime);
                cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);
                result.complete(r);
            });
        } finally {
            lock.unlock();
        }
        return new CompletableFutureWrapper<>(result);
    }

    /** 同步批量 remove。 */
    @Override
    public void removeAll(Set<? extends K> keys) {
        RFuture<Void> future = removeAllAsync(keys);
        sync(future);
    }

    /** 异步批量 remove。 */
    @Override
    public RFuture<Void> removeAllAsync(Set<? extends K> keys) {
        checkNotClosed();

        for (K key : keys) {
            checkKey(key);
        }

        long startTime = currentNanoTime();
        if (config.isWriteThrough()) {
            CompletionStage<Map<K, V>> future = getAndRemoveValues((Set<K>) keys);
            CompletionStage<Void> f = future.handle((r, ex) -> {
                try {
                    cacheWriter.deleteAll(r.keySet());
                } catch (Exception e) {
                    putAllValues(r);
                    if (e instanceof CacheWriterException) {
                        throw new CompletionException(e);
                    }
                    throw new CompletionException(new CacheWriterException(e));
                }
                cacheManager.getStatBean(this).addRemovals(r.size());
                cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
                return null;
            });
            return new CompletableFutureWrapper<>(f);
        }

        RFuture<Long> future = removeValues(keys.toArray());
        CompletionStage<Void> f = future.thenAccept(res -> {
            cacheManager.getStatBean(this).addRemovals(res);
            cacheManager.getStatBean(this).addRemoveTime(currentNanoTime() - startTime);
        });
        f = handleException(f);
        return new CompletableFutureWrapper<>(f);
    }

    MapScanResult<Object, Object> scanIterator(String name, RedisClient client, String startPos) {
        RFuture<MapScanResult<Object, Object>> f
            = commandExecutor.readAsync(client, name, codec, RedisCommands.HSCAN, name, startPos, "COUNT", 50);
        try {
            return get(f);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    Iterator<K> keyIterator() {
        return new RedissonBaseMapIterator<K>() {
            @Override
            protected K getValue(Map.Entry<Object, Object> entry) {
                return (K) entry.getKey();
            }

            @Override
            protected void remove(Map.Entry<Object, Object> value) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected Object put(Map.Entry<Object, Object> entry, Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected ScanResult<Map.Entry<Object, Object>> iterator(RedisClient client,
                                                                     String nextIterPos) {
                return JCache.this.scanIterator(JCache.this.getRawName(), client, nextIterPos);
            }
        };
    }

    /** 通过迭代器分批 removeAll（每批 50 key）。 */
    @Override
    public void removeAll() {
        checkNotClosed();
        Set<K> keys = new HashSet<>();
        for (Iterator<K> iterator = keyIterator(); iterator.hasNext();) {
            K key = iterator.next();
            keys.add(key);
            if (keys.size() == 50) {
                removeAll(keys);
                keys.clear();
            }
        }

        if (!keys.isEmpty()) {
            removeAll(keys);
        }
    }

    /** 同步 clear：删除 hash 与 timeout ZSET。 */
    @Override
    public void clear() {
        RFuture<Void> future = clearAsync();
        sync(future);
    }

    /** 异步 DEL hash 与 timeout 键。 */
    @Override
    public RFuture<Void> clearAsync() {
        return clearAsync(commandExecutor, null, getRawName());
    }

    /** clear 底层 write DEL。 */
    RFuture<Void> clearAsync(CommandAsyncExecutor commandExecutor, MasterSlaveEntry entry, String name) {
        checkNotClosed();
        if (entry == null) {
            return commandExecutor.writeAsync(name, StringCodec.INSTANCE, RedisCommands.DEL_VOID, name, getTimeoutSetName(name));
        }
        return commandExecutor.writeAsync(entry, StringCodec.INSTANCE, RedisCommands.DEL_VOID, name, getTimeoutSetName(name));
    }

    /** 返回 {@link JCacheConfiguration} 实例。 */
    @Override
    public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
        if (clazz.isInstance(config)) {
            return clazz.cast(config);
        }

        throw new IllegalArgumentException("Configuration object is not an instance of " + clazz);
    }

    /**
     * 在 key 上加锁执行 EntryProcessor；按 {@link JMutableEntry.Action} 提交 put/remove。
     */
    @Override
    public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments)
            throws EntryProcessorException {
        checkNotClosed();
        checkKey(key);
        if (entryProcessor == null) {
            throw new NullPointerException();
        }

        long startTime = currentNanoTime();
        if (containsKey(key)) {
            cacheManager.getStatBean(this).addHits(1);
        } else {
            cacheManager.getStatBean(this).addMisses(1);
        }
        cacheManager.getStatBean(this).addGetTime(currentNanoTime() - startTime);

        JMutableEntry<K, V> entry = new JMutableEntry<K, V>(this, key, null, config.isReadThrough());

        RLock lock = getLockedLock(key);
        try {
            T result = entryProcessor.process(entry, arguments);
            if (entry.getAction() == Action.CREATED
                    || entry.getAction() == Action.UPDATED) {
                put(key, entry.value());
            }
            if (entry.getAction() == Action.DELETED) {
                remove(key);
            }
            return result;
        } catch (EntryProcessorException e) {
            throw e;
        } catch (Exception e) {
            throw new EntryProcessorException(e);
        } finally {
            lock.unlock();
        }
    }

    /** 对每个 key 依次 invoke，收集结果或 EntryProcessorException。 */
    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor,
            Object... arguments) {
        checkNotClosed();
        if (entryProcessor == null) {
            throw new NullPointerException();
        }

        Map<K, EntryProcessorResult<T>> results = new HashMap<K, EntryProcessorResult<T>>();
        for (K key : keys) {
            try {
                final T result = invoke(key, entryProcessor, arguments);
                if (result != null) {
                    results.put(key, () -> result);
                }
            } catch (final EntryProcessorException e) {
                results.put(key, () -> {
                    throw e;
                });
            }
        }

        return results;
    }

    /** 返回所属 JCacheManager。 */
    @Override
    public CacheManager getCacheManager() {
        checkNotClosed();
        return cacheManager;
    }

    /** 委托 Manager 的 URI。 */
    URI getURI() {
        return cacheManager.getURI();
    }

    /** 关闭 Cache：注销监听器、Eviction 任务，独立 Redisson 时 shutdown。 */
    @Override
    public void close() {
        if (isClosed()) {
            return;
        }

        if (closed.compareAndSet(false, true)) {
            if (hasOwnRedisson) {
                redisson.shutdown();
            }
            cacheManager.closeCache(this);
            for (CacheEntryListenerConfiguration<K, V> config : listeners.keySet()) {
                deregisterCacheEntryListener(config);
            }
            redisson.getEvictionScheduler().remove(getRawName());
        }
    }

    /** 是否已 close。 */
    @Override
    public boolean isClosed() {
        return closed.get();
    }

    /** 支持 unwrap 为 JCache、CacheAsync、CacheReactive、CacheRx。 */
    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(getClass())) {
            return clazz.cast(this);
        }
        if (clazz == CacheAsync.class) {
            return (T) this;
        }
        if (clazz == CacheReactive.class) {
            return (T) ReactiveProxyBuilder.create(((RedissonReactive) redisson.reactive()).getCommandExecutor(), this, CacheReactive.class);
        }
        if (clazz == CacheRx.class) {
            return (T) RxProxyBuilder.create(((RedissonRx) redisson.rxJava()).getCommandExecutor(), this, CacheRx.class);
        }
        return null;
    }

    /** 注册监听器并写入配置。 */
    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        registerCacheEntryListener(cacheEntryListenerConfiguration, true);
    }

    /** Redis 服务器 OS 类型，用于 JCacheEventCodec struct 打包。 */
    JCacheEventCodec.OSType osType;

    /**
     * 按监听器类型订阅 CREATED/UPDATED/REMOVED/EXPIRED 频道；
     * 同步监听器使用 *_sync_channel，UPDATE+oldValue 时递增计数器。
     */
    void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration, boolean addToConfig) {
        if (osType == null) {
            RFuture<Map<String, String>> serverFuture = commandExecutor.readAsync((String) null, StringCodec.INSTANCE, RedisCommands.INFO_SERVER);
            String os = serverFuture.toCompletableFuture().join().get("os");
            if (os.contains("Windows")) {
                osType = BaseEventCodec.OSType.WINDOWS;
            } else if (os.contains("NONSTOP")) {
                osType = BaseEventCodec.OSType.HPNONSTOP;
            }
        }

        Factory<CacheEntryListener<? super K, ? super V>> factory = cacheEntryListenerConfiguration.getCacheEntryListenerFactory();
        final CacheEntryListener<? super K, ? super V> listener = factory.create();

        Factory<CacheEntryEventFilter<? super K, ? super V>> filterFactory = cacheEntryListenerConfiguration.getCacheEntryEventFilterFactory();
        final CacheEntryEventFilter<? super K, ? super V> filter;
        if (filterFactory != null) {
            filter = filterFactory.create();
        } else {
            filter = null;
        }

        Map<Integer, String> values = new ConcurrentHashMap<Integer, String>();

        Map<Integer, String> oldValues = listeners.putIfAbsent(cacheEntryListenerConfiguration, values);
        if (oldValues != null) {
            values = oldValues;
        }

        final boolean sync = cacheEntryListenerConfiguration.isSynchronous();

        if (CacheEntryRemovedListener.class.isAssignableFrom(listener.getClass())) {
            String channelName = getRemovedChannelName();
            if (sync) {
                channelName = getRemovedSyncChannelName();
            }

            RTopic topic = redisson.getTopic(channelName, new JCacheEventCodec(codec, osType, sync));
            int listenerId = topic.addListener(List.class, (MessageListener<List<Object>>) (channel, msg) -> {
                JCacheEntryEvent<K, V> event = new JCacheEntryEvent<K, V>(JCache.this, EventType.REMOVED, msg.get(0), msg.get(1), msg.get(1));
                try {
                    if (filter == null || filter.evaluate(event)) {
                        List<CacheEntryEvent<? extends K, ? extends V>> events = Collections.<CacheEntryEvent<? extends K, ? extends V>>singletonList(event);
                        ((CacheEntryRemovedListener<K, V>) listener).onRemoved(events);
                    }
                } finally {
                    sendSync(sync, msg);
                }
            });
            values.put(listenerId, channelName);
        }
        if (CacheEntryCreatedListener.class.isAssignableFrom(listener.getClass())) {
            String channelName = getCreatedChannelName();
            if (sync) {
                channelName = getCreatedSyncChannelName();
            }

            RTopic topic = redisson.getTopic(channelName, new JCacheEventCodec(codec, osType, sync));
            int listenerId = topic.addListener(List.class, (MessageListener<List<Object>>) (channel, msg) -> {
                JCacheEntryEvent<K, V> event = new JCacheEntryEvent<K, V>(JCache.this, EventType.CREATED, msg.get(0), msg.get(1));
                try {
                    if (filter == null || filter.evaluate(event)) {
                        List<CacheEntryEvent<? extends K, ? extends V>> events = Collections.<CacheEntryEvent<? extends K, ? extends V>>singletonList(event);
                        ((CacheEntryCreatedListener<K, V>) listener).onCreated(events);
                    }
                } finally {
                    sendSync(sync, msg);
                }
            });
            values.put(listenerId, channelName);
        }
        if (CacheEntryUpdatedListener.class.isAssignableFrom(listener.getClass())) {
            String channelName = getUpdatedChannelName();
            if (sync) {
                channelName = getUpdatedSyncChannelName();
            }

            if (cacheEntryListenerConfiguration.isOldValueRequired()) {
                incrementOldValueListenerCounter(getOldValueListenerCounter());
            }

            RTopic topic = redisson.getTopic(channelName, new JCacheEventCodec(codec, osType, sync, true));
            int listenerId = topic.addListener(List.class, (MessageListener<List<Object>>) (channel, msg) -> {
                JCacheEntryEvent<K, V> event = new JCacheEntryEvent<K, V>(JCache.this, EventType.UPDATED, msg.get(0), msg.get(1), msg.get(2));

                try {
                    if (filter == null || filter.evaluate(event)) {
                        List<CacheEntryEvent<? extends K, ? extends V>> events = Collections.<CacheEntryEvent<? extends K, ? extends V>>singletonList(event);
                        ((CacheEntryUpdatedListener<K, V>) listener).onUpdated(events);
                    }
                } finally {
                    sendSync(sync, msg);
                }
            });
            values.put(listenerId, channelName);
        }
        if (CacheEntryExpiredListener.class.isAssignableFrom(listener.getClass())) {
            String channelName = getExpiredChannelName();

            RTopic topic = redisson.getTopic(channelName, new JCacheEventCodec(codec, osType, false));
            int listenerId = topic.addListener(List.class, (MessageListener<List<Object>>) (channel, msg) -> {
                JCacheEntryEvent<K, V> event = new JCacheEntryEvent<K, V>(JCache.this, EventType.EXPIRED, msg.get(0), msg.get(1), msg.get(1));
                if (filter == null || filter.evaluate(event)) {
                    List<CacheEntryEvent<? extends K, ? extends V>> events = Collections.<CacheEntryEvent<? extends K, ? extends V>>singletonList(event);
                    ((CacheEntryExpiredListener<K, V>) listener).onExpired(events);
                }
            });
            values.put(listenerId, channelName);
        }

        if (addToConfig) {
            config.addCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);
        }
    }

    /** 同步监听器处理完成后 release 信号量通知写端继续。 */
    void sendSync(boolean sync, List<Object> msg) {
        if (sync) {
            Object syncId = msg.get(msg.size() - 1);
            RSemaphore semaphore = redisson.getSemaphore(getSyncName(syncId));
            semaphore.release();
        }
    }

    /** 取消 topic 订阅并更新 oldValue 计数与配置。 */
    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        Map<Integer, String> listenerIds = listeners.remove(cacheEntryListenerConfiguration);
        if (listenerIds != null) {
            for (Map.Entry<Integer, String> entry : listenerIds.entrySet()) {
                redisson.getTopic(entry.getValue()).removeListener(entry.getKey());
            }
        }

        if (cacheEntryListenerConfiguration.isOldValueRequired()) {
            final CacheEntryListener<? super K, ? super V> listener = cacheEntryListenerConfiguration.getCacheEntryListenerFactory().create();

            if (CacheEntryUpdatedListener.class.isAssignableFrom(listener.getClass())) {
                decrementOldValueListenerCounter(getOldValueListenerCounter());
            }
        }

        config.removeCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);
    }

    /**
     * HSCAN 迭代 entry；命中计统计，按 access 策略刷新或删除。
     * 迭代 Entry 不支持 setValue。
     */
    @Override
    public Iterator<Entry<K, V>> iterator() {
        checkNotClosed();
        return new RedissonBaseMapIterator<Entry<K, V>>() {
            @Override
            protected Entry<K, V> getValue(Map.Entry<Object, Object> entry) {
                cacheManager.getStatBean(JCache.this).addHits(1);
                Long accessTimeout = getAccessTimeout();
                JCacheEntry<K, V> je = new JCacheEntry<K, V>((K) entry.getKey(), (V) entry.getValue());
                if (accessTimeout == 0) {
                    remove();
                } else if (accessTimeout != -1) {
                    String name = getRawName(entry.getKey());
                    write(name, RedisCommands.ZADD_BOOL, getTimeoutSetName(name), accessTimeout, encodeMapKey(entry.getKey()));
                }
                return je;
            }

            @Override
            protected void remove(Map.Entry<Object, Object> entry) {
                JCache.this.remove((K) entry.getKey());
            }

            @Override
            protected Object put(Map.Entry<Object, Object> entry, Object value) {
                throw new UnsupportedOperationException();
            }



            @Override
            protected ScanResult<Map.Entry<Object, Object>> iterator(RedisClient client,
                                                                     String nextIterPos) {
                return JCache.this.scanIterator(JCache.this.getRawName(), client, nextIterPos);
            }

        };
    }

}
