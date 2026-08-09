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

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;

/**
 * JCache {@link javax.cache.Cache} 的完整配置封装，实现 {@link CompleteConfiguration}。
 * <p>
 * 内部委托 {@link MutableConfiguration} 存储各项开关；
 * 构造时从 {@link RedissonConfiguration} 解包或直接复制外部 {@link Configuration}，
 * 并立即实例化 {@link ExpiryPolicy} 供运行时查询。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 */
public class JCacheConfiguration<K, V> implements CompleteConfiguration<K, V> {

    private static final long serialVersionUID = -7861479608049089078L;
    
    /** 构造时由 ExpiryPolicyFactory 创建的过期策略实例。 */
    private final ExpiryPolicy expiryPolicy;
    /** 实际持有 JCache 配置项的可变配置对象。 */
    private final MutableConfiguration<K, V> delegate;
    
    /**
     * 从外部 Configuration 构建 JCacheConfiguration。
     * <p>
     * 若为 {@link RedissonConfiguration} 则先解包内部 jcacheConfig；
     * 若为 {@link CompleteConfiguration} 则完整复制，否则仅复制 storeByValue 与类型信息。
     */
    public JCacheConfiguration(Configuration<K, V> configuration) {
        if (configuration != null) {
            if (configuration instanceof RedissonConfiguration) {
                configuration = ((RedissonConfiguration<K, V>) configuration).getJcacheConfig();
            }
            
            if (configuration instanceof CompleteConfiguration) {
                delegate = new MutableConfiguration<K, V>((CompleteConfiguration<K, V>) configuration);
            } else {
                delegate = new MutableConfiguration<K, V>();
                delegate.setStoreByValue(configuration.isStoreByValue());
                delegate.setTypes(configuration.getKeyType(), configuration.getValueType());
            }
        } else {
            delegate = new MutableConfiguration<K, V>();
        }
        
        this.expiryPolicy = delegate.getExpiryPolicyFactory().create();
    }
    
    /** 返回 key 类型，未配置时默认为 {@link Object}.class。 */
    @Override
    public Class<K> getKeyType() {
        if (delegate.getKeyType() == null) {
            return (Class<K>) Object.class; 
        }
        return delegate.getKeyType();
    }

    /** 返回 value 类型，未配置时默认为 {@link Object}.class。 */
    @Override
    public Class<V> getValueType() {
        if (delegate.getValueType() == null) {
            return (Class<V>) Object.class;
        }
        return delegate.getValueType();
    }

    @Override
    public boolean isStoreByValue() {
        return delegate.isStoreByValue();
    }

    @Override
    public boolean isReadThrough() {
        return delegate.isReadThrough();
    }

    @Override
    public boolean isWriteThrough() {
        return delegate.isWriteThrough();
    }

    @Override
    public boolean isStatisticsEnabled() {
        return delegate.isStatisticsEnabled();
    }
    
    /** 启用或关闭缓存统计（命中/未命中等计数）。 */
    public void setStatisticsEnabled(boolean enabled) {
        delegate.setStatisticsEnabled(enabled);
    }
    
    /** 启用或关闭 JMX 管理接口。 */
    public void setManagementEnabled(boolean enabled) {
        delegate.setManagementEnabled(enabled);
    }

    @Override
    public boolean isManagementEnabled() {
        return delegate.isManagementEnabled();
    }

    @Override
    public Iterable<CacheEntryListenerConfiguration<K, V>> getCacheEntryListenerConfigurations() {
        return delegate.getCacheEntryListenerConfigurations();
    }
    
    /** 添加缓存条目监听器配置。 */
    public void addCacheEntryListenerConfiguration(
            CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        delegate.addCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);
    }
    
    /** 移除指定的缓存条目监听器配置。 */
    public void removeCacheEntryListenerConfiguration(
            CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        delegate.removeCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);
    }

    @Override
    public Factory<CacheLoader<K, V>> getCacheLoaderFactory() {
        return delegate.getCacheLoaderFactory();
    }

    @Override
    public Factory<CacheWriter<? super K, ? super V>> getCacheWriterFactory() {
        return delegate.getCacheWriterFactory();
    }

    @Override
    public Factory<ExpiryPolicy> getExpiryPolicyFactory() {
        return delegate.getExpiryPolicyFactory();
    }
    
    /** 返回构造时已实例化的过期策略对象。 */
    public ExpiryPolicy getExpiryPolicy() {
        return expiryPolicy;
    }

    
    
}
