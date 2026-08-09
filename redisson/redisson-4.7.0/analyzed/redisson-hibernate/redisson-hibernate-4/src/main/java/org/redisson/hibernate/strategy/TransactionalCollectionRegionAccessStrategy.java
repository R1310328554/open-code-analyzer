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
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.GeneralDataRegion;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.cfg.Settings;

/**
 * 集合并发策略 {@code transactional} 的 Redisson 区域访问实现。
 * <p>缓存写入与 JTA 事务边界对齐；解锁为空操作。
 *
 * @author Nikita Koksharov
 */
public class TransactionalCollectionRegionAccessStrategy extends BaseRegionAccessStrategy implements CollectionRegionAccessStrategy {

    /** @param settings Hibernate 缓存配置
     * @param region 集合缓存区域
     */
    public TransactionalCollectionRegionAccessStrategy(Settings settings, GeneralDataRegion region) {
        super(settings, region);
    }

    /** 返回强类型 {@link CollectionRegion} 视图。 */
    @Override
    public CollectionRegion getRegion() {
        return (CollectionRegion) region;
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

    /** 事务策略不提供项级软锁。 */
    @Override
    public SoftLock lockItem(Object key, Object version) throws CacheException {
        return null;
    }

    /** 解锁为空操作，事务回滚由 Hibernate 协调。 */
    @Override
    public void unlockItem(Object key, SoftLock lock) throws CacheException {
    }
    
    /** 移除指定键对应的缓存条目。 */
    @Override
    public void remove(Object key) throws CacheException {
        region.evict(key);
    }

}
