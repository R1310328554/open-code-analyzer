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
import org.hibernate.cache.spi.GeneralDataRegion;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.redisson.hibernate.region.RedissonNaturalIdRegion;

/**
 * 自然 ID 并发策略 {@code transactional} 的 Redisson 区域访问实现（Hibernate 5.2）。
 * <p>插入/更新在事务阶段立即写入缓存，after 阶段返回 {@code false}。
 *
 * @author Nikita Koksharov
 */
public class TransactionalNaturalIdRegionAccessStrategy extends BaseRegionAccessStrategy implements NaturalIdRegionAccessStrategy {

    /** @param settings Hibernate 缓存配置
     * @param region 自然 ID 缓存区域
     */
    public TransactionalNaturalIdRegionAccessStrategy(Settings settings, GeneralDataRegion region) {
        super(settings, region);
    }

    /** 在当前会话上下文中读取缓存条目。 */
    @Override
    public Object get(SharedSessionContractImplementor session, Object key, long txTimestamp) throws CacheException {
        return region.get(session, key);
    }

    @Override
    public boolean putFromLoad(SharedSessionContractImplementor session, Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)
            throws CacheException {
        // 最小写入模式下键已存在则跳过写入。
        if (minimalPutOverride && region.contains(key)) {
            return false;
        }

        region.put(session, key, value);
        return true;
    }

    /** 事务策略不提供项级软锁。 */
    @Override
    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {
        return null;
    }

    /** 解锁为空操作。 */
    @Override
    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {
    }

    /** 返回强类型 {@link NaturalIdRegion} 视图。 */
    @Override
    public NaturalIdRegion getRegion() {
        return (NaturalIdRegion) region;
    }
    
    /** 移除指定自然 ID 对应的缓存条目。 */
    @Override
    public void remove(SharedSessionContractImplementor session, Object key) throws CacheException {
        region.evict(key);
    }

    /** 插入阶段立即写入缓存。 */
    @Override
    public boolean insert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
        region.put(session, key, value);
        return true;
    }

    /** 插入后阶段不再重复写入。 */
    @Override
    public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
        return false;
    }

    /** 更新阶段复用 insert 逻辑立即写入。 */
    @Override
    public boolean update(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
        return insert(session, key, value);
    }

    /** 更新后阶段不再重复写入。 */
    @Override
    public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, SoftLock lock) throws CacheException {
        return false;
    }

    /** 通过 {@link RedissonNaturalIdRegion} 的键工厂生成自然 ID 缓存键。 */
    @Override
    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {
        return ((RedissonNaturalIdRegion)region).getCacheKeysFactory().createNaturalIdKey(naturalIdValues, persister, session);
    }

    /** 从缓存键反解自然 ID 值数组。 */
    @Override
    public Object[] getNaturalIdValues(Object cacheKey) {
        return ((RedissonNaturalIdRegion)region).getCacheKeysFactory().getNaturalIdValues(cacheKey);
    }

}
