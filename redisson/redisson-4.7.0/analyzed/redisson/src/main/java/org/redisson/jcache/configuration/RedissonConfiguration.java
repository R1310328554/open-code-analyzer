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
package org.redisson.jcache.configuration;

import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;

import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * Redisson 与 JCache 的双层配置桥接类，实现 {@link Configuration}。
 * <p>
 * 同时持有 {@link RedissonClient}/{@link Config} 与底层 JCache {@link Configuration}，
 * 供 {@link org.redisson.jcache.JCacheManager} 创建缓存时一并注入。
 *
 * @author Nikita Koksharov
 *
 * @param <K> the type of key 
 * @param <V> the type of value
 */
public class RedissonConfiguration<K, V> implements Configuration<K, V> {

    private static final long serialVersionUID = 5331107577281201157L;

    /** 底层 JCache 配置（键值类型、监听器、过期策略等）。 */
    private Configuration<K, V> jcacheConfig;
    
    /** Redisson 客户端配置（仅 fromConfig 路径使用）。 */
    private Config config;
    /** 已连接的 Redisson 客户端实例（仅 fromInstance 路径使用）。 */
    private RedissonClient redisson;
    
    /** 基于 Config 与 JCache 配置构造（尚未创建客户端）。 */
    RedissonConfiguration(Config config, Configuration<K, V> jcacheConfig) {
        this.config = config;
        this.jcacheConfig = jcacheConfig;
    }
    
    /** 基于已有 RedissonClient 与 JCache 配置构造。 */
    RedissonConfiguration(RedissonClient redisson, Configuration<K, V> jcacheConfig) {
        this.redisson = redisson;
        this.jcacheConfig = jcacheConfig;
    }

    /** 使用默认 MutableConfiguration 与给定 RedissonClient 创建配置。 */
    public static <K, V> Configuration<K, V> fromInstance(RedissonClient redisson) {
        MutableConfiguration<K, V> config = new MutableConfiguration<K, V>();
        return fromInstance(redisson, config);
    }
    
    /** 将 RedissonClient 与指定 JCache 配置组合为 RedissonConfiguration。 */
    public static <K, V> Configuration<K, V> fromInstance(RedissonClient redisson, Configuration<K, V> jcacheConfig) {
        return new RedissonConfiguration<K, V>(redisson, jcacheConfig);
    }

    /** 使用 Config 与默认 MutableConfiguration 创建配置。 */
    public static <K, V> Configuration<K, V> fromConfig(Config config) {
        MutableConfiguration<K, V> jcacheConfig = new MutableConfiguration<K, V>();
        return new RedissonConfiguration<K, V>(config, jcacheConfig);
    }
    
    /** 将 Config 与指定 JCache 配置组合为 RedissonConfiguration。 */
    public static <K, V> Configuration<K, V> fromConfig(Config config, Configuration<K, V> jcacheConfig) {
        return new RedissonConfiguration<K, V>(config, jcacheConfig);
    }
    
    /** 返回内部 JCache 配置对象。 */
    public Configuration<K, V> getJcacheConfig() {
        return jcacheConfig;
    }
    
    /** 返回关联的 RedissonClient（fromInstance 路径）。 */
    public RedissonClient getRedisson() {
        return redisson;
    }
    
    /** 返回关联的 Redisson Config（fromConfig 路径）。 */
    public Config getConfig() {
        return config;
    }
    
    @Override
    public Class<K> getKeyType() {
        return (Class<K>) Object.class;
    }

    @Override
    public Class<V> getValueType() {
        return (Class<V>) Object.class;
    }

    /** Redisson 缓存始终按值存储（store by value）。 */
    @Override
    public boolean isStoreByValue() {
        return true;
    }

}
