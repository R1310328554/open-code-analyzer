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

import java.io.Serializable;

/**
 * 原子锁抽象基类，持有锁资源键并实现 {@link AtomicLockService}。
 *
 * <p>具体互斥/可重入语义由子类（如 {@link com.alibaba.nacos.lock.core.reentrant.mutex.MutexAtomicLock}）实现。</p>
 *
 * @author 985492783@qq.com
 * @description AtomicLock
 * @date 2023/7/10 14:50
 */
public abstract class AbstractAtomicLock implements AtomicLockService, Serializable {
    
    private static final long serialVersionUID = -3460985546856855524L;
    
    /** 锁资源键，全局唯一标识。 */
    private final String key;
    
    /**
     * 以指定键构造原子锁。
     *
     * @param key 锁资源键
     */
    public AbstractAtomicLock(String key) {
        this.key = key;
    }
    
    /** 返回锁资源键。 */
    @Override
    public String getKey() {
        return key;
    }
}
