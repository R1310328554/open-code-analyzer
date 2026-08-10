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

package com.alibaba.nacos.lock.service;

import com.alibaba.nacos.api.lock.model.LockInstance;

/**
 * 锁操作业务服务接口。
 *
 * <p>封装 {@link com.alibaba.nacos.api.lock.model.LockInstance} 到内部 {@link com.alibaba.nacos.lock.model.LockInfo}
 * 的转换，并驱动 Raft 一致性加锁/解锁流程。</p>
 *
 * @author 985492783@qq.com
 * @date 2023/6/28 2:38
 */
public interface LockOperationService {
    
    /**
     * 尝试获取分布式锁。
     *
     * @param lockInstance 客户端锁实例
     * @return 加锁成功返回 {@code true}
     */
    Boolean lock(LockInstance lockInstance);
    
    /**
     * 释放分布式锁。
     *
     * @param lockInstance 客户端锁实例
     * @return 释放成功返回 {@code true}
     */
    Boolean unLock(LockInstance lockInstance);
    
}
