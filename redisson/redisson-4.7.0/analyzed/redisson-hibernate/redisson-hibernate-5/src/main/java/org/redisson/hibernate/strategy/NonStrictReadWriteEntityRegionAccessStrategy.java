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
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.GeneralDataRegion;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.redisson.hibernate.region.RedissonEntityRegion;

/**
 * 实体并发策略 {@code nonstrict-read-write} 的 Redisson 区域访问实现（Hibernate 5）。
 * <p>允许脏读；更新时先移除键，插入/更新后不主动写回缓存。</p>
 *
 * @author Nikita Koksharov
 */
public class NonStrictReadWriteEntityRegionAccessStrategy extends BaseRegionAccessStrategy implements EntityRegionAccessStrategy {

    /** @param settings Hibernate 缓存配置
     * @param region 实体缓存区域
     */
    public NonStrictReadWriteEntityRegionAccessStrategy(Settings settings, GeneralDataRegion region) {
        super(settings, region);
    }

    /** 直接读取缓存，不校验事务时间戳。 */
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

    /** 非严格读写不提供项级软锁。 */
    @Override
    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {
        return null;
    }

    /** 解锁时逐出键。 */
    @Override
    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {
        evict(key);
    }

    /** 返回强类型 {@link EntityRegion} 视图。 */
    @Override
    public EntityRegion getRegion() {
        return (EntityRegion) region;
    }

    /** 插入阶段不写缓存，延迟至 afterInsert。 */
    @Override
    public boolean insert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {
        return false;
    }

    /** 插入完成后也不写回缓存。 */
    @Override
    public boolean afterInsert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {
        return false;
    }

    /** 更新时移除旧缓存条目，不立即写入新值。 */
    @Override
    public boolean update(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)
            throws CacheException {
        remove(session, key);
        return false;
    }

    /** 更新完成后解锁并逐出，不写回新值。 */
    @Override
    public boolean afterUpdate(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)
            throws CacheException {
        unlockItem(session, key, lock);
        return false;
    }
    
    /** 移除键等同于逐出缓存条目。 */
    @Override
    public void remove(SessionImplementor session, Object key) throws CacheException {
        evict(key);
    }

    /** 委托 Region 的 {@link CacheKeysFactory} 生成实体缓存键。 */
    @Override
    public Object generateCacheKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        return ((RedissonEntityRegion)region).getCacheKeysFactory().createEntityKey( id, persister, factory, tenantIdentifier );
    }

    /** 从缓存键解析实体 ID。 */
    @Override
    public Object getCacheKeyId(Object cacheKey) {
        return ((RedissonEntityRegion)region).getCacheKeysFactory().getEntityId( cacheKey );
    }

}
