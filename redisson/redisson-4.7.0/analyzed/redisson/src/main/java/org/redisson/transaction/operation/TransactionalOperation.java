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
package org.redisson.transaction.operation;

import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;

/**
 * 事务内单步 Redis 操作的抽象基类。
 * 子类实现 {@link #commit} 与 {@link #rollback}，在事务提交或回滚时执行实际命令并释放锁。
 *
 * @author Nikita Koksharov
 *
 */
public abstract class TransactionalOperation {

    /** 值序列化编解码器。 */
    protected Codec codec;
    /** Redis 键名或结构名称。 */
    protected String name;
    /** 持有事务锁的线程 ID，unlock 时使用。 */
    protected long threadId;
    
    public TransactionalOperation() {
    }
    
    public TransactionalOperation(String name, Codec codec) {
        this.name = name;
        this.codec = codec;
    }

    public TransactionalOperation(String name, Codec codec, long threadId) {
        this.name = name;
        this.codec = codec;
        this.threadId = threadId;
    }

    public long getThreadId() {
        return threadId;
    }

    public Codec getCodec() {
        return codec;
    }
    
    public String getName() {
        return name;
    }
    
    /** 事务提交：将缓冲的操作应用到 Redis。 */
    public abstract void commit(CommandAsyncExecutor commandExecutor);
    
    /** 事务回滚：撤销或跳过变更，通常仅释放事务锁。 */
    public abstract void rollback(CommandAsyncExecutor commandExecutor);
    
}
