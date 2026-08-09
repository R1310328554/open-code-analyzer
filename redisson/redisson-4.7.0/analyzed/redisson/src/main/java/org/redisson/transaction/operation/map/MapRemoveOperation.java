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
 * 事务内 map 删除操作：可按 key 删除，或按 key+value 条件删除（HDEL）。
 *
 * @author Nikita Koksharov
 *
 */
public class MapRemoveOperation extends MapOperation {

    public MapRemoveOperation() {
    }
    
    public MapRemoveOperation(RMap<?, ?> map, Object key, String transactionId, long threadId) {
        super(map, key, null, transactionId, threadId);
    }
    
    public MapRemoveOperation(RMap<?, ?> map, Object key, Object value, String transactionId, long threadId) {
        super(map, key, value, transactionId, threadId);
    }

    /** 提交：value 非空时按 key-value 删除，否则仅按 key 删除。 */
    @Override
    public void commit(RMap<Object, Object> map) {
        if (value != null) {
            map.removeAsync(key, value);
        } else {
            map.removeAsync(key);
        }
    }
    
}
