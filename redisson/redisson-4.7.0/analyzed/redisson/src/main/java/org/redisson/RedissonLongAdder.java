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
package org.redisson;

import org.redisson.api.RFuture;
import org.redisson.api.RLongAdder;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * {@link org.redisson.api.RLongAdder} 的分布式长整型累加器。
 * <p>本地 {@link java.util.concurrent.atomic.LongAdder} 缓冲增量，
 * {@link #sum()} 时通过 {@link RedissonBaseAdder} Topic 协议汇总各节点计数。
 *
 * @author Nikita Koksharov
 */
public class RedissonLongAdder extends RedissonBaseAdder<Long> implements RLongAdder {

    private final RedissonClient redisson;
    private final LongAdder counter = new LongAdder();
    
    /** @param name 累加器 Redis 键名前缀 */
    public RedissonLongAdder(CommandAsyncExecutor connectionManager, String name, RedissonClient redisson) {
        super(connectionManager, name, redisson);

        this.redisson = redisson;
    }

    @Override
    /** 重置本地 {@link LongAdder}。 */
    protected void doReset() {
        counter.reset();
    }
    
    @Override
    protected RFuture<Long> addAndGetAsync(String id) {
        return redisson.getAtomicLong(getCounterName(id)).getAndAddAsync(counter.sum());
    }
    
    @Override
    protected RFuture<Long> getAndDeleteAsync(String id) {
        return redisson.getAtomicLong(getCounterName(id)).getAndDeleteAsync();
    }

    @Override
    /** 本地累加 {@code x}，不立即写 Redis。 */
    public void add(long x) {
        counter.add(x);
    }

    @Override
    public void increment() {
        add(1L);
    }

    @Override
    public void decrement() {
        add(-1L);
    }

    @Override
    /** 汇总各节点局部计数并返回总和（默认超时 60 秒）。 */
    public long sum() {
        return get(sumAsync(60, TimeUnit.SECONDS));
    }
    
}
