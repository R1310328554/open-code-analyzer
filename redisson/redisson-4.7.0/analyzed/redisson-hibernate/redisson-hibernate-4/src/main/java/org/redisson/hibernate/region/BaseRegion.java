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
package org.redisson.hibernate.region;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.GeneralDataRegion;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.TransactionalDataRegion;
import org.redisson.api.RFuture;
import org.redisson.api.RMapCache;
import org.redisson.connection.ServiceManager;
import org.redisson.hibernate.RedissonRegionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 缓存 Region 的抽象基类，实现 {@link TransactionalDataRegion} 与 {@link GeneralDataRegion}。
 * 封装 {@link RMapCache} 的读写、驱逐、TTL 及 Redis 不可用时的 fallback 降级逻辑。
 *
 * @author Nikita Koksharov
 *
 */
public class BaseRegion implements TransactionalDataRegion, GeneralDataRegion {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    final RMapCache<Object, Object> mapCache;
    final RegionFactory regionFactory;
    final CacheDataDescription metadata;
    final ServiceManager serviceManager;
    
    int ttl;
    int maxIdle;
    int size;
    boolean fallback;
    volatile boolean fallbackMode;

    /** 根据 Hibernate 属性初始化 TTL、maxIdle、maxSize 及 fallback 模式。 */
    public BaseRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager, RegionFactory regionFactory, CacheDataDescription metadata, Properties properties, String defaultKey) {
        super();
        this.mapCache = mapCache;
        this.regionFactory = regionFactory;
        this.metadata = metadata;
        this.serviceManager = serviceManager;
        
        String maxEntries = getProperty(properties, mapCache.getName(), defaultKey, RedissonRegionFactory.MAX_ENTRIES_SUFFIX);
        if (maxEntries != null) {
            size = Integer.parseInt(maxEntries);
            mapCache.setMaxSize(size);
        }
        String timeToLive = getProperty(properties, mapCache.getName(), defaultKey, RedissonRegionFactory.TTL_SUFFIX);
        if (timeToLive != null) {
            ttl = Integer.parseInt(timeToLive);
        }
        String maxIdleTime = getProperty(properties, mapCache.getName(), defaultKey, RedissonRegionFactory.MAX_IDLE_SUFFIX);
        if (maxIdleTime != null) {
            maxIdle = Integer.parseInt(maxIdleTime);
        }

        String fallbackValue = (String) properties.getOrDefault(RedissonRegionFactory.FALLBACK, "false");
        fallback = Boolean.parseBoolean(fallbackValue);
    }

    private String getProperty(Properties properties, String name, String defaultKey, String suffix) {
        String value = properties.getProperty(RedissonRegionFactory.CONFIG_PREFIX + name + suffix);
        if (value != null) {
            return value;
        }
        String defValue = properties.getProperty(RedissonRegionFactory.CONFIG_PREFIX + defaultKey + suffix);
        if (defValue != null) {
            return defValue;
        }
        return null;
    }

    /** 异步探测 Redis 连通性；恢复后退出 fallback 模式，否则继续定时重试。 */
    private void ping() {
        fallbackMode = true;
        serviceManager.newTimeout(t -> {
            RFuture<Boolean> future = mapCache.isExistsAsync();
            future.whenComplete((r, ex) -> {
                if (ex == null) {
                    fallbackMode = false;
                } else {
                    ping();
                }
            });
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean isTransactionAware() {
        // 当前实现不参与 Hibernate 事务同步
        return false;
    }
    
    @Override
    public CacheDataDescription getCacheDataDescription() {
        return metadata;
    }

    @Override
    public String getName() {
        return mapCache.getName();
    }

    @Override
    public void destroy() throws CacheException {
        try {
            mapCache.destroy();
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public boolean contains(Object key) {
        if (fallbackMode) {
            return false;
        }
        try {
            return mapCache.containsKey(key);
        } catch (Exception e) {
            if (fallback) {
                ping();
                logger.error(e.getMessage(), e);
                return false;
            }
            throw new CacheException(e);
        }
    }

    @Override
    public long getSizeInMemory() {
        return mapCache.sizeInMemory();
    }

    @Override
    public long getElementCountInMemory() {
        return mapCache.size();
    }

    @Override
    public long getElementCountOnDisk() {
        return -1;
    }

    @Override
    public Map<?, ?> toMap() {
        return Collections.unmodifiableMap(mapCache);
    }

    @Override
    public long nextTimestamp() {
        return regionFactory.nextTimestamp();
    }

    @Override
    public int getTimeout() {
        // 60 秒（Hibernate 规范化后的超时值）
        return (1 << 12) * 60000;
    }

    /** 从缓存读取条目；fallback 模式下返回 null 而不访问 Redis。 */
    @Override
    public Object get(Object key) throws CacheException {
        if (fallbackMode) {
            return null;
        }
        try {
            if (maxIdle == 0 && size == 0) {
                return mapCache.getWithTTLOnly(key);
            }
            return mapCache.get(key);
        } catch (Exception e) {
            if (fallback) {
                ping();
                logger.error(e.getMessage(), e);
                return null;
            }
            throw new CacheException(e);
        }
    }

    /** 写入缓存条目，应用 Region 配置的 TTL 与 maxIdle。 */
    @Override
    public void put(Object key, Object value) throws CacheException {
        if (fallbackMode) {
            return;
        }
        try {
            mapCache.fastPut(key, value, ttl, TimeUnit.MILLISECONDS, maxIdle, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (fallback) {
                ping();
                logger.error(e.getMessage(), e);
                return;
            }
            throw new CacheException(e);
        }
    }

    /** 移除指定键的缓存条目。 */
    @Override
    public void evict(Object key) throws CacheException {
        if (fallbackMode) {
            return;
        }
        try {
            mapCache.fastRemove(key);
        } catch (Exception e) {
            if (fallback) {
                ping();
                logger.error(e.getMessage(), e);
                return;
            }
            throw new CacheException(e);
        }
    }

    /** 清空整个 Region 缓存。 */
    @Override
    public void evictAll() throws CacheException {
        if (fallbackMode) {
            return;
        }
        try {
            mapCache.clear();
        } catch (Exception e) {
            if (fallback) {
                ping();
                logger.error(e.getMessage(), e);
                return;
            }
            throw new CacheException(e);
        }
    }

}
