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

import org.redisson.RedissonMap;
import org.redisson.RedissonMapCache;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.transaction.RedissonTransactionalLock;
import org.redisson.transaction.operation.TransactionalOperation;

/**
 * 事务内 {@link RMap} / {@link RMapCache} 写操作的抽象基类。
 * commit 时执行子类 {@link #commit(RMap)} 并释放 map 键级事务锁；rollback 仅解锁。
 *
 * @author Nikita Koksharov
 *
 */
public abstract class MapOperation extends TransactionalOperation {

    /** 目标 map 键。 */
    Object key;
    /** 写入或替换的新值。 */
    Object value;
    /** 条件更新时的旧值（replace 等场景）。 */
    Object oldValue;
    /** 参与事务的 map 实例（用于构造与类型判断）。 */
    RMap<?, ?> map;
    /** 当前事务 ID，构造 {@link RedissonTransactionalLock} 时使用。 */
    String transactionId;

    public MapOperation() {
    }
    
    public MapOperation(RMap<?, ?> map, Object key, Object value, String transactionId, long threadId) {
        this(map, key, value, null, transactionId, threadId);
    }
    
    public MapOperation(RMap<?, ?> map, Object key, Object value, Object oldValue, String transactionId, long threadId) {
        super(map.getName(), map.getCodec(), threadId);
        this.map = map;
        this.key = key;
        this.value = value;
        this.oldValue = oldValue;
        this.transactionId = transactionId;
    }

    public Object getKey() {
        return key;
    }
    
    public RMap<?, ?> getMap() {
        return map;
    }
    
    /** 提交：执行 map 写操作后异步释放键锁。 */
    @Override
    public final void commit(CommandAsyncExecutor commandExecutor) {
        RMap<Object, Object> map = getMap(commandExecutor);
        commit(map);
        getLock(map, commandExecutor, key).unlockAsync(threadId);
    }

    /** 按原始 map 类型重建 {@link RedissonMap} 或 {@link RedissonMapCache} 执行器视图。 */
    protected RMap<Object, Object> getMap(CommandAsyncExecutor commandExecutor) {
        if (map instanceof RMapCache) {
            return new RedissonMapCache<>(codec, null, commandExecutor, name, null, null, null);
        }
        return new RedissonMap<>(codec, commandExecutor, name, null, null, null);
    }
    
    /** 回滚：不修改 map，仅释放键级事务锁。 */
    @Override
    public void rollback(CommandAsyncExecutor commandExecutor) {
        RMap<Object, Object> map = getMap(commandExecutor);
        getLock(map, commandExecutor, key).unlockAsync(threadId);
    }

    /** 获取 map 指定键对应的事务写锁。 */
    protected RLock getLock(RMap<?, ?> map, CommandAsyncExecutor commandExecutor, Object key) {
        String lockName = ((RedissonMap<?, ?>) map).getLockByMapKey(key, "lock");
        return new RedissonTransactionalLock(commandExecutor, lockName, transactionId);
    }
    
    /** 子类实现具体的 map 写命令（异步）。 */
    protected abstract void commit(RMap<Object, Object> map);

    public Object getValue() {
        return value;
    }
    
    public Object getOldValue() {
        return oldValue;
    }
}
