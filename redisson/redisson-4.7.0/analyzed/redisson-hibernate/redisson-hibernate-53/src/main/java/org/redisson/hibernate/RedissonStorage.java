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
package org.redisson.hibernate;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.redisson.api.RFuture;
import org.redisson.api.RMapCache;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redisson {@link RMapCache} 上的 Hibernate {@link DomainDataStorageAccess} 实现（Hibernate 5.3）。
 * <p>支持 TTL、max_idle、max_entries 配置及 Redis 不可用时的 fallback 降级。
 *
 * @author Nikita Koksharov
 */
public class RedissonStorage implements DomainDataStorageAccess {

    private static final Logger logger = LoggerFactory.getLogger(RedissonStorage.class);

    private final RMapCache<Object, Object> mapCache;

    private final ServiceManager serviceManager;

    int ttl;
    int maxIdle;
    int size;
    boolean fallback;
    volatile boolean fallbackMode;
    
    /** 从 Hibernate 属性解析 TTL、max_idle、max_entries 与 fallback 并应用到 {@link RMapCache}。
     *
     * @param regionName Region 逻辑名
     * @param mapCache 底层 Redisson Map 缓存
     * @param serviceManager Redisson 服务管理器（用于 fallback 心跳）
     * @param properties Hibernate 缓存属性
     * @param defaultKey 默认配置键后缀（entity/collection 等）
     */
    public RedissonStorage(String regionName, RMapCache<Object, Object> mapCache, ServiceManager serviceManager, Map<String, Object> properties, String defaultKey) {
        super();
        this.mapCache = mapCache;
        this.serviceManager = serviceManager;
        
        String maxEntries = getProperty(properties, mapCache.getName(), regionName, defaultKey, RedissonRegionFactory.MAX_ENTRIES_SUFFIX);
        if (maxEntries != null) {
            size = Integer.parseInt(maxEntries);
            mapCache.setMaxSize(size);
        }
        String timeToLive = getProperty(properties, mapCache.getName(), regionName, defaultKey, RedissonRegionFactory.TTL_SUFFIX);
        if (timeToLive != null) {
            ttl = Integer.parseInt(timeToLive);
        }
        String maxIdleTime = getProperty(properties, mapCache.getName(), regionName, defaultKey, RedissonRegionFactory.MAX_IDLE_SUFFIX);
        if (maxIdleTime != null) {
            maxIdle = Integer.parseInt(maxIdleTime);
        }

        String fallbackValue = (String) properties.getOrDefault(RedissonRegionFactory.FALLBACK, "false");
        fallback = Boolean.parseBoolean(fallbackValue);
    }

    /** 按 map 名、Region 名、默认键后缀的优先级查找配置属性。 */
    private String getProperty(Map<String, Object> properties, String name, String regionName, String defaultKey, String suffix) {
        String maxEntries = (String) properties.get(RedissonRegionFactory.CONFIG_PREFIX + name + suffix);
        if (maxEntries != null) {
            return maxEntries;
        }
        maxEntries = (String) properties.get(RedissonRegionFactory.CONFIG_PREFIX + regionName + suffix);
        if (maxEntries != null) {
            return maxEntries;
        }
        return (String) properties.get(RedissonRegionFactory.CONFIG_PREFIX + defaultKey + suffix);
    }

    /** 进入 fallback 模式后周期性探测 Redis 是否恢复。 */
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

    /** 从缓存读取条目；fallback 模式下返回 null。 */
    @Override
    public Object getFromCache(Object key, SharedSessionContractImplementor session) {
        // fallback 模式下跳过远程读取。
        if (fallbackMode) {
            return null;
        }
        try {
            // 未配置 max_idle 与 max_entries 时使用仅 TTL 的读取路径。
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

    /** 写入缓存条目；fallback 模式下跳过写入。 */
    @Override
    public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {
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

    /** 判断键是否存在；fallback 模式下恒为 false。 */
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

    /** 清空整个 Region 缓存；fallback 模式下跳过。 */
    @Override
    public void evictData() {
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

    /** 逐出指定键；fallback 模式下跳过。 */
    @Override
    public void evictData(Object key) {
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

    /** 销毁底层 {@link RMapCache} 并释放 Redis 资源。 */
    @Override
    public void release() {
        try {
            mapCache.destroy();
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

}
