/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.lock.core.reentrant.mutex;

import com.alibaba.nacos.lock.core.reentrant.AbstractAtomicLock;
import com.alibaba.nacos.lock.model.LockInfo;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于 {@link AtomicInteger} 的互斥原子锁实现。
 *
 * <p>状态在 {@code EMPTY(0)} 与 {@code FULL(1)} 间切换，支持过期自动释放。</p>
 *
 * @author 985492783@qq.com
 * @description MutexAtomicLock
 * @date 2023/7/10 15:33
 */
public class MutexAtomicLock extends AbstractAtomicLock {
    
    /** 锁空闲状态值。 */
    private static final Integer EMPTY = 0;
    
    /** 锁占用状态值。 */
    private static final Integer FULL = 1;
    
    /** 锁状态计数器，CAS 保证互斥。 */
    private final AtomicInteger state;
    
    /** 锁过期时间戳（毫秒）。 */
    private Long expiredTimestamp;
    
    /**
     * 以指定键创建互斥锁，初始状态为空闲。
     *
     * @param key 锁资源键
     */
    public MutexAtomicLock(String key) {
        super(key);
        this.state = new AtomicInteger(EMPTY);
    }
    
    /** CAS 尝试加锁；若已过期则强制抢占。 */
    @Override
    public Boolean tryLock(LockInfo lockInfo) {
        Long endTime = lockInfo.getEndTime();
        if (state.compareAndSet(EMPTY, FULL) || autoExpire()) {
            this.expiredTimestamp = endTime;
            return true;
        }
        return false;
    }
    
    /** CAS 将状态从 FULL 恢复为 EMPTY。 */
    @Override
    public Boolean unLock(LockInfo lockInfo) {
        return state.compareAndSet(FULL, EMPTY);
    }
    
    /** 比较当前时间与过期时间戳判定是否过期。 */
    @Override
    public Boolean autoExpire() {
        return System.currentTimeMillis() >= this.expiredTimestamp;
    }
    
    /** 空闲或已过期时返回 {@code true}，表示可被 GC 清理。 */
    @Override
    public Boolean isClear() {
        return EMPTY.equals(state.get()) || autoExpire();
    }
    
}
