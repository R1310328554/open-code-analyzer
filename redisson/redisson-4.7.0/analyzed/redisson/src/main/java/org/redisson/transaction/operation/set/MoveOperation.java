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

import org.redisson.RedissonSet;
import org.redisson.api.RObject;
import org.redisson.api.RSet;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;

/**
 * 事务内 set 元素迁移操作：将 value 从源 set 移动到目标 set。
 * commit 时执行 move 并释放源、目标两侧的元素锁；rollback 仅解锁。
 *
 * @author Nikita Koksharov
 *
 */
public class MoveOperation extends SetOperation {

    /** 目标 set 的 Redis 键名。 */
    private String destinationName;
    /** 待迁移的元素值。 */
    private Object value;

    public MoveOperation(RObject set, String destinationName, long threadId, Object value, String transactionId) {
        this(set.getName(), set.getCodec(), destinationName, threadId, value, transactionId);
    }
    
    public MoveOperation(String name, Codec codec, String destinationName, long threadId, Object value, String transactionId) {
        super(name, codec, transactionId, threadId);
        this.destinationName = destinationName;
        this.value = value;
    }

    /** 提交：SMOVE 到目标 set，并释放两侧元素锁。 */
    @Override
    public void commit(CommandAsyncExecutor commandExecutor) {
        RSet<Object> set = new RedissonSet<Object>(codec, commandExecutor, name, null);
        RSet<Object> destinationSet = new RedissonSet<Object>(codec, commandExecutor, destinationName, null);
        set.moveAsync(destinationSet.getName(), value);

        getLock(destinationSet, commandExecutor, value).unlockAsync(threadId);
        getLock(set, commandExecutor, value).unlockAsync(threadId);
    }

    /** 回滚：不执行 move，仅释放源与目标元素锁。 */
    @Override
    public void rollback(CommandAsyncExecutor commandExecutor) {
        RSet<Object> set = new RedissonSet<Object>(codec, commandExecutor, name, null);
        RSet<Object> destinationSet = new RedissonSet<Object>(codec, commandExecutor, destinationName, null);
        
        getLock(destinationSet, commandExecutor, value).unlockAsync(threadId);
        getLock(set, commandExecutor, value).unlockAsync(threadId);
    }
    
    public String getDestinationName() {
        return destinationName;
    }
    
    public Object getValue() {
        return value;
    }
    
    public long getThreadId() {
        return threadId;
    }

}
