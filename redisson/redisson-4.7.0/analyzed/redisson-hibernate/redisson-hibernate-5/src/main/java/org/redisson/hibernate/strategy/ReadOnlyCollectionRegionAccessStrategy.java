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
package org.redisson.hibernate.strategy;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.GeneralDataRegion;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.redisson.hibernate.region.RedissonCollectionRegion;

/**
 * 集合并发策略 {@code read-only} 的 Redisson 区域访问实现（Hibernate 5）。
 * <p>缓存内容不可变；解锁为空操作，不逐出条目。
 *
 * @author Nikita Koksharov
 */
public class ReadOnlyCollectionRegionAccessStrategy extends BaseRegionAccessStrategy implements CollectionRegionAccessStrategy {

    /** @param settings Hibernate 缓存配置
     * @param region 集合缓存区域
     */
    public ReadOnlyCollectionRegionAccessStrategy(Settings settings, GeneralDataRegion region) {
        super(settings, region);
    }

    /** 返回强类型 {@link CollectionRegion} 视图。 */
    @Override
    public CollectionRegion getRegion() {
        return (CollectionRegion) region;
    }

    /** 在当前会话上下文中读取缓存条目。 */
    @Override
    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {
        return region.get(session, key);
    }

    @Override
    public boolean putFromLoad(SessionImplementor session, Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)
            throws CacheException {
        // 最小写入模式下键已存在则跳过写入。
        if (minimalPutOverride && region.contains(key)) {
            return false;
        }

        region.put(session, key, value);
        return true;
    }

    /** 只读策略不提供项级软锁。 */
    @Override
    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {
        return null;
    }

    /** 只读解锁为空操作，不修改缓存。 */
    @Override
    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {
    }

    @Override
    public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        return ((RedissonCollectionRegion)region).getCacheKeysFactory().createCollectionKey(id, persister, factory, tenantIdentifier);
    }

    @Override
    public Object getCacheKeyId(Object cacheKey) {
        return ((RedissonCollectionRegion)region).getCacheKeysFactory().getCollectionId(cacheKey);
    }

}
