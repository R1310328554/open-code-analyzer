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

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.lock.core.reentrant.AtomicLockService;
import com.alibaba.nacos.lock.exception.NacosLockException;
import com.alibaba.nacos.lock.factory.LockFactory;
import com.alibaba.nacos.lock.model.LockKey;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Nacos 分布式锁管理器实现：SPI 加载 {@link LockFactory}，按 lockType 创建并缓存 {@link AtomicLockService}。
 *
 * @author 985492783@qq.com
 * @date 2023/8/22 21:01
 */
@Service
public class NacosLockManager implements LockManager {
    
    /** lockType → {@link LockFactory} SPI 工厂映射。 */
    private final Map<String, LockFactory> factoryMap;
    
    /** 已创建的锁实例缓存：lockKey → {@link AtomicLockService}。 */
    private final ConcurrentHashMap<LockKey, AtomicLockService> atomicLockMap =
        new ConcurrentHashMap<>();
    
    /** 通过 {@link NacosServiceLoader} 加载全部 {@link LockFactory} 实现。 */
    public NacosLockManager() {
        Collection<LockFactory> factories = NacosServiceLoader.load(LockFactory.class);
        factoryMap = factories.stream()
            .collect(
                Collectors.toConcurrentMap(LockFactory::getLockType, lockFactory -> lockFactory));
    }
    
    /** 校验 lockKey 后按类型创建或返回已缓存的互斥锁。 */
    @Override
    public AtomicLockService getMutexLock(LockKey lockKey) {
        if (lockKey == null || lockKey.getLockType() == null || lockKey.getKey() == null) {
            throw new NacosLockException("lockType or lockKey is null.");
        }
        if (!factoryMap.containsKey(lockKey.getLockType())) {
            throw new NacosLockException("lockType: " + lockKey.getLockType() + " is not exist.");
        }
        return atomicLockMap.computeIfAbsent(lockKey, lock -> {
            LockFactory lockFactory = factoryMap.get(lock.getLockType());
            return lockFactory.createLock(lock.getKey());
        });
    }
    
    /** 返回当前全部锁实例映射（供快照导出）。 */
    @Override
    public ConcurrentHashMap<LockKey, AtomicLockService> showLocks() {
        return atomicLockMap;
    }
    
    /** 校验后从缓存移除并返回互斥锁实例。 */
    @Override
    public AtomicLockService removeMutexLock(LockKey lockKey) {
        if (lockKey == null || lockKey.getLockType() == null || lockKey.getKey() == null) {
            throw new NacosLockException("lockType or lockKey is null.");
        }
        if (!factoryMap.containsKey(lockKey.getLockType())) {
            throw new NacosLockException("lockType: " + lockKey.getLockType() + " is not exist.");
        }
        return atomicLockMap.remove(lockKey);
    }
    
}
