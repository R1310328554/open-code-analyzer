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

package com.alibaba.nacos.api.lock;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.lock.model.LockInstance;

/**
 * Nacos 分布式锁服务接口。
 *
 * <p>加锁流程：{@link #lock(LockInstance)} → {@link LockInstance#lock(LockService)} → {@link #remoteTryLock(LockInstance)}<br/>
 * 解锁流程：{@link #unLock(LockInstance)} → {@link LockInstance#unLock(LockService)} → {@link #remoteReleaseLock(LockInstance)}</p>
 *
 * @author 985492783@qq.com
 * @date 2023/8/24 19:49
 */
public interface LockService {
    
    /**
     * 向用户暴露的加锁入口，内部委托 {@link LockInstance#lock(LockService)}。
     *
     * @param instance 锁实例描述
     * @return 加锁成功返回 {@code true}
     * @throws NacosException 远程调用或参数错误时抛出
     */
    @Since("3.0.0")
    Boolean lock(LockInstance instance) throws NacosException;
    
    /**
     * 向用户暴露的解锁入口，内部委托 {@link LockInstance#unLock(LockService)}。
     *
     * @param instance 锁实例描述
     * @return 解锁成功返回 {@code true}
     * @throws NacosException 远程调用或参数错误时抛出
     */
    @Since("3.0.0")
    Boolean unLock(LockInstance instance) throws NacosException;
    
    /**
     * 通过 gRPC 远程尝试加锁。
     *
     * @param instance 锁实例描述
     * @return 加锁成功返回 {@code true}
     * @throws NacosException 远程调用失败时抛出
     */
    @Since("3.0.0")
    Boolean remoteTryLock(LockInstance instance) throws NacosException;
    
    /**
     * 通过 gRPC 远程释放锁。
     *
     * @param instance 锁实例描述
     * @return 解锁成功返回 {@code true}
     * @throws NacosException 远程调用失败时抛出
     */
    @Since("3.0.0")
    Boolean remoteReleaseLock(LockInstance instance) throws NacosException;
    
    /**
     * 关闭锁服务占用的资源（如线程池、gRPC 连接）。
     *
     * @throws NacosException 关闭失败时抛出
     */
    @Since("3.0.0")
    void shutdown() throws NacosException;
}
