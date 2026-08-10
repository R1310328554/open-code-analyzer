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

package com.alibaba.nacos.lock.core.reentrant;

import com.alibaba.nacos.lock.model.LockInfo;

/**
 * 原子锁服务接口：定义加锁、解锁、过期与回收语义。
 *
 * <p>由 {@link com.alibaba.nacos.lock.core.reentrant.mutex.MutexAtomicLock} 等实现，
 * 供 {@link com.alibaba.nacos.lock.LockManager} 统一管理。</p>
 *
 * @author 985492783@qq.com
 * @description AtomicLockService
 * @date 2023/7/10 15:34
 */
public interface AtomicLockService {
    
    /**
     * 尝试获取锁，并设置过期时间。
     *
     * @param lockInfo 锁请求信息（含过期时间戳）
     * @return 加锁成功返回 {@code true}
     */
    Boolean tryLock(LockInfo lockInfo);
    
    /**
     * 释放锁。
     *
     * @param lockInfo 锁实例信息
     * @return 释放成功返回 {@code true}
     */
    Boolean unLock(LockInfo lockInfo);
    
    /**
     * 判断锁是否已自动过期。
     *
     * @return 已过期返回 {@code true}
     */
    Boolean autoExpire();
    
    /**
     * 获取锁资源键。
     *
     * @return 锁键字符串
     */
    String getKey();
    
    /**
     * 判断锁是否可被垃圾回收（空闲或已过期）。
     *
     * @return 可清理返回 {@code true}
     */
    Boolean isClear();
}
