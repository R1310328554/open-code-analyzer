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
 * 事务内 map 快速「键不存在则写入」操作：commit 时调用 {@link RMap#fastPutIfAbsentAsync}。
 *
 * @author Nikita Koksharov
 *
 */
public class MapFastPutIfAbsentOperation extends MapOperation {

    public MapFastPutIfAbsentOperation() {
    }
    
    public MapFastPutIfAbsentOperation(RMap<?, ?> map, Object key, Object value, String transactionId, long threadId) {
        super(map, key, value, transactionId, threadId);
    }

    /** 提交：仅当 key 不存在时写入 value。 */
    @Override
    public void commit(RMap<Object, Object> map) {
        map.fastPutIfAbsentAsync(key, value);
    }
    
}
