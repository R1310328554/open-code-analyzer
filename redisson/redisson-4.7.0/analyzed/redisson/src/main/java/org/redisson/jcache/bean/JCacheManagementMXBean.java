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
package org.redisson.jcache.bean;

import javax.cache.Cache;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.management.CacheMXBean;

/**
 * JSR-107 {@link CacheMXBean} 实现，暴露 Cache 配置元数据。
 * <p>
 * 通过 MBean 查询键值类型、read/write-through、store-by-value 等开关。
 *
 * @author Nikita Koksharov
 *
 */
public class JCacheManagementMXBean implements CacheMXBean {

    /** 被管理的 Cache 实例。 */
    private final Cache<?, ?> cache;
    
    /** 绑定指定 Cache 的配置视图。 */
    public JCacheManagementMXBean(Cache<?, ?> cache) {
        super();
        this.cache = cache;
    }

    /** 配置中的键类型全限定名。 */
    @Override
    public String getKeyType() {
        return cache.getConfiguration(CompleteConfiguration.class).getKeyType().getName();
    }

    /** 配置中的值类型全限定名。 */
    @Override
    public String getValueType() {
        return cache.getConfiguration(CompleteConfiguration.class).getValueType().getName();
    }

    /** 是否启用 read-through。 */
    @Override
    public boolean isReadThrough() {
        return cache.getConfiguration(CompleteConfiguration.class).isReadThrough();
    }

    /** 是否启用 write-through。 */
    @Override
    public boolean isWriteThrough() {
        return cache.getConfiguration(CompleteConfiguration.class).isWriteThrough();
    }

    /** 是否按值存储（非引用）。 */
    @Override
    public boolean isStoreByValue() {
        return cache.getConfiguration(CompleteConfiguration.class).isStoreByValue();
    }

    /** 统计是否已启用。 */
    @Override
    public boolean isStatisticsEnabled() {
        return cache.getConfiguration(CompleteConfiguration.class).isStatisticsEnabled();
    }

    /** JMX 管理是否已启用。 */
    @Override
    public boolean isManagementEnabled() {
        return cache.getConfiguration(CompleteConfiguration.class).isManagementEnabled();
    }

}
