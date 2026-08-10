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

package com.alibaba.nacos.api.lock.model;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.lock.LockService;

import java.io.Serializable;
import java.util.Map;

/**
 * 分布式锁实例信息实体。
 *
 * <p>封装锁键、过期时间、锁类型及扩展参数，供 {@link LockService} 远程加锁/解锁使用。</p>
 *
 * @author 985492783@qq.com
 * @date 2023/6/28 2:46
 */
public class LockInstance implements Serializable {
    
    private static final long serialVersionUID = -3460985546826875524L;
    
    /** 锁资源键，全局唯一标识一把锁。 */
    private String key;
    
    /** 锁过期时间戳（毫秒），超时自动释放。 */
    private Long expiredTime;
    
    /** 扩展参数字典，可携带 SPI 或业务自定义属性。 */
    private Map<String, ? extends Serializable> params;
    
    /** 锁实现类型，由 SPI 扩展决定。 */
    private String lockType;
    
    /**
     * 构造指定键、过期时间与锁类型的锁实例。
     *
     * @param key         锁键
     * @param expiredTime 过期时间戳（毫秒）
     * @param lockType    锁类型标识
     */
    public LockInstance(String key, Long expiredTime, String lockType) {
        this.key = key;
        this.expiredTime = expiredTime;
        this.lockType = lockType;
    }
    
    /** 无参构造，供序列化或框架实例化使用。 */
    public LockInstance() {
    }
    
    /** 获取锁过期时间戳。 */
    public Long getExpiredTime() {
        return expiredTime;
    }
    
    /** 设置锁过期时间戳。 */
    public void setExpiredTime(Long expiredTime) {
        this.expiredTime = expiredTime;
    }
    
    /** 获取锁键。 */
    public String getKey() {
        return key;
    }
    
    /** 设置锁键。 */
    public void setKey(String key) {
        this.key = key;
    }
    
    /** 获取扩展参数。 */
    public Map<String, ? extends Serializable> getParams() {
        return params;
    }
    
    /** 设置扩展参数。 */
    public void setParams(Map<String, ? extends Serializable> params) {
        this.params = params;
    }
    
    /**
     * 尝试获取分布式锁。
     *
     * <p>内部调用 {@link LockService#remoteTryLock(LockInstance)} 通过 gRPC 向服务端申请锁；
     * 子类可 {@link Override} 以注入客户端特有逻辑。</p>
     *
     * @param lockService 锁服务实例
     * @return 加锁成功返回 {@code true}，否则 {@code false}
     * @throws NacosException 远程调用失败时抛出
     */
    public Boolean lock(LockService lockService) throws NacosException {
        return lockService.remoteTryLock(this);
    }
    
    /**
     * 释放分布式锁。
     *
     * <p>内部调用 {@link LockService#remoteReleaseLock(LockInstance)} 通过 gRPC 向服务端释放锁；
     * 子类可 {@link Override} 以注入客户端特有逻辑。</p>
     *
     * @param lockService 锁服务实例
     * @return 释放成功返回 {@code true}，否则 {@code false}
     * @throws NacosException 远程调用失败时抛出
     */
    public Boolean unLock(LockService lockService) throws NacosException {
        return lockService.remoteReleaseLock(this);
    }
    
    /**
     * 获取 SPI 锁类型标识。
     *
     * @return 锁类型字符串
     */
    public String getLockType() {
        return lockType;
    }
    
    /** 设置锁类型标识。 */
    public void setLockType(String lockType) {
        this.lockType = lockType;
    }
}
