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

package com.alibaba.nacos.lock.factory;

import com.alibaba.nacos.lock.core.reentrant.AbstractAtomicLock;

/**
 * 锁工厂 SPI 接口：按类型创建 {@link AbstractAtomicLock} 实例。
 *
 * <p>通过 Java SPI 扩展不同锁实现（互斥、可重入等）。</p>
 *
 * @author 985492783@qq.com
 * @date 2023/8/22 20:57
 */
public interface LockFactory {
    
    /**
     * 返回 SPI 锁工厂类型标识。
     *
     * @return 锁类型字符串
     */
    String getLockType();
    
    /**
     * 创建指定键的原子锁实例。
     *
     * @param key 锁资源键
     * @return {@link AbstractAtomicLock} 实例
     */
    AbstractAtomicLock createLock(String key);
}
