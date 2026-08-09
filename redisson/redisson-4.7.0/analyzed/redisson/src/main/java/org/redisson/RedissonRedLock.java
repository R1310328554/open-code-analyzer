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

import java.util.List;

import org.redisson.api.RLock;

/**
 * 多锁 RedLock 加锁算法实现，将多个 {@link RLock} 作为整体管理。
 * <p>需在多数实例（N/2+1）上成功加锁才算获取成功。
 *
 * @see <a href="http://redis.io/topics/distlock">http://redis.io/topics/distlock</a>
 * @author Nikita Koksharov
 */
@Deprecated
public class RedissonRedLock extends RedissonMultiLock {

    /**
     * 使用多个 {@link RLock} 创建 RedLock 实例。
     * <p>每个 RLock 可由独立的 Redisson 实例创建。
     *
     * @param locks 锁数组
     */
    public RedissonRedLock(RLock... locks) {
        super(locks);
    }

    @Override
    protected int failedLocksLimit() {
        return locks.size() - minLocksAmount(locks);
    }
    
    protected int minLocksAmount(final List<RLock> locks) {
        return locks.size()/2 + 1;
    }

    @Override
    protected long calcLockWaitTime(long remainTime) {
        return Math.max(remainTime / locks.size(), 1);
    }
    
    @Override
    public void unlock() {
        unlockInner(locks);
    }

}
