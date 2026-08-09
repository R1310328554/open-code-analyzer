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
package org.redisson;

import org.redisson.api.*;
import org.redisson.api.ExecutorOptions;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.MapCacheOptions;
import org.redisson.api.MapOptions;
import org.redisson.api.options.*;
import org.redisson.api.redisnode.*;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonCodec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.Config;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.ServiceManager;
import org.redisson.eviction.EvictionScheduler;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import org.redisson.redisnode.RedissonClusterNodes;
import org.redisson.redisnode.RedissonMasterSlaveNodes;
import org.redisson.redisnode.RedissonSentinelMasterSlaveNodes;
import org.redisson.redisnode.RedissonSingleNode;
import org.redisson.renewal.LockRenewalScheduler;
import org.redisson.transaction.RedissonTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 主入口：在 Redis/Valkey 之上创建并访问全部分布式对象。
 * <p>管理连接池、命令执行器、淘汰调度、Write-Behind 与 Live Object 等基础设施；
 * 实现 {@link RedissonClient} 的全部 factory 方法。
 *
 * @author Nikita Koksharov
 */
public final class Redisson implements RedissonClient {

    static final Logger log = LoggerFactory.getLogger(Redisson.class);
    private final Set<Integer> printed = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** 淘汰调度器（MapCache 等对象使用）。 */
    private final EvictionScheduler evictionScheduler;
    private final WriteBehindService writeBehindService;
    private final ConnectionManager connectionManager;
    private final CommandAsyncExecutor commandExecutor;

    private final ConcurrentMap<Class<?>, Class<?>> liveObjectClassCache = new ConcurrentHashMap<>();
    private final Config config;

    Redisson(Config config) {
        Version.logVersion();

        this.config = config;
        Config configCopy = new Config(config);

        connectionManager = ConnectionManager.create(configCopy);
        RedissonObjectBuilder objectBuilder = null;
        if (config.isReferenceEnabled()) {
            objectBuilder = new RedissonObjectBuilder(this);
        }
        commandExecutor = connectionManager.createCommandExecutor(objectBuilder, RedissonObjectBuilder.ReferenceType.DEFAULT);
        evictionScheduler = new EvictionScheduler(commandExecutor);
        writeBehindService = new WriteBehindService(commandExecutor);

        connectionManager.getServiceManager().register(new LockRenewalScheduler(commandExecutor));
    }

    /** 返回淘汰调度器。 */
    public EvictionScheduler getEvictionScheduler() {
        return evictionScheduler;
    }

    /** 返回命令异步执行器。 */
    public CommandAsyncExecutor getCommandExecutor() {
        return commandExecutor;
    }

    /** 返回连接服务管理器。 */
    public ServiceManager getServiceManager() {
        return connectionManager.getServiceManager();
    }

    /**
     * Create sync/async Redisson instance with default config
     *
     * @return Redisson instance
     */
    public static RedissonClient create() {
        Config config = new Config();
        config.useSingleServer()
        .setAddress("redis://127.0.0.1:6379");
        return create(config);
    }

    /**
     * Create sync/async Redisson instance with provided config
     *
     * @param config for Redisson
     * @return Redisson instance
     */
    /** 使用配置创建 Redisson 客户端实例。 */
    public static RedissonClient create(Config config) {
        return new Redisson(config);
    }

    /** 返回 RxJava3 风格客户端。 */
    @Override
    public RedissonRxClient rxJava() {
        return new RedissonRx(connectionManager, evictionScheduler, writeBehindService);
    }

    /** 返回 Reactor 风格客户端。 */
    @Override
    public RedissonReactiveClient reactive() {
        return new RedissonReactive(connectionManager, evictionScheduler, writeBehindService);
    }

    /** 获取 {@link RArray} 分布式对象。 */
    @Override
    public <V> RArray<V> getArray(String name) {
        return new RedissonArray<>(commandExecutor, name);
    }

    /** 获取 {@link RArray} 分布式对象。 */
    @Override
    public <V> RArray<V> getArray(String name, Codec codec) {
        return new RedissonArray<>(codec, commandExecutor, name);
    }

    /** 获取 {@link RArray} 分布式对象。 */
    @Override
    public <V> RArray<V> getArray(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonArray<>(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RTimeSeries} 分布式对象。 */
    @Override
    public <V, L> RTimeSeries<V, L> getTimeSeries(String name) {
        return new RedissonTimeSeries<>(evictionScheduler, commandExecutor, name);
    }

    /** 获取 {@link RTimeSeries} 分布式对象。 */
    @Override
    public <V, L> RTimeSeries<V, L> getTimeSeries(String name, Codec codec) {
        return new RedissonTimeSeries<>(codec, evictionScheduler, commandExecutor, name);
    }

    /** 获取 {@link RTimeSeries} 分布式对象。 */
    @Override
    public <V, L> RTimeSeries<V, L> getTimeSeries(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonTimeSeries<>(params.getCodec(), evictionScheduler,
                commandExecutor.copy(params),
                params.getName());
    }

    /** 获取 {@link RStream} 分布式对象。 */
    @Override
    public <K, V> RStream<K, V> getStream(String name) {
        return new RedissonStream<K, V>(commandExecutor, name);
    }

    /** 获取 {@link RStream} 分布式对象。 */
    @Override
    public <K, V> RStream<K, V> getStream(String name, Codec codec) {
        return new RedissonStream<>(codec, commandExecutor, name);
    }

    /** 获取 {@link RStream} 分布式对象。 */
    @Override
    public <K, V> RStream<K, V> getStream(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonStream<>(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RSearch} 分布式对象。 */
    @Override
    public RSearch getSearch() {
        return new RedissonSearch(null, commandExecutor);
    }

    /** 获取 {@link RSearch} 分布式对象。 */
    @Override
    public RSearch getSearch(Codec codec) {
        return new RedissonSearch(codec, commandExecutor);
    }

    /** 获取 {@link RSearch} 分布式对象。 */
    @Override
    public RSearch getSearch(OptionalOptions options) {
        OptionalParams params = (OptionalParams) options;
        return new RedissonSearch(params.getCodec(), commandExecutor.copy(params));
    }

    /** 获取 {@link RBinaryStream} 分布式对象。 */
    @Override
    public RBinaryStream getBinaryStream(String name) {
        return new RedissonBinaryStream(commandExecutor, name);
    }

    /** 获取 {@link RBinaryStream} 分布式对象。 */
    @Override
    public RBinaryStream getBinaryStream(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonBinaryStream(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RGeo} 分布式对象。 */
    @Override
    public <V> RGeo<V> getGeo(String name) {
        return new RedissonGeo<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RGeo} 分布式对象。 */
    @Override
    public <V> RGeo<V> getGeo(String name, Codec codec) {
        return new RedissonGeo<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RGeo} 分布式对象。 */
    @Override
    public <V> RGeo<V> getGeo(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonGeo<>(params.getCodec(), commandExecutor.copy(params),
                params.getName(), this);
    }

    /** 获取 {@link RBucket} 分布式对象。 */
    @Override
    public <V> RBucket<V> getBucket(String name) {
        return new RedissonBucket<V>(commandExecutor, name);
    }

    /** 获取 {@link RRateLimiter} 分布式对象。 */
    @Override
    public RRateLimiter getRateLimiter(String name) {
        return new RedissonRateLimiter(commandExecutor, name);
    }

    /** 获取 {@link RRateLimiter} 分布式对象。 */
    @Override
    public RRateLimiter getRateLimiter(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonRateLimiter(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RGcra} 分布式对象。 */
    @Override
    public RGcra getGcra(String name) {
        return new RedissonGcra(commandExecutor, name);
    }

    /** 获取 {@link RGcra} 分布式对象。 */
    @Override
    public RGcra getGcra(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonGcra(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RBucket} 分布式对象。 */
    @Override
    public <V> RBucket<V> getBucket(String name, Codec codec) {
        return new RedissonBucket<>(codec, commandExecutor, name);
    }

    /** 获取 {@link RBucket} 分布式对象。 */
    @Override
    public <V> RBucket<V> getBucket(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonBucket<>(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RBuckets} 分布式对象。 */
    @Override
    public RBuckets getBuckets() {
        return new RedissonBuckets(commandExecutor);
    }

    /** 获取 {@link RBuckets} 分布式对象。 */
    @Override
    public RBuckets getBuckets(Codec codec) {
        return new RedissonBuckets(codec, commandExecutor);
    }

    /** 获取 {@link RBuckets} 分布式对象。 */
    @Override
    public RBuckets getBuckets(OptionalOptions options) {
        OptionalParams params = (OptionalParams) options;
        return new RedissonBuckets(params.getCodec(), commandExecutor.copy(params));
    }

    /** 获取 {@link RMaps} 分布式对象。 */
    @Override
    public <K, V> RMaps<K, V> getMaps() {
        return new RedissonMaps<>(commandExecutor);
    }

    /** 获取 {@link RMaps} 分布式对象。 */
    @Override
    public <K, V> RMaps<K, V> getMaps(Codec codec) {
        return new RedissonMaps<>(codec, commandExecutor);
    }

    /** 获取 {@link RMaps} 分布式对象。 */
    @Override
    public <K, V> RMaps<K, V> getMaps(OptionalOptions options) {
        OptionalParams params = (OptionalParams) options;
        return new RedissonMaps<>(params.getCodec(), commandExecutor.copy(params));
    }

    /** 获取 {@link RJsonBucket} 分布式对象。 */
    @Override
    public <V> RJsonBucket<V> getJsonBucket(String name, JsonCodec codec) {
        return new RedissonJsonBucket<>(codec, commandExecutor, name);
    }

    /** 获取 {@link RJsonBucket} 分布式对象。 */
    @Override
    public <V> RJsonBucket<V> getJsonBucket(JsonBucketOptions<V> options) {
        JsonBucketParams<V> params = (JsonBucketParams) options;
        return new RedissonJsonBucket<>(params.getCodec(), commandExecutor.copy(params), params.getName());
    }
    
    /** 获取 {@link RJsonBuckets} 分布式对象。 */
    @Override
    public RJsonBuckets getJsonBuckets(JsonCodec codec) {
        return new RedissonJsonBuckets(codec, commandExecutor);
    }
    
    /** 获取 {@link RHyperLogLog} 分布式对象。 */
    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(String name) {
        return new RedissonHyperLogLog<V>(commandExecutor, name);
    }

    /** 获取 {@link RHyperLogLog} 分布式对象。 */
    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(String name, Codec codec) {
        return new RedissonHyperLogLog<V>(codec, commandExecutor, name);
    }

    /** 获取 {@link RHyperLogLog} 分布式对象。 */
    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonHyperLogLog<V>(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RList} 分布式对象。 */
    @Override
    public <V> RList<V> getList(String name) {
        return new RedissonList<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RList} 分布式对象。 */
    @Override
    public <V> RList<V> getList(String name, Codec codec) {
        return new RedissonList<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RList} 分布式对象。 */
    @Override
    public <V> RList<V> getList(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonList<V>(params.getCodec(), commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RListMultimap} 分布式对象。 */
    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(String name) {
        return new RedissonListMultimap<K, V>(commandExecutor, name);
    }

    /** 获取 {@link RListMultimap} 分布式对象。 */
    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(String name, Codec codec) {
        return new RedissonListMultimap<K, V>(codec, commandExecutor, name);
    }

    /** 获取 {@link RListMultimap} 分布式对象。 */
    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonListMultimap<>(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RLocalCachedMapCache} 分布式对象。 */
    @Override
    public <K, V> RLocalCachedMapCache<K, V> getLocalCachedMapCache(String name, LocalCachedMapCacheOptions<K, V> options) {
        throw new UnsupportedOperationException("This feature is implemented in the Redisson PRO version. Please refer to https://redisson.pro/feature-comparison.html");
    }

    /** 获取 {@link RLocalCachedMapCache} 分布式对象。 */
    @Override
    public <K, V> RLocalCachedMapCache<K, V> getLocalCachedMapCache(String name, Codec codec, LocalCachedMapCacheOptions<K, V> options) {
        throw new UnsupportedOperationException("This feature is implemented in the Redisson PRO version. Please refer to https://redisson.pro/feature-comparison.html");
    }

    /** 获取 {@link RLocalCachedMap} 分布式对象。 */
    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, LocalCachedMapOptions<K, V> options) {
        return getLocalCachedMap(name, null, options);
    }

    /** 获取 {@link RLocalCachedMap} 分布式对象。 */
    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, Codec codec, LocalCachedMapOptions<K, V> options) {
        return new RedissonLocalCachedMap<K, V>(codec, commandExecutor, name,
                options, evictionScheduler, this, writeBehindService);
    }

    /** 获取 {@link RLocalCachedMap} 分布式对象。 */
    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(org.redisson.api.options.LocalCachedMapOptions<K, V> options) {
        LocalCachedMapParams<K, V> params = (LocalCachedMapParams) options;

        LocalCachedMapOptions<K, V> ops = LocalCachedMapOptions.<K, V>defaults()
                .cacheProvider(LocalCachedMapOptions.CacheProvider.valueOf(params.getCacheProvider().toString()))
                .cacheSize(params.getCacheSize())
                .storeMode(LocalCachedMapOptions.StoreMode.valueOf(params.getStoreMode().toString()))
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.valueOf(params.getEvictionPolicy().toString()))
                .maxIdle(params.getMaxIdleInMillis())
                .loader(params.getLoader())
                .loaderAsync(params.getLoaderAsync())
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.valueOf(params.getReconnectionStrategy().toString()))
                .storeCacheMiss(params.isStoreCacheMiss())
                .timeToLive(params.getTimeToLiveInMillis())
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.valueOf(params.getSyncStrategy().toString()))
                .useObjectAsCacheKey(params.isUseObjectAsCacheKey())
                .useTopicPattern(params.isUseTopicPattern())
                .expirationEventPolicy(LocalCachedMapOptions.ExpirationEventPolicy.valueOf(params.getExpirationEventPolicy().toString()))
                .writer(params.getWriter())
                .writerAsync(params.getWriterAsync())
                .writeBehindDelay(params.getWriteBehindDelay())
                .writeBehindBatchSize(params.getWriteBehindBatchSize())
                .writerRetryInterval(Duration.ofMillis(params.getWriteRetryInterval()));

        if (params.getWriteMode() != null) {
            ops.writeMode(MapOptions.WriteMode.valueOf(params.getWriteMode().toString()));
        }
        if (params.getWriteRetryAttempts() > 0) {
            ops.writerRetryAttempts(params.getWriteRetryAttempts());
        }

        return new RedissonLocalCachedMap<>(params.getCodec(), commandExecutor.copy(params), params.getName(),
                ops, evictionScheduler, this, writeBehindService);
    }

    /** 获取 Session 属性 Map 或通用 Redis Map。 */
    @Override
    public <K, V> RMap<K, V> getMap(String name) {
        return new RedissonMap<K, V>(commandExecutor, name, this, null, null);
    }

    /** 获取 Session 属性 Map 或通用 Redis Map。 */
    @Override
    public <K, V> RMap<K, V> getMap(String name, MapOptions<K, V> options) {
        return new RedissonMap<K, V>(commandExecutor, name, this, options, writeBehindService);
    }

    /** 获取 Session 属性 Map 或通用 Redis Map。 */
    @Override
    public <K, V> RMap<K, V> getMap(org.redisson.api.options.MapOptions<K, V> options) {
        MapParams<K, V> params = (MapParams<K, V>) options;
        MapOptions<K, V> ops = MapOptions.<K, V>defaults()
                .loader(params.getLoader())
                .loaderAsync(params.getLoaderAsync())
                .writer(params.getWriter())
                .writerAsync(params.getWriterAsync())
                .writeBehindDelay(params.getWriteBehindDelay())
                .writeBehindBatchSize(params.getWriteBehindBatchSize())
                .writerRetryInterval(Duration.ofMillis(params.getWriteRetryInterval()));

        if (params.getWriteMode() != null) {
            ops.writeMode(MapOptions.WriteMode.valueOf(params.getWriteMode().toString()));
        }
        if (params.getWriteRetryAttempts() > 0) {
            ops.writerRetryAttempts(params.getWriteRetryAttempts());
        }

        return new RedissonMap<>(params.getCodec(), commandExecutor.copy(params), params.getName(),
                this, ops, writeBehindService);
    }

    /** 获取 {@link RMapCacheNative} 分布式对象。 */
    @Override
    public <K, V> RMapCacheNative<K, V> getMapCacheNative(String name) {
        return new RedissonMapCacheNative<>(commandExecutor, name, this, null, null);
    }

    /** 获取 {@link RMapCacheNative} 分布式对象。 */
    @Override
    public <K, V> RMapCacheNative<K, V> getMapCacheNative(String name, Codec codec) {
        return new RedissonMapCacheNative<>(codec, commandExecutor, name, this, null, null);
    }

    /** 获取 {@link RMapCacheNative} 分布式对象。 */
    @Override
    public <K, V> RMapCacheNative<K, V> getMapCacheNative(org.redisson.api.options.MapOptions<K, V> options) {
        MapParams<K, V> params = (MapParams<K, V>) options;
        MapOptions<K, V> ops = MapOptions.<K, V>defaults()
                .loader(params.getLoader())
                .loaderAsync(params.getLoaderAsync())
                .writer(params.getWriter())
                .writerAsync(params.getWriterAsync())
                .writeBehindDelay(params.getWriteBehindDelay())
                .writeBehindBatchSize(params.getWriteBehindBatchSize())
                .writerRetryInterval(Duration.ofMillis(params.getWriteRetryInterval()));

        if (params.getWriteMode() != null) {
            ops.writeMode(MapOptions.WriteMode.valueOf(params.getWriteMode().toString()));
        }
        if (params.getWriteRetryAttempts() > 0) {
            ops.writerRetryAttempts(params.getWriteRetryAttempts());
        }

        return new RedissonMapCacheNative<>(params.getCodec(), commandExecutor.copy(params), params.getName(),
                this, ops, writeBehindService);
    }

    /** 获取 {@link RSetMultimap} 分布式对象。 */
    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(String name) {
        return new RedissonSetMultimap<K, V>(commandExecutor, name);
    }

    /** 获取 {@link RSetMultimapCache} 分布式对象。 */
    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name) {
        return new RedissonSetMultimapCache<K, V>(evictionScheduler, commandExecutor, name);
    }

    /** 获取 {@link RSetMultimapCache} 分布式对象。 */
    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name, Codec codec) {
        return new RedissonSetMultimapCache<K, V>(evictionScheduler, codec, commandExecutor, name);
    }

    /** 获取 {@link RSetMultimapCache} 分布式对象。 */
    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonSetMultimapCache<K, V>(evictionScheduler, params.getCodec(),
                commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RSetMultimapCacheNative} 分布式对象。 */
    @Override
    public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(String name) {
        return new RedissonSetMultimapCacheNative<>(commandExecutor, name);
    }

    /** 获取 {@link RSetMultimapCacheNative} 分布式对象。 */
    @Override
    public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(String name, Codec codec) {
        return new RedissonSetMultimapCacheNative<>(codec, commandExecutor, name);
    }

    /** 获取 {@link RSetMultimapCacheNative} 分布式对象。 */
    @Override
    public <K, V> RSetMultimapCacheNative<K, V> getSetMultimapCacheNative(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonSetMultimapCacheNative<>(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RListMultimapCache} 分布式对象。 */
    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name) {
        return new RedissonListMultimapCache<K, V>(evictionScheduler, commandExecutor, name);
    }

    /** 获取 {@link RListMultimapCache} 分布式对象。 */
    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name, Codec codec) {
        return new RedissonListMultimapCache<K, V>(evictionScheduler, codec, commandExecutor, name);
    }

    /** 获取 {@link RListMultimapCache} 分布式对象。 */
    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonListMultimapCache<K, V>(evictionScheduler, params.getCodec(),
                commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RListMultimapCacheNative} 分布式对象。 */
    @Override
    public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(String name) {
        return new RedissonListMultimapCacheNative<K, V>(commandExecutor, name);
    }

    /** 获取 {@link RListMultimapCacheNative} 分布式对象。 */
    @Override
    public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(String name, Codec codec) {
        return new RedissonListMultimapCacheNative<>(codec, commandExecutor, name);
    }

    /** 获取 {@link RListMultimapCacheNative} 分布式对象。 */
    @Override
    public <K, V> RListMultimapCacheNative<K, V> getListMultimapCacheNative(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonListMultimapCacheNative<>(params.getCodec(),
                commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RSetMultimap} 分布式对象。 */
    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(String name, Codec codec) {
        return new RedissonSetMultimap<K, V>(codec, commandExecutor, name);
    }

    /** 获取 {@link RSetMultimap} 分布式对象。 */
    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonSetMultimap<>(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RSetCache} 分布式对象。 */
    @Override
    public <V> RSetCache<V> getSetCache(String name) {
        return new RedissonSetCache<V>(evictionScheduler, commandExecutor, name, this);
    }

    /** 获取 {@link RSetCache} 分布式对象。 */
    @Override
    public <V> RSetCache<V> getSetCache(String name, Codec codec) {
        return new RedissonSetCache<V>(codec, evictionScheduler, commandExecutor, name, this);
    }

    /** 获取 {@link RSetCache} 分布式对象。 */
    @Override
    public <V> RSetCache<V> getSetCache(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonSetCache<V>(params.getCodec(), evictionScheduler,
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RMapCache} 分布式对象。 */
    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name) {
        return new RedissonMapCache<K, V>(evictionScheduler, commandExecutor, name, this, null, null);
    }

    /** 获取 {@link RMapCache} 分布式对象。 */
    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, MapCacheOptions<K, V> options) {
        return new RedissonMapCache<K, V>(evictionScheduler, commandExecutor, name, this, options, writeBehindService);
    }

    /** 获取 {@link RMapCache} 分布式对象。 */
    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec) {
        return new RedissonMapCache<K, V>(codec, evictionScheduler, commandExecutor, name, this, null, null);
    }

    /** 获取 {@link RMapCache} 分布式对象。 */
    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec, MapCacheOptions<K, V> options) {
        return new RedissonMapCache<K, V>(codec, evictionScheduler, commandExecutor, name, this, options, writeBehindService);
    }

    /** 获取 {@link RMapCache} 分布式对象。 */
    @Override
    public <K, V> RMapCache<K, V> getMapCache(org.redisson.api.options.MapCacheOptions<K, V> options) {
        MapCacheParams<K, V> params = (MapCacheParams<K, V>) options;
        MapCacheOptions<K, V> ops = createOptions(params);
        return new RedissonMapCache<>(params.getCodec(), evictionScheduler,
                commandExecutor.copy(params), params.getName(), this, ops, writeBehindService);
    }

    /** Redisson 客户端 createOptions 方法。 */
    private static <K, V> MapCacheOptions<K, V> createOptions(MapCacheParams<K, V> params) {
        MapCacheOptions<K, V> ops = MapCacheOptions.<K, V>defaults()
                .loader(params.getLoader())
                .loaderAsync(params.getLoaderAsync())
                .writer(params.getWriter())
                .writerAsync(params.getWriterAsync())
                .writeBehindDelay(params.getWriteBehindDelay())
                .writeBehindBatchSize(params.getWriteBehindBatchSize())
                .writerRetryInterval(Duration.ofMillis(params.getWriteRetryInterval()));

        if (params.getWriteMode() != null) {
            ops.writeMode(MapOptions.WriteMode.valueOf(params.getWriteMode().toString()));
        }
        if (params.getWriteRetryAttempts() > 0) {
            ops.writerRetryAttempts(params.getWriteRetryAttempts());
        }

        if (params.isRemoveEmptyEvictionTask()) {
            ops.removeEmptyEvictionTask();
        }
        return ops;
    }

    /** 获取 Session 属性 Map 或通用 Redis Map。 */
    @Override
    public <K, V> RMap<K, V> getMap(String name, Codec codec) {
        return new RedissonMap<K, V>(codec, commandExecutor, name, this, null, null);
    }

    /** 获取 Session 属性 Map 或通用 Redis Map。 */
    @Override
    public <K, V> RMap<K, V> getMap(String name, Codec codec, MapOptions<K, V> options) {
        return new RedissonMap<K, V>(codec, commandExecutor, name, this, options, writeBehindService);
    }

    /** 获取分布式锁。 */
    @Override
    public RLock getLock(String name) {
        return new RedissonLock(commandExecutor, name);
    }

    /** 获取分布式锁。 */
    @Override
    public RLock getLock(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonLock(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RSpinLock} 分布式对象。 */
    @Override
    public RLock getSpinLock(String name) {
        return getSpinLock(name, LockOptions.defaults());
    }

    /** 获取 {@link RSpinLock} 分布式对象。 */
    @Override
    public RLock getSpinLock(String name, LockOptions.BackOff backOff) {
        return new RedissonSpinLock(commandExecutor, name, backOff);
    }

    /** 获取 {@link RNonReentrantLock} 分布式对象。 */
    @Override
    public RLock getNonReentrantLock(String name) {
        return new RedissonNonReentrantLock(commandExecutor, name);
    }

    /** 获取 {@link RNonReentrantLock} 分布式对象。 */
    @Override
    public RLock getNonReentrantLock(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonNonReentrantLock(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RFencedLock} 分布式对象。 */
    @Override
    public RFencedLock getFencedLock(String name) {
        return new RedissonFencedLock(commandExecutor, name);
    }

    /** 获取 {@link RFencedLock} 分布式对象。 */
    @Override
    public RFencedLock getFencedLock(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonFencedLock(commandExecutor.copy(params), params.getName());
    }

    /** 获取联锁（MultiLock）。 */
    @Override
    public RLock getMultiLock(RLock... locks) {
        return new RedissonMultiLock(locks);
    }

    /** 获取联锁（MultiLock）。 */
    @Override
    public RLock getMultiLock(String group, Collection<Object> values) {
        return new RedissonFasterMultiLock(commandExecutor, group, values);
    }

    /** 获取红锁（RedLock）。 */
    @Override
    public RLock getRedLock(RLock... locks) {
        if (printed.add(1)) {
            log.error("RedLock object is deprecated. Use RLock or RFencedLock object instead.");
        }
        return new RedissonRedLock(locks);
    }

    /** 获取公平锁。 */
    @Override
    public RLock getFairLock(String name) {
        return new RedissonFairLock(commandExecutor, name);
    }

    /** 获取公平锁。 */
    @Override
    public RLock getFairLock(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonFairLock(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RNonReentrantFairLock} 分布式对象。 */
    @Override
    public RLock getNonReentrantFairLock(String name) {
        return new RedissonNonReentrantFairLock(commandExecutor, name);
    }

    /** 获取 {@link RNonReentrantFairLock} 分布式对象。 */
    @Override
    public RLock getNonReentrantFairLock(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonNonReentrantFairLock(commandExecutor.copy(params), params.getName());
    }

    /** 获取读写锁。 */
    @Override
    public RReadWriteLock getReadWriteLock(String name) {
        return new RedissonReadWriteLock(commandExecutor, name);
    }

    /** 获取读写锁。 */
    @Override
    public RReadWriteLock getReadWriteLock(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonReadWriteLock(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RSet} 分布式对象。 */
    @Override
    public <V> RSet<V> getSet(String name) {
        return new RedissonSet<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RSet} 分布式对象。 */
    @Override
    public <V> RSet<V> getSet(String name, Codec codec) {
        return new RedissonSet<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RSet} 分布式对象。 */
    @Override
    public <V> RSet<V> getSet(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonSet<V>(params.getCodec(), commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RFunction} 分布式对象。 */
    @Override
    public RFunction getFunction() {
        return new RedissonFuction(commandExecutor);
    }

    /** 获取 {@link RFunction} 分布式对象。 */
    @Override
    public RFunction getFunction(Codec codec) {
        return new RedissonFuction(commandExecutor, codec);
    }

    /** 获取 {@link RFunction} 分布式对象。 */
    @Override
    public RFunction getFunction(OptionalOptions options) {
        OptionalParams params = (OptionalParams) options;
        return new RedissonFuction(commandExecutor.copy(params), params.getCodec());
    }

    /** 获取 {@link RScript} 分布式对象。 */
    @Override
    public RScript getScript() {
        return new RedissonScript(commandExecutor);
    }
    
    /** 获取 {@link RScript} 分布式对象。 */
    @Override
    public RScript getScript(Codec codec) {
        return new RedissonScript(commandExecutor, codec);
    }

    /** 获取 {@link RScript} 分布式对象。 */
    @Override
    public RScript getScript(OptionalOptions options) {
        OptionalParams params = (OptionalParams) options;
        return new RedissonScript(commandExecutor.copy(params), params.getCodec());
    }

    /** 获取 {@link RVectorSet} 分布式对象。 */
    @Override
    public RVectorSet getVectorSet(String name) {
        return new RedissonVectorSet(commandExecutor, name);
    }

    /** 获取 {@link RVectorSet} 分布式对象。 */
    @Override
    public RVectorSet getVectorSet(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonVectorSet(commandExecutor.copy(params), params.getName());
    }

    /** 返回分布式任务执行器。 */
    @Override
    public RScheduledExecutorService getExecutorService(String name) {
        return getExecutorService(name, connectionManager.getServiceManager().getCfg().getCodec());
    }

    /** 返回分布式任务执行器。 */
    @Override
    public RScheduledExecutorService getExecutorService(String name, ExecutorOptions options) {
        return getExecutorService(name, connectionManager.getServiceManager().getCfg().getCodec(), options);
    }

    /** 返回分布式任务执行器。 */
    @Override
    public RScheduledExecutorService getExecutorService(String name, Codec codec) {
        return getExecutorService(name, codec, ExecutorOptions.defaults());
    }

    /** 返回分布式任务执行器。 */
    @Override
    public RScheduledExecutorService getExecutorService(String name, Codec codec, ExecutorOptions options) {
        return new RedissonExecutorService(codec, commandExecutor, this, name, options);
    }

    /** 返回分布式任务执行器。 */
    @Override
    public RScheduledExecutorService getExecutorService(org.redisson.api.options.ExecutorOptions options) {
        ExecutorParams params = (ExecutorParams) options;
        ExecutorOptions ops = ExecutorOptions.defaults()
                                            .idGenerator(params.getIdGenerator())
                                            .taskRetryInterval(params.getTaskRetryInterval(), TimeUnit.MILLISECONDS);
        return new RedissonExecutorService(params.getCodec(),
                commandExecutor.copy(params), this, params.getName(), ops);
    }

    /** 返回远程服务执行器。 */
    @Override
    public RRemoteService getRemoteService() {
        return getRemoteService("redisson_rs", connectionManager.getServiceManager().getCfg().getCodec());
    }

    /** 返回远程服务执行器。 */
    @Override
    public RRemoteService getRemoteService(String name) {
        return getRemoteService(name, connectionManager.getServiceManager().getCfg().getCodec());
    }

    /** 返回远程服务执行器。 */
    @Override
    public RRemoteService getRemoteService(Codec codec) {
        return getRemoteService("redisson_rs", codec);
    }

    /** 返回远程服务执行器。 */
    @Override
    public RRemoteService getRemoteService(String name, Codec codec) {
        String executorId = connectionManager.getServiceManager().getId();
        if (codec != connectionManager.getServiceManager().getCfg().getCodec()) {
            executorId = executorId + ":" + name;
        }
        return new RedissonRemoteService(codec, name, commandExecutor, executorId);
    }

    /** 返回远程服务执行器。 */
    @Override
    public RRemoteService getRemoteService(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        String executorId = connectionManager.getServiceManager().getId();
        if (params.getCodec() != null
                && params.getCodec() != connectionManager.getServiceManager().getCfg().getCodec()) {
            executorId = executorId + ":" + params.getName();
        }
        return new RedissonRemoteService(params.getCodec(), params.getName(), commandExecutor.copy(params), executorId);
    }

    /** 获取 {@link RSortedSet} 分布式对象。 */
    @Override
    public <V> RSortedSet<V> getSortedSet(String name) {
        return new RedissonSortedSet<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RSortedSet} 分布式对象。 */
    @Override
    public <V> RSortedSet<V> getSortedSet(String name, Codec codec) {
        return new RedissonSortedSet<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RSortedSet} 分布式对象。 */
    @Override
    public <V> RSortedSet<V> getSortedSet(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonSortedSet<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RScoredSortedSet} 分布式对象。 */
    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(String name) {
        return new RedissonScoredSortedSet<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RScoredSortedSet} 分布式对象。 */
    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(String name, Codec codec) {
        return new RedissonScoredSortedSet<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RScoredSortedSet} 分布式对象。 */
    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonScoredSortedSet<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RLexSortedSet} 分布式对象。 */
    @Override
    public RLexSortedSet getLexSortedSet(String name) {
        return new RedissonLexSortedSet(commandExecutor, name, this);
    }

    /** 获取 {@link RLexSortedSet} 分布式对象。 */
    @Override
    public RLexSortedSet getLexSortedSet(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonLexSortedSet(commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RShardedTopic} 分布式对象。 */
    @Override
    public RShardedTopic getShardedTopic(String name) {
        return new RedissonShardedTopic(commandExecutor, name);
    }

    /** 获取 {@link RShardedTopic} 分布式对象。 */
    @Override
    public RShardedTopic getShardedTopic(String name, Codec codec) {
        return new RedissonShardedTopic(codec, commandExecutor, name);
    }

    /** 获取 {@link RShardedTopic} 分布式对象。 */
    @Override
    public RShardedTopic getShardedTopic(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonShardedTopic(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 返回 Session 集群同步 Topic。 */
    @Override
    public RTopic getTopic(String name) {
        return new RedissonTopic(commandExecutor, name);
    }

    /** 返回 Session 集群同步 Topic。 */
    @Override
    public RTopic getTopic(String name, Codec codec) {
        return new RedissonTopic(codec, commandExecutor, name);
    }

    /** 返回 Session 集群同步 Topic。 */
    @Override
    public RTopic getTopic(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonTopic(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RReliableTopic} 分布式对象。 */
    @Override
    public RReliableTopic getReliableTopic(String name) {
        return new RedissonReliableTopic(commandExecutor, name);
    }

    /** 获取 {@link RReliableTopic} 分布式对象。 */
    @Override
    public RReliableTopic getReliableTopic(String name, Codec codec) {
        return new RedissonReliableTopic(codec, commandExecutor, name);
    }

    /** 获取 {@link RReliableTopic} 分布式对象。 */
    @Override
    public RReliableTopic getReliableTopic(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonReliableTopic(params.getCodec(),
                commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RReliablePubSubTopic} 分布式对象。 */
    @Override
    public <V> RReliablePubSubTopic<V> getReliablePubSubTopic(String name) {
        throw new UnsupportedOperationException("This feature is implemented in the Redisson PRO version. Please refer to https://redisson.pro/feature-comparison.html");
    }

    /** 获取 {@link RReliablePubSubTopic} 分布式对象。 */
    @Override
    public <V> RReliablePubSubTopic<V> getReliablePubSubTopic(String name, Codec codec) {
        throw new UnsupportedOperationException("This feature is implemented in the Redisson PRO version. Please refer to https://redisson.pro/feature-comparison.html");
    }

    /** 获取 {@link RReliablePubSubTopic} 分布式对象。 */
    @Override
    public <V> RReliablePubSubTopic<V> getReliablePubSubTopic(PlainOptions options) {
        throw new UnsupportedOperationException("This feature is implemented in the Redisson PRO version. Please refer to https://redisson.pro/feature-comparison.html");
    }

    /** 获取 {@link RPatternTopic} 分布式对象。 */
    @Override
    public RPatternTopic getPatternTopic(String pattern) {
        return new RedissonPatternTopic(commandExecutor, pattern);
    }

    /** 获取 {@link RPatternTopic} 分布式对象。 */
    @Override
    public RPatternTopic getPatternTopic(String pattern, Codec codec) {
        return new RedissonPatternTopic(codec, commandExecutor, pattern);
    }

    /** 获取 {@link RPatternTopic} 分布式对象。 */
    @Override
    public RPatternTopic getPatternTopic(PatternTopicOptions options) {
        PatternTopicParams params = (PatternTopicParams) options;
        return new RedissonPatternTopic(params.getCodec(), commandExecutor.copy(params), params.getPattern());
    }

    /** 获取 {@link RDelayedQueue} 分布式对象。 */
    @Override
    public <V> RDelayedQueue<V> getDelayedQueue(RQueue<V> destinationQueue) {
        if (printed.add(2)) {
            log.error("RDelayedQueue object is deprecated due to github issues #3020, #2998, #1057. Use RReliableQueue object instead.");
        }

        if (destinationQueue == null) {
            throw new NullPointerException();
        }
        return new RedissonDelayedQueue<V>(destinationQueue.getCodec(), commandExecutor, destinationQueue.getName());
    }

    /** 获取 {@link RReliableQueue} 分布式对象。 */
    @Override
    public <V> RReliableQueue<V> getReliableQueue(String name) {
        throw new UnsupportedOperationException("This feature is implemented in the Redisson PRO version. Please refer to https://redisson.pro/feature-comparison.html");
    }

    /** 获取 {@link RReliableQueue} 分布式对象。 */
    @Override
    public <V> RReliableQueue<V> getReliableQueue(String name, Codec codec) {
        throw new UnsupportedOperationException("This feature is implemented in the Redisson PRO version. Please refer to https://redisson.pro/feature-comparison.html");
    }

    /** 获取 {@link RReliableQueue} 分布式对象。 */
    @Override
    public <V> RReliableQueue<V> getReliableQueue(PlainOptions options) {
        throw new UnsupportedOperationException("This feature is implemented in the Redisson PRO version. Please refer to https://redisson.pro/feature-comparison.html");
    }

    /** 获取 {@link RQueue} 分布式对象。 */
    @Override
    public <V> RQueue<V> getQueue(String name) {
        return new RedissonQueue<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RQueue} 分布式对象。 */
    @Override
    public <V> RQueue<V> getQueue(String name, Codec codec) {
        return new RedissonQueue<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RQueue} 分布式对象。 */
    @Override
    public <V> RQueue<V> getQueue(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonQueue<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RTransferQueue} 分布式对象。 */
    @Override
    public <V> RTransferQueue<V> getTransferQueue(String name) {
        String remoteName = RedissonObject.suffixName(name, "remoteService");
        RRemoteService service = getRemoteService(remoteName);
        return new RedissonTransferQueue<V>(commandExecutor, name, service);
    }

    /** 获取 {@link RTransferQueue} 分布式对象。 */
    @Override
    public <V> RTransferQueue<V> getTransferQueue(String name, Codec codec) {
        String remoteName = RedissonObject.suffixName(name, "remoteService");
        RRemoteService service = getRemoteService(remoteName);
        return new RedissonTransferQueue<V>(codec, commandExecutor, name, service);
    }

    /** 获取 {@link RTransferQueue} 分布式对象。 */
    @Override
    public <V> RTransferQueue<V> getTransferQueue(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        String remoteName = RedissonObject.suffixName(params.getName(), "remoteService");
        RRemoteService service = getRemoteService(remoteName);
        return new RedissonTransferQueue<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), service);
    }

    /** 获取 {@link RRingBuffer} 分布式对象。 */
    @Override
    public <V> RRingBuffer<V> getRingBuffer(String name) {
        return new RedissonRingBuffer<V>(commandExecutor, name, this);
    }
    
    /** 获取 {@link RRingBuffer} 分布式对象。 */
    @Override
    public <V> RRingBuffer<V> getRingBuffer(String name, Codec codec) {
        return new RedissonRingBuffer<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RRingBuffer} 分布式对象。 */
    @Override
    public <V> RRingBuffer<V> getRingBuffer(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonRingBuffer<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RCircularBuffer} 分布式对象。 */
    @Override
    public <V> RCircularBuffer<V> getCircularBuffer(String name) {
        return new RedissonCircularBuffer<>(commandExecutor, name);
    }

    /** 获取 {@link RCircularBuffer} 分布式对象。 */
    @Override
    public <V> RCircularBuffer<V> getCircularBuffer(String name, Codec codec) {
        return new RedissonCircularBuffer<>(codec, commandExecutor, name);
    }

    /** 获取 {@link RCircularBuffer} 分布式对象。 */
    @Override
    public <V> RCircularBuffer<V> getCircularBuffer(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonCircularBuffer<>(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RBlockingQueue} 分布式对象。 */
    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(String name) {
        return new RedissonBlockingQueue<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RBlockingQueue} 分布式对象。 */
    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(String name, Codec codec) {
        return new RedissonBlockingQueue<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RBlockingQueue} 分布式对象。 */
    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonBlockingQueue<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RBoundedBlockingQueue} 分布式对象。 */
    @Override
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name) {
        if (printed.add(5)) {
            log.error("RBoundedBlockingQueue object is deprecated due to github issues #3979, #3835, #4481, #5104, #5575, #5653. Instead, use the RReliableQueue object with delay feature.");
        }

        return new RedissonBoundedBlockingQueue<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RBoundedBlockingQueue} 分布式对象。 */
    @Override
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name, Codec codec) {
        if (printed.add(4)) {
            log.error("RBoundedBlockingQueue object is deprecated due to github issues #3979, #3835, #4481, #5104, #5575, #5653. Instead, use the RReliableQueue object with delay feature.");
        }

        return new RedissonBoundedBlockingQueue<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RBoundedBlockingQueue} 分布式对象。 */
    @Override
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(PlainOptions options) {
        if (printed.add(3)) {
            log.error("RBoundedBlockingQueue object is deprecated due to github issues #3979, #3835, #4481, #5104, #5575, #5653. Instead, use the RReliableQueue object with delay feature.");
        }

        PlainParams params = (PlainParams) options;
        return new RedissonBoundedBlockingQueue<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RDeque} 分布式对象。 */
    @Override
    public <V> RDeque<V> getDeque(String name) {
        return new RedissonDeque<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RDeque} 分布式对象。 */
    @Override
    public <V> RDeque<V> getDeque(String name, Codec codec) {
        return new RedissonDeque<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RDeque} 分布式对象。 */
    @Override
    public <V> RDeque<V> getDeque(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonDeque<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RBlockingDeque} 分布式对象。 */
    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(String name) {
        return new RedissonBlockingDeque<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RBlockingDeque} 分布式对象。 */
    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(String name, Codec codec) {
        return new RedissonBlockingDeque<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RBlockingDeque} 分布式对象。 */
    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonBlockingDeque<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RAtomicLong} 分布式对象。 */
    @Override
    public RAtomicLong getAtomicLong(String name) {
        return new RedissonAtomicLong(commandExecutor, name);
    }

    /** 获取 {@link RAtomicLong} 分布式对象。 */
    @Override
    public RAtomicLong getAtomicLong(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonAtomicLong(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RLongAdder} 分布式对象。 */
    @Override
    public RLongAdder getLongAdder(String name) {
        return new RedissonLongAdder(commandExecutor, name, this);
    }

    /** 获取 {@link RLongAdder} 分布式对象。 */
    @Override
    public RLongAdder getLongAdder(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonLongAdder(commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RDoubleAdder} 分布式对象。 */
    @Override
    public RDoubleAdder getDoubleAdder(String name) {
        return new RedissonDoubleAdder(commandExecutor, name, this);
    }

    /** 获取 {@link RDoubleAdder} 分布式对象。 */
    @Override
    public RDoubleAdder getDoubleAdder(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonDoubleAdder(commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RAtomicDouble} 分布式对象。 */
    @Override
    public RAtomicDouble getAtomicDouble(String name) {
        return new RedissonAtomicDouble(commandExecutor, name);
    }

    /** 获取 {@link RAtomicDouble} 分布式对象。 */
    @Override
    public RAtomicDouble getAtomicDouble(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonAtomicDouble(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RCountDownLatch} 分布式对象。 */
    @Override
    public RCountDownLatch getCountDownLatch(String name) {
        return new RedissonCountDownLatch(commandExecutor, name);
    }

    /** 获取 {@link RCountDownLatch} 分布式对象。 */
    @Override
    public RCountDownLatch getCountDownLatch(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonCountDownLatch(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RBitSet} 分布式对象。 */
    @Override
    public RBitSet getBitSet(String name) {
        return new RedissonBitSet(commandExecutor, name);
    }

    /** 获取 {@link RBitSet} 分布式对象。 */
    @Override
    public RBitSet getBitSet(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonBitSet(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RBitVectorStore} 分布式对象。 */
    @Override
    public <K> RBitVectorStore<K> getBitVectorStore(String name) {
        throw new UnsupportedOperationException("This feature is implemented in the Redisson PRO version. Please refer to https://redisson.pro/feature-comparison.html");
    }

    /** 获取 {@link RBitVectorStore} 分布式对象。 */
    @Override
    public <K> RBitVectorStore<K> getBitVectorStore(String name, Codec codec) {
        throw new UnsupportedOperationException("This feature is implemented in the Redisson PRO version. Please refer to https://redisson.pro/feature-comparison.html");
    }

    /** 获取 {@link RBitVectorStore} 分布式对象。 */
    @Override
    public <K> RBitVectorStore<K> getBitVectorStore(PlainOptions options) {
        throw new UnsupportedOperationException("This feature is implemented in the Redisson PRO version. Please refer to https://redisson.pro/feature-comparison.html");
    }

    /** 获取 {@link RSemaphore} 分布式对象。 */
    @Override
    public RSemaphore getSemaphore(String name) {
        return new RedissonSemaphore(commandExecutor, name);
    }

    /** 获取 {@link RSemaphore} 分布式对象。 */
    @Override
    public RSemaphore getSemaphore(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonSemaphore(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RPermitExpirableSemaphore} 分布式对象。 */
    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(String name) {
        return new RedissonPermitExpirableSemaphore(commandExecutor, name);
    }

    /** 获取 {@link RPermitExpirableSemaphore} 分布式对象。 */
    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonPermitExpirableSemaphore(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RBloomFilter} 分布式对象。 */
    @Override
    public <V> RBloomFilter<V> getBloomFilter(String name) {
        return new RedissonBloomFilter<V>(commandExecutor, name);
    }

    /** 获取 {@link RBloomFilter} 分布式对象。 */
    @Override
    public <V> RBloomFilter<V> getBloomFilter(String name, Codec codec) {
        return new RedissonBloomFilter<V>(codec, commandExecutor, name);
    }

    /** 获取 {@link RBloomFilter} 分布式对象。 */
    @Override
    public <V> RBloomFilter<V> getBloomFilter(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonBloomFilter<V>(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RBloomFilterNative} 分布式对象。 */
    @Override
    public <V> RBloomFilterNative<V> getBloomFilterNative(String name) {
        return new RedissonBloomFilterNative<V>(commandExecutor, name);
    }

    /** 获取 {@link RBloomFilterNative} 分布式对象。 */
    @Override
    public <V> RBloomFilterNative<V> getBloomFilterNative(String name, Codec codec) {
        return new RedissonBloomFilterNative<V>(codec, commandExecutor, name);
    }

    /** 获取 {@link RCuckooFilter} 分布式对象。 */
    @Override
    public <V> RCuckooFilter<V> getCuckooFilter(String name) {
        return getCuckooFilter(name, null);
    }

    /** 获取 {@link RCuckooFilter} 分布式对象。 */
    @Override
    public <V> RCuckooFilter<V> getCuckooFilter(String name, Codec codec) {
        return new RedissonCuckooFilter<V>(codec, commandExecutor, name);
    }

    /** 获取 {@link RCuckooFilter} 分布式对象。 */
    @Override
    public <V> RCuckooFilter<V> getCuckooFilter(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonCuckooFilter<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RTDigest} 分布式对象。 */
    @Override
    public RTDigest getTDigest(String name) {
        return new RedissonTDigest(commandExecutor, name);
    }

    /** 获取 {@link RTDigest} 分布式对象。 */
    @Override
    public RTDigest getTDigest(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonTDigest(commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RTopK} 分布式对象。 */
    @Override
    public <V> RTopK<V> getTopK(String name) {
        return getTopK(name, null);
    }

    /** 获取 {@link RTopK} 分布式对象。 */
    @Override
    public <V> RTopK<V> getTopK(String name, Codec codec) {
        return new RedissonTopK<V>(codec, commandExecutor, name);
    }

    /** 获取 {@link RTopK} 分布式对象。 */
    @Override
    public <V> RTopK<V> getTopK(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonTopK<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName());
    }


    /** 获取 {@link RBloomFilterNative} 分布式对象。 */
    @Override
    public <V> RBloomFilterNative<V> getBloomFilterNative(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonBloomFilterNative<V>(params.getCodec(), commandExecutor.copy(params), params.getName());
    }

    /** 获取 {@link RIdGenerator} 分布式对象。 */
    @Override
    public RIdGenerator getIdGenerator(String name) {
        return new RedissonIdGenerator(commandExecutor, name);
    }

    /** 获取 {@link RIdGenerator} 分布式对象。 */
    @Override
    public RIdGenerator getIdGenerator(CommonOptions options) {
        CommonParams params = (CommonParams) options;
        return new RedissonIdGenerator(commandExecutor.copy(params), params.getName());
    }

    /** 返回全部键（慎用）。 */
    @Override
    public RKeys getKeys() {
        return new RedissonKeys(commandExecutor);
    }

    /** 返回全部键（慎用）。 */
    @Override
    public RKeys getKeys(KeysOptions options) {
        KeysParams params = (KeysParams) options;
        return new RedissonKeys(commandExecutor.copy(params));
    }

    /** 创建 Redis 事务。 */
    @Override
    public RTransaction createTransaction(TransactionOptions options) {
        return new RedissonTransaction(commandExecutor, options);
    }

    /** 创建命令批处理。 */
    @Override
    public RBatch createBatch(BatchOptions options) {
        return new RedissonBatch(evictionScheduler, commandExecutor, options);
    }

    /** 创建命令批处理。 */
    @Override
    public RBatch createBatch() {
        return createBatch(BatchOptions.defaults());
    }

    /** 返回 Live Object 服务。 */
    @Override
    public RLiveObjectService getLiveObjectService() {
        return new RedissonLiveObjectService(liveObjectClassCache, commandExecutor);
    }

    /** 返回 Live Object 服务。 */
    @Override
    public RLiveObjectService getLiveObjectService(LiveObjectOptions options) {
        LiveObjectParams params = (LiveObjectParams) options;
        return new RedissonLiveObjectService(liveObjectClassCache, commandExecutor.copy(params));
    }

    /** 获取 {@link RClientSideCaching} 分布式对象。 */
    @Override
    public RClientSideCaching getClientSideCaching(ClientSideCachingOptions options) {
        if (!getServiceManager().isResp3()) {
            throw new IllegalStateException("'protocol' config setting should be set to RESP3 value. "
                    + System.lineSeparator() + System.lineSeparator() +
                    "NOTE: client side caching feature invalidates whole Map per entry change which is ineffective. " +
                    "Use local cached https://redisson.org/docs/data-and-services/collections/#eviction-local-cache-and-data-partitioning or https://redisson.org/docs/data-and-services/collections/#local-cache instead.");
        }
        return new RedissonClientSideCaching(commandExecutor, options);
    }

    /** 关闭 Redisson 客户端。 */
    @Override
    public void shutdown() {
        writeBehindService.stop();
        connectionManager.shutdown();
    }

    /** 异步执行 shutdown。 */
    @Override
    public CompletionStage<Void> shutdownAsync() {
        return shutdownAsync(Duration.ZERO, Duration.ofSeconds(2));
    }

    /** 关闭 Redisson 客户端。 */
    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
        writeBehindService.stop();
        connectionManager.shutdown(quietPeriod, timeout, unit);
    }

    /** 异步执行 shutdown。 */
    @Override
    public CompletionStage<Void> shutdownAsync(Duration quietPeriod, Duration timeout) {
        writeBehindService.stop();
        return connectionManager.shutdownAsync(quietPeriod.toNanos(), timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    /** 获取 {@link RConfig} 分布式对象。 */
    @Override
    public Config getConfig() {
        return config;
    }

    /** 获取 {@link RRedisNodes} 分布式对象。 */
    @Override
    public <T extends BaseRedisNodes> T getRedisNodes(org.redisson.api.redisnode.RedisNodes<T> nodes) {
        if (nodes.getClazz() == RedisSingle.class) {
            if (config.isSentinelConfig() || config.isClusterConfig()) {
                throw new IllegalArgumentException("Can't be used in non Redis single configuration");
            }
            return (T) new RedissonSingleNode(connectionManager, commandExecutor);
        }
        if (nodes.getClazz() == RedisCluster.class) {
            if (!config.isClusterConfig()) {
                throw new IllegalArgumentException("Can't be used in non Redis Cluster configuration");
            }
            return (T) new RedissonClusterNodes(connectionManager, commandExecutor);
        }
        if (nodes.getClazz() == RedisSentinelMasterSlave.class) {
            if (!config.isSentinelConfig()) {
                throw new IllegalArgumentException("Can't be used in non Redis Sentinel configuration");
            }
            return (T) new RedissonSentinelMasterSlaveNodes(connectionManager, commandExecutor);
        }
        if (nodes.getClazz() == RedisMasterSlave.class) {
            if (config.isSentinelConfig() || config.isClusterConfig()) {
                throw new IllegalArgumentException("Can't be used in non Redis Master Slave configuration");
            }
            return (T) new RedissonMasterSlaveNodes(connectionManager, commandExecutor);
        }
        throw new IllegalArgumentException();
    }

    /** 客户端是否已关闭。 */
    @Override
    public boolean isShutdown() {
        return connectionManager.getServiceManager().isShutdown();
    }

    /** 客户端是否正在关闭。 */
    @Override
    public boolean isShuttingDown() {
        return connectionManager.getServiceManager().isShuttingDown();
    }

    /** 获取 {@link RPriorityQueue} 分布式对象。 */
    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(String name) {
        return new RedissonPriorityQueue<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RPriorityQueue} 分布式对象。 */
    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(String name, Codec codec) {
        return new RedissonPriorityQueue<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RPriorityQueue} 分布式对象。 */
    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonPriorityQueue<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RPriorityBlockingQueue} 分布式对象。 */
    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name) {
        return new RedissonPriorityBlockingQueue<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RPriorityBlockingQueue} 分布式对象。 */
    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name, Codec codec) {
        return new RedissonPriorityBlockingQueue<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RPriorityBlockingQueue} 分布式对象。 */
    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonPriorityBlockingQueue<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RPriorityBlockingDeque} 分布式对象。 */
    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name) {
        return new RedissonPriorityBlockingDeque<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RPriorityBlockingDeque} 分布式对象。 */
    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name, Codec codec) {
        return new RedissonPriorityBlockingDeque<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RPriorityBlockingDeque} 分布式对象。 */
    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonPriorityBlockingDeque<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 获取 {@link RPriorityDeque} 分布式对象。 */
    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(String name) {
        return new RedissonPriorityDeque<V>(commandExecutor, name, this);
    }

    /** 获取 {@link RPriorityDeque} 分布式对象。 */
    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(String name, Codec codec) {
        return new RedissonPriorityDeque<V>(codec, commandExecutor, name, this);
    }

    /** 获取 {@link RPriorityDeque} 分布式对象。 */
    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(PlainOptions options) {
        PlainParams params = (PlainParams) options;
        return new RedissonPriorityDeque<V>(params.getCodec(),
                commandExecutor.copy(params), params.getName(), this);
    }

    /** 返回对象名称/ID。 */
    @Override
    public String getId() {
        return connectionManager.getServiceManager().getId();
    }

}
