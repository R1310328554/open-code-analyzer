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
package org.redisson.spring.cache;

import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.MapEntryListener;
import org.redisson.client.codec.Codec;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于 Redisson {@link RMap}/{@link RMapCache} 的 Spring {@link org.springframework.cache.CacheManager}。
 * <p>支持动态创建缓存、YAML/Map 配置、Codec 共享、事务感知装饰及空值策略。
 * TTL/maxIdle/maxSize 全为 0 时使用 {@link RMap}，否则使用 {@link RMapCache}。
 *
 * @author Nikita Koksharov
 *
 */
@SuppressWarnings("unchecked")
public class RedissonSpringCacheManager implements CacheManager, ResourceLoaderAware, InitializingBean {

    /** Spring 资源加载器，用于读取 YAML 缓存配置。 */
    ResourceLoader resourceLoader;

    /** {@code true} 时按名称动态创建缓存；{@code false} 时仅使用预定义名称。 */
    private boolean dynamic = true;
    
    /** 是否允许缓存 {@code null} 值（默认 {@code true}）。 */
    private boolean allowNullValues = true;

    /** 是否包装 {@link TransactionAwareCacheDecorator} 以参与 Spring 事务。 */
    private boolean transactionAware = false;

    Codec codec;

    RedissonClient redisson;

    Map<String, CacheConfig> configMap = new ConcurrentHashMap<String, CacheConfig>();
    ConcurrentMap<String, Cache> instanceMap = new ConcurrentHashMap<String, Cache>();

    String configLocation;

    /** 使用 Redisson 客户端与默认配置创建 CacheManager。 */
    /**
     * Creates CacheManager supplied by Redisson instance
     *
     * @param redisson object
     */
    public RedissonSpringCacheManager(RedissonClient redisson) {
        this(redisson, (String) null, null);
    }

    /**
     * Creates CacheManager supplied by Redisson instance and
     * Cache config mapped by Cache name
     *
     * @param redisson object
     * @param config object
     */
    public RedissonSpringCacheManager(RedissonClient redisson, Map<String, ? extends CacheConfig> config) {
        this(redisson, config, null);
    }

    /**
     * Creates CacheManager supplied by Redisson instance, Codec instance
     * and Cache config mapped by Cache name.
     * <p>
     * Each Cache instance share one Codec instance.
     *
     * @param redisson object
     * @param config object
     * @param codec object
     */
    public RedissonSpringCacheManager(RedissonClient redisson, Map<String, ? extends CacheConfig> config, Codec codec) {
        this.redisson = redisson;
        this.configMap = (Map<String, CacheConfig>) config;
        this.codec = codec;
    }

    /**
     * Creates CacheManager supplied by Redisson instance
     * and Cache config mapped by Cache name.
     * <p>
     * Loads the config file from the class path, interpreting plain paths as class path resource names
     * that include the package path (e.g. "mypackage/myresource.txt").
     *
     * @param redisson object
     * @param configLocation path
     */
    public RedissonSpringCacheManager(RedissonClient redisson, String configLocation) {
        this(redisson, configLocation, null);
    }

    /**
     * Creates CacheManager supplied by Redisson instance, Codec instance
     * and Config location path.
     * <p>
     * Each Cache instance share one Codec instance.
     * <p>
     * Loads the config file from the class path, interpreting plain paths as class path resource names
     * that include the package path (e.g. "mypackage/myresource.txt").
     *
     * @param redisson object
     * @param configLocation path
     * @param codec object
     */
    public RedissonSpringCacheManager(RedissonClient redisson, String configLocation, Codec codec) {
        this.redisson = redisson;
        this.configLocation = configLocation;
        this.codec = codec;
    }
    
    /** 设置是否允许缓存空值；关闭时 {@code null} 不会写入 Redis。 */
    /**
     * Defines possibility of storing {@code null} values.
     * <p>
     * Default is <code>true</code>
     * 
     * @param allowNullValues stores if <code>true</code>
     */
    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    /** 启用后 put/evict 仅在事务成功提交后的 after-commit 阶段执行。 */
    /**
     * Defines if cache aware of Spring-managed transactions.
     * If {@code true} put/evict operations are executed only for successful transaction in after-commit phase.
     * <p>
     * Default is <code>false</code>
     *
     * @param transactionAware cache is transaction aware if <code>true</code>
     */
    public void setTransactionAware(boolean transactionAware) {
        this.transactionAware = transactionAware;
    }

    /** 预注册固定缓存名；传入非 {@code null} 集合后关闭动态模式。 */
    /**
     * Defines 'fixed' cache names. 
     * A new cache instance will not be created in dynamic for non-defined names.
     * <p>
     * `null` parameter setups dynamic mode 
     * 
     * @param names of caches
     */
    public void setCacheNames(Collection<String> names) {
        if (names != null) {
            for (String name : names) {
                getCache(name);
            }
            dynamic = false;
        } else {
            dynamic = true;
        }
    }
    
    /**
     * Set cache config location
     *
     * @param configLocation object
     */
    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    /**
     * Set cache config mapped by cache name
     *
     * @param config object
     */
    public void setConfig(Map<String, ? extends CacheConfig> config) {
        this.configMap = (Map<String, CacheConfig>) config;
    }

    /**
     * Set Redisson instance
     *
     * @param redisson instance
     */
    public void setRedisson(RedissonClient redisson) {
        this.redisson = redisson;
    }

    /**
     * Set Codec instance shared between all Cache instances
     *
     * @param codec object
     */
    public void setCodec(Codec codec) {
        this.codec = codec;
    }
    
    protected CacheConfig createDefaultConfig() {
        return new CacheConfig();
    }

    /** 按名称获取或（动态模式下）创建 {@link RedissonCache} 实例。 */
    @Override
    public Cache getCache(String name) {
        Cache cache = instanceMap.get(name);
        if (cache != null) {
            return cache;
        }
        if (!dynamic) {
            return cache;
        }
        
        CacheConfig config = configMap.get(name);
        if (config == null) {
            config = createDefaultConfig();
            configMap.put(name, config);
        }
        
        // 无过期与容量限制时使用普通 RMap，否则使用 RMapCache。
        if (config.getMaxIdleTime() == 0 && config.getTTL() == 0 && config.getMaxSize() == 0) {
            return createMap(name, config);
        }
        
        return createMapCache(name, config);
    }

    /** 基于 {@link RMap} 创建无 TTL 的 {@link RedissonCache}。 */
    private Cache createMap(String name, CacheConfig config) {
        RMap<Object, Object> map = getMap(name, config);
        
        Cache cache = new RedissonCache(map, allowNullValues);
        if (transactionAware) {
            cache = new TransactionAwareCacheDecorator(cache);
        }
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        }
        return cache;
    }

    protected RMap<Object, Object> getMap(String name, CacheConfig config) {
        if (codec != null) {
            return redisson.getMap(name, codec);
        }
        return redisson.getMap(name);
    }

    /** 基于 {@link RMapCache} 创建带 TTL/淘汰策略的 {@link RedissonCache}。 */
    private Cache createMapCache(String name, CacheConfig config) {
        RMapCache<Object, Object> map = getMapCache(name, config);
        
        Cache cache = new RedissonCache(map, config, allowNullValues);
        if (transactionAware) {
            cache = new TransactionAwareCacheDecorator(cache);
        }
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        } else {
            // 首次创建时应用 maxSize 与 MapEntryListener。
            map.setMaxSize(config.getMaxSize(), config.getEvictionMode());
            for (MapEntryListener listener : config.getListeners()) {
                map.addListener(listener);
            }
        }
        return cache;
    }

    protected RMapCache<Object, Object> getMapCache(String name, CacheConfig config) {
        if (codec != null) {
            return redisson.getMapCache(name, codec);
        }
        return redisson.getMapCache(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(configMap.keySet());
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /** 若配置了 {@code configLocation}，从 classpath 加载 YAML 缓存配置。 */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (configLocation == null) {
            return;
        }

        Resource resource = resourceLoader.getResource(configLocation);
        this.configMap = (Map<String, CacheConfig>) CacheConfig.fromYAML(resource.getInputStream());
    }
    
}
