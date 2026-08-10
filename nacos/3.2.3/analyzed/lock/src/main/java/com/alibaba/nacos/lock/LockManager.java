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

package com.alibaba.nacos.lock;

import com.alibaba.nacos.lock.core.reentrant.AtomicLockService;
import com.alibaba.nacos.lock.model.LockKey;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 分布式锁管理器接口：按 {@link LockKey} 获取/移除互斥锁并导出快照。
 *
 * <p>由 {@link NacosLockManager} 实现，供远程 RPC 与持久化模块使用。</p>
 *
 * @author 985492783@qq.com
 * @description LockManager
 * @date 2023/7/10 15:10
 */
public interface LockManager {
    
    /**
     * 获取或创建指定键的可重入互斥锁。
     *
     * @param lockKey 锁键（含类型与资源键）
     * @return {@link AtomicLockService} 实例
     */
    AtomicLockService getMutexLock(LockKey lockKey);
    
    /**
     * 返回全部活跃锁实例映射，供快照持久化。
     *
     * @return lockKey → {@link AtomicLockService} 映射
     */
    ConcurrentHashMap<LockKey, AtomicLockService> showLocks();
    
    /**
     * 从管理器中移除并返回指定互斥锁。
     *
     * @param lockKey 锁键
     * @return 被移除的 {@link AtomicLockService}，不存在时返回 {@code null}
     */
    AtomicLockService removeMutexLock(LockKey lockKey);
}
