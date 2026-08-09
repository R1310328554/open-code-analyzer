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

import java.util.concurrent.TimeUnit;

import org.redisson.RedissonSetCache;
import org.redisson.api.RObject;
import org.redisson.api.RSetCache;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;

/**
 * 事务内 {@link RSetCache} 添加元素操作，可选 TTL。
 * commit 时写入 set 并释放元素级事务锁；rollback 仅解锁。
 *
 * @author Nikita Koksharov
 *
 */
public class AddCacheOperation extends SetOperation {

    /** 待添加的元素值。 */
    private Object value;
    /** 元素存活时间数值（0 表示无 TTL）。 */
    private long ttl;
    /** ttl 的时间单位；为 null 时不设置过期。 */
    private TimeUnit timeUnit;

    public AddCacheOperation(RObject set, Object value, String transactionId, long threadId) {
        this(set, value, 0, null, transactionId, threadId);
    }
    
    public AddCacheOperation(RObject set, Object value, long ttl, TimeUnit timeUnit, String transactionId, long threadId) {
        this(set.getName(), set.getCodec(), value, ttl, timeUnit, transactionId, threadId);
    }

    public AddCacheOperation(String name, Codec codec, Object value, long ttl, TimeUnit timeUnit, String transactionId, long threadId) {
        super(name, codec, transactionId, threadId);
        this.value = value;
        this.timeUnit = timeUnit;
        this.ttl = ttl;
    }

    /** 提交：SADD 到 SetCache（可选 EXPIRE）并 unlock。 */
    @Override
    public void commit(CommandAsyncExecutor commandExecutor) {
        RSetCache<Object> set = new RedissonSetCache<>(codec, null, commandExecutor, name, null);
        if (timeUnit != null) {
            set.addAsync(value, ttl, timeUnit);
        } else {
            set.addAsync(value);
        }
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
    
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    
    public long getTTL() {
        return ttl;
    }
    
}
