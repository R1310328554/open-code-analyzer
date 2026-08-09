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
package org.redisson.transaction.operation.set;

import org.redisson.RedissonSetCache;
import org.redisson.api.RObject;
import org.redisson.api.RSetCache;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;

/**
 * 事务内 {@link RSetCache} 删除元素操作。
 * commit 时 SREM 并释放元素级事务锁；rollback 仅解锁。
 *
 * @author Nikita Koksharov
 *
 */
public class RemoveCacheOperation extends SetOperation {

    /** 待删除的元素值。 */
    private Object value;

    public RemoveCacheOperation(RObject set, Object value, String transactionId, long threadId) {
        this(set.getName(), set.getCodec(), value, transactionId, threadId);
    }
    
    public RemoveCacheOperation(String name, Codec codec, Object value, String transactionId, long threadId) {
        super(name, codec, transactionId, threadId);
        this.value = value;
    }

    /** 提交：从 SetCache 移除 value 并 unlock。 */
    @Override
    public void commit(CommandAsyncExecutor commandExecutor) {
        RSetCache<Object> set = new RedissonSetCache<>(codec, null, commandExecutor, name, null);
        set.removeAsync(value);
        getLock(set, commandExecutor, value).unlockAsync(threadId);
    }

    /** 回滚：不修改 set，仅释放元素锁。 */
    @Override
    public void rollback(CommandAsyncExecutor commandExecutor) {
        RSetCache<Object> set = new RedissonSetCache<>(codec, null, commandExecutor, name, null);
        getLock(set, commandExecutor, value).unlockAsync(threadId);
    }
    
    public Object getValue() {
        return value;
    }

}
