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

import org.redisson.api.RMap;

/**
 * Map 字段原子递增（addAndGet / HINCRBY）的事务操作：
 * 对指定 key 的数值字段加上 delta，由 {@link MapOperation} 负责加锁与解锁。
 *
 * @author Nikita Koksharov
 *
 */
public class MapAddAndGetOperation extends MapOperation {

    public MapAddAndGetOperation() {
    }
    
    public MapAddAndGetOperation(RMap<?, ?> map, Object key, Object value, String transactionId, long threadId) {
        super(map, key, value, transactionId, threadId);
    }

    /** 提交：对 map 字段执行 addAndGetAsync。 */
    @Override
    public void commit(RMap<Object, Object> map) {
        map.addAndGetAsync(key, (Number) value);
    }
    
}
