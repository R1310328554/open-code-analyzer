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
 * 自然 ID 并发策略 {@code read-only} 的 Redisson 区域访问实现（Hibernate 5.2）。
 * <p>插入后写入缓存；后续更新抛出 {@link UnsupportedOperationException}。
 *
 * @author Nikita Koksharov
 */
public class ReadOnlyNaturalIdRegionAccessStrategy extends BaseRegionAccessStrategy implements NaturalIdRegionAccessStrategy {

    /** @param settings Hibernate 缓存配置
     * @param region 自然 ID 缓存区域
     */
    public ReadOnlyNaturalIdRegionAccessStrategy(Settings settings, GeneralDataRegion region) {
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

    /** 只读策略不提供项级软锁。 */
    @Override
    public SoftLock lockItem(SharedSessionContractImplementor session, Object key, Object version) throws CacheException {
        return null;
    }

    /** 解锁时逐出键。 */
    @Override
    public void unlockItem(SharedSessionContractImplementor session, Object key, SoftLock lock) throws CacheException {
        evict(key);
    }

    /** 返回强类型 {@link NaturalIdRegion} 视图。 */
    @Override
    public NaturalIdRegion getRegion() {
        return (NaturalIdRegion) region;
    }

    /** 插入阶段不写缓存，延迟至 afterInsert。 */
    @Override
    public boolean insert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
        return false;
    }

    /** 插入完成后将自然 ID 映射写入缓存。 */
    @Override
    public boolean afterInsert(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
        region.put(session, key, value);
        return true;
    }

    /** 只读自然 ID 禁止更新。 */
    @Override
    public boolean update(SharedSessionContractImplementor session, Object key, Object value) throws CacheException {
        throw new UnsupportedOperationException("Unable to update read-only object");
    }

    /** 只读自然 ID 禁止 afterUpdate。 */
    @Override
    public boolean afterUpdate(SharedSessionContractImplementor session, Object key, Object value, SoftLock lock) throws CacheException {
        throw new UnsupportedOperationException("Unable to update read-only object");
    }

    /** 通过 {@link RedissonNaturalIdRegion} 的键工厂生成自然 ID 缓存键。 */
    @Override
    public Object generateCacheKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {
        return ((RedissonNaturalIdRegion)region).getCacheKeysFactory().createNaturalIdKey(naturalIdValues, persister, session);
    }

    /** 从缓存键解析自然 ID 值数组。 */
    @Override
    public Object[] getNaturalIdValues(Object cacheKey) {
        return ((RedissonNaturalIdRegion)region).getCacheKeysFactory().getNaturalIdValues(cacheKey);
    }

}
