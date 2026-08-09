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
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.GeneralDataRegion;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.cfg.Settings;

/**
 * 实体并发策略 {@code read-only} 的 Redisson 区域访问实现。
 * <p>插入后写入缓存；后续更新抛出 {@link UnsupportedOperationException}。
 *
 * @author Nikita Koksharov
 */
public class ReadOnlyEntityRegionAccessStrategy extends BaseRegionAccessStrategy implements EntityRegionAccessStrategy {

    /** @param settings Hibernate 缓存配置
     * @param region 实体缓存区域
     */
    public ReadOnlyEntityRegionAccessStrategy(Settings settings, GeneralDataRegion region) {
        super(settings, region);
    }

    /** 直接读取缓存条目。 */
    @Override
    public Object get(Object key, long txTimestamp) throws CacheException {
        return region.get(key);
    }

    @Override
    public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)
            throws CacheException {
        // 最小写入模式下键已存在则跳过写入。
        if (minimalPutOverride && region.contains(key)) {
            return false;
        }

        region.put(key, value);
        return true;
    }

    /** 只读策略不提供项级软锁。 */
    @Override
    public SoftLock lockItem(Object key, Object version) throws CacheException {
        return null;
    }

    /** 解锁时逐出键（与 Hibernate 只读语义一致）。 */
    @Override
    public void unlockItem(Object key, SoftLock lock) throws CacheException {
        evict(key);
    }

    /** 返回强类型 {@link EntityRegion} 视图。 */
    @Override
    public EntityRegion getRegion() {
        return (EntityRegion) region;
    }

    /** 插入阶段不写缓存，延迟至 afterInsert。 */
    @Override
    public boolean insert(Object key, Object value, Object version) throws CacheException {
        return false;
    }

    /** 插入完成后将实体写入缓存。 */
    @Override
    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {
        region.put(key, value);
        return true;
    }

    /** 只读实体禁止更新。 */
    @Override
    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion)
            throws CacheException {
        throw new UnsupportedOperationException("Unable to update read-only object");
    }

    /** 只读实体禁止 afterUpdate。 */
    @Override
    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock)
            throws CacheException {
        throw new UnsupportedOperationException("Unable to update read-only object");
    }

}
