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

import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.GeneralDataRegion;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.redisson.api.RMapCache;
import org.redisson.hibernate.region.RedissonCollectionRegion;

/**
 * 集合并发策略 {@code read-write} 的 Redisson 区域访问实现（Hibernate 5）。
 * <p>基于 {@link AbstractReadWriteAccessStrategy}，利用 {@link RMapCache} 提供读写一致性。
 *
 * @author Nikita Koksharov
 */
public class ReadWriteCollectionRegionAccessStrategy extends AbstractReadWriteAccessStrategy implements CollectionRegionAccessStrategy {

    /** @param settings Hibernate 缓存配置
     * @param region 集合缓存区域
     * @param mapCache 底层 Redisson 带 TTL 的 Map 缓存
     */
    public ReadWriteCollectionRegionAccessStrategy(Settings settings, GeneralDataRegion region,
            RMapCache<Object, Object> mapCache) {
        super(settings, region, mapCache);
    }

    /** 返回强类型 {@link CollectionRegion} 视图。 */
    @Override
    public CollectionRegion getRegion() {
        return (CollectionRegion) region;
    }

    /** 通过 {@link RedissonCollectionRegion} 的键工厂生成集合缓存键。 */
    @Override
    public Object generateCacheKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        return ((RedissonCollectionRegion)region).getCacheKeysFactory().createCollectionKey( id, persister, factory, tenantIdentifier );
    }

    /** 从缓存键反解集合标识符。 */
    @Override
    public Object getCacheKeyId(Object cacheKey) {
        return ((RedissonCollectionRegion)region).getCacheKeysFactory().getCollectionId(cacheKey);
    }

}
