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
package org.redisson.hibernate.region;

import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.TimestampsRegion;
import org.redisson.api.RMapCache;
import org.redisson.connection.ServiceManager;

import java.util.Properties;

/**
 * Hibernate 5.2 查询失效时间戳 Region，基于 Redisson {@link RMapCache}。
 * <p>用于跟踪表/空间更新时间以支持查询缓存失效。</p>
 *
 * @author Nikita Koksharov
 */
public class RedissonTimestampsRegion extends BaseRegion implements TimestampsRegion {

    /** 构造时间戳 Region，metadata 为 null（非实体类缓存）。 */
    public RedissonTimestampsRegion(RMapCache<Object, Object> mapCache, ServiceManager serviceManager,
            RegionFactory regionFactory, Properties properties, String defaultKey) {
        super(mapCache, serviceManager, regionFactory, null, properties, defaultKey);
    }

}
