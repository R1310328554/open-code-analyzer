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

package com.alibaba.nacos.lock.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 锁键复合标识：锁类型 + 资源键名。
 *
 * <p>作为 {@link com.alibaba.nacos.lock.LockManager} 中锁映射的主键，
 * 实现 {@link #equals} 与 {@link #hashCode} 以支持哈希表存储。</p>
 *
 * @author 985492783@qq.com
 * @date 2023/9/7 21:31
 */
public class LockKey implements Serializable {
    
    private static final long serialVersionUID = -3460548121526875524L;
    
    /**
     * 构造锁键。
     *
     * @param lockType 锁实现类型（SPI 标识）
     * @param key      资源键名
     */
    public LockKey(String lockType, String key) {
        this.lockType = lockType;
        this.key = key;
    }
    
    /** 锁实现类型标识。 */
    private String lockType;
    
    /** 锁资源键名。 */
    private String key;
    
    /** 获取锁类型。 */
    public String getLockType() {
        return lockType;
    }
    
    /** 设置锁类型。 */
    public void setLockType(String lockType) {
        this.lockType = lockType;
    }
    
    /** 获取资源键。 */
    public String getKey() {
        return key;
    }
    
    /** 设置资源键。 */
    public void setKey(String key) {
        this.key = key;
    }
    
    /** 按 lockType 与 key 字段比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LockKey lockKey = (LockKey) o;
        return Objects.equals(lockType, lockKey.lockType) && Objects.equals(key, lockKey.key);
    }
    
    /** 基于 lockType 与 key 计算哈希码。 */
    @Override
    public int hashCode() {
        return Objects.hash(lockType, key);
    }
}
