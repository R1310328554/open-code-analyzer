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
package org.redisson.transaction.operation.map;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RMap;
import org.redisson.api.RMapCache;

/**
 * 事务内 {@link RMapCache} 带 TTL / maxIdle 的写入操作。
 * commit 时调用 {@link RMapCache#putAsync(Object, Object, long, TimeUnit, long, TimeUnit)}。
 *
 * @author Nikita Koksharov
 *
 */
public class MapCachePutOperation extends MapOperation {

    /** 条目固定存活时间数值。 */
    private long ttlTimeout;
    /** ttlTimeout 的时间单位。 */
    private TimeUnit ttlUnit;
    /** 最大空闲时间数值（未访问则过期）。 */
    private long maxIdleTimeout;
    /** maxIdleTimeout 的时间单位。 */
    private TimeUnit maxIdleUnit;
    
    public MapCachePutOperation() {
    }
    
    public MapCachePutOperation(RMap<?, ?> map, Object key, Object value, long ttlTimeout, TimeUnit ttlUnit, long maxIdleTimeout, TimeUnit maxIdleUnit, String transactionId, long threadId) {
        super(map, key, value, transactionId, threadId);
        this.ttlTimeout = ttlTimeout;
        this.ttlUnit = ttlUnit;
        this.maxIdleTimeout = maxIdleTimeout;
        this.maxIdleUnit = maxIdleUnit;
    }
    
    /** 提交：写入带过期策略的 map 条目。 */
    @Override
    public void commit(RMap<Object, Object> map) {
        ((RMapCache<Object, Object>) map).putAsync(key, value, ttlTimeout, ttlUnit, maxIdleTimeout, maxIdleUnit);
    }
    
    public long getTTL() {
        return ttlTimeout;
    }
    
    public TimeUnit getTTLUnit() {
        return ttlUnit;
    }
    
    public long getMaxIdleTimeout() {
        return maxIdleTimeout;
    }
    
    public TimeUnit getMaxIdleUnit() {
        return maxIdleUnit;
    }
    
}
