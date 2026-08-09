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
import org.redisson.api.RMapCache;
import org.redisson.hibernate.region.RedissonEntityRegion;

/**
 * 实体并发策略 {@code read-write} 的 Redisson 区域访问实现（Hibernate 5）。
 * <p>插入/更新完成后写回缓存；更新阶段不立即写入。
 *
 * @author Nikita Koksharov
 */
public class ReadWriteEntityRegionAccessStrategy extends AbstractReadWriteAccessStrategy implements EntityRegionAccessStrategy {

    /** @param settings Hibernate 缓存配置
     * @param region 实体缓存区域
     * @param mapCache 底层 Redisson 带 TTL 的 Map 缓存
     */
    public ReadWriteEntityRegionAccessStrategy(Settings settings, GeneralDataRegion region, RMapCache<Object, Object> mapCache) {
        super(settings, region, mapCache);
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

    /** 插入完成后将实体写入缓存。 */
    @Override
    public boolean afterInsert(SessionImplementor session, Object key, Object value, Object version) throws CacheException {
        region.put(session, key, value);
        return true;
    }

    /** 更新阶段不立即写入，等待 afterUpdate。 */
    @Override
    public boolean update(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion)
            throws CacheException {
        return false;
    }

    /** 更新完成后将新实体版本写入缓存。 */
    @Override
    public boolean afterUpdate(SessionImplementor session, Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)
            throws CacheException {
        region.put(session, key, value);
        return true;
    }

    @Override
    public Object generateCacheKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        return ((RedissonEntityRegion)region).getCacheKeysFactory().createEntityKey(id, persister, factory, tenantIdentifier);
    }

    @Override
    public Object getCacheKeyId(Object cacheKey) {
        return ((RedissonEntityRegion)region).getCacheKeysFactory().getEntityId(cacheKey);
    }

}
