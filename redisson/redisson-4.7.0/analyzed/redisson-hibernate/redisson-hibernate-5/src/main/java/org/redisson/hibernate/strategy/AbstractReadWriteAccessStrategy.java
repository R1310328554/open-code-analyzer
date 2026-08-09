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
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.spi.SessionImplementor;
import org.redisson.api.RMapCache;

/**
 * 读写（READ_WRITE）缓存并发访问策略的抽象基类（Hibernate 5）。
 * <p>委托 {@link GeneralDataRegion} 完成 get/put，解锁时驱逐条目。</p>
 *
 * @author Nikita Koksharov
 */
public class AbstractReadWriteAccessStrategy extends BaseRegionAccessStrategy {

    /** 底层 Redisson 带 TTL 的 Map 缓存。 */
    final RMapCache<Object, Object> mapCache;
    
    /** @param settings Hibernate 缓存配置
     * @param region 通用数据区域
     * @param mapCache 底层 Redisson Map 缓存
     */
    public AbstractReadWriteAccessStrategy(Settings settings, GeneralDataRegion region, RMapCache<Object, Object> mapCache) {
        super(settings, region);
        this.mapCache = mapCache;
    }

    /** 从 Region 读取缓存条目（不校验版本）。 */
    @Override
    public Object get(SessionImplementor session, Object key, long txTimestamp) throws CacheException {
        return region.get(session, key);
    }

    /** 加载后写入缓存并始终返回 true。 */
    @Override
    public boolean putFromLoad(SessionImplementor session, Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)
            throws CacheException {
        region.put(session, key, value);
        return true;
    }

    /** 当前实现不使用软锁，直接返回 null。 */
    @Override
    public SoftLock lockItem(SessionImplementor session, Object key, Object version) throws CacheException {
        return null;
    }

    /** 解锁时驱逐对应缓存条目。 */
    @Override
    public void unlockItem(SessionImplementor session, Object key, SoftLock lock) throws CacheException {
        region.evict(key);
    }
    

}
