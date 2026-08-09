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
import org.hibernate.cache.spi.access.RegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.spi.SessionImplementor;

/**
 * Hibernate 5 缓存区域访问策略的抽象基类。
 * <p>封装 {@link GeneralDataRegion} 与 {@link Settings}，提供区域锁、逐出
 * 以及带最小写入开关的 {@link #putFromLoad} 等默认实现。</p>
 *
 * @author Nikita Koksharov
 */
abstract class BaseRegionAccessStrategy implements RegionAccessStrategy {

    /** 关联的通用数据区域。 */
    final GeneralDataRegion region;
    /** Hibernate 缓存相关配置。 */
    final Settings settings;
    
    /** 绑定 Hibernate 设置与底层缓存区域。
     *
     * @param settings Hibernate 缓存配置
     * @param region 通用数据区域实例
     */
    BaseRegionAccessStrategy(Settings settings, GeneralDataRegion region) {
        this.settings = settings;
        this.region = region;
    }

    /** 按全局最小写入开关委托至六参数 {@link #putFromLoad}。 */
    @Override
    public boolean putFromLoad(SessionImplementor session, Object key, Object value, long txTimestamp, Object version) throws CacheException {
        return putFromLoad(session, key, value, txTimestamp, version, settings.isMinimalPutsEnabled() );
    }

    /** 区域级锁：本实现不支持，始终返回 {@code null}。 */
    @Override
    public SoftLock lockRegion() throws CacheException {
        return null;
    }

    /** 解锁区域时清空整个缓存区域。 */
    @Override
    public void unlockRegion(SoftLock lock) throws CacheException {
        region.evictAll();
    }

    /** 移除键：基类为空实现，由子类按需覆盖。 */
    @Override
    public void remove(SessionImplementor session, Object key) throws CacheException {
    }

    /** 移除全部条目，委托区域 {@link GeneralDataRegion#evictAll()}。 */
    @Override
    public void removeAll() throws CacheException {
        region.evictAll();
    }

    /** 逐出指定键对应的缓存条目。 */
    @Override
    public void evict(Object key) throws CacheException {
        region.evict(key);
    }

    /** 逐出区域内全部条目。 */
    @Override
    public void evictAll() throws CacheException {
        region.evictAll();
    }

}
