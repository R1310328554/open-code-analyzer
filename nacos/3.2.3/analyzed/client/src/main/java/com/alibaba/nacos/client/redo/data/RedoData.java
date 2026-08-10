/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.redo.data;

import java.util.Objects;

/**
 * 客户端 Redo 缓存数据抽象基类。
 *
 * <p>跟踪注册/注销期望状态与实际状态，供 {@link com.alibaba.nacos.client.redo.service.AbstractRedoService} 在 gRPC 断连后重试注册或注销。</p>
 *
 * @author xiweng.yy
 */
public abstract class RedoData<T> {
    
    /**
     * 最终期望状态。
     *
     * <ul>
     *     <li>{@code true}：期望最终在服务端处于已注册状态。</li>
     *     <li>{@code false}：期望最终在服务端已注销。</li>
     * </ul>
     */
    private volatile boolean expectedRegistered;
    
    /** 为 {@code true} 表示已成功注册到服务端。 */
    private volatile boolean registered;
    
    /** 为 {@code true} 表示正在执行注销流程。 */
    private volatile boolean unregistering;
    
    /** 业务负载（如实例、订阅信息等）。 */
    private T data;
    
    /** 默认期望最终为已注册状态。 */
    protected RedoData() {
        this.expectedRegistered = true;
    }
    
    /** 设置最终期望注册状态。 */
    public void setExpectedRegistered(boolean registered) {
        this.expectedRegistered = registered;
    }
    
    /** 是否期望最终保持注册。 */
    public boolean isExpectedRegistered() {
        return expectedRegistered;
    }
    
    /** 当前是否已在服务端注册成功。 */
    public boolean isRegistered() {
        return registered;
    }
    
    /** 是否处于注销中。 */
    public boolean isUnregistering() {
        return unregistering;
    }
    
    /** 设置已注册标志。 */
    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
    
    /** 设置注销中标志。 */
    public void setUnregistering(boolean unregistering) {
        this.unregistering = unregistering;
    }
    
    /** 获取业务负载。 */
    public T get() {
        return data;
    }
    
    /** 设置业务负载。 */
    public void set(T data) {
        this.data = data;
    }
    
    /** 标记注册成功并清除注销中状态。 */
    public void registered() {
        this.registered = true;
        this.unregistering = false;
    }
    
    /** 标记未注册且处于注销流程。 */
    public void unregistered() {
        this.registered = false;
        this.unregistering = true;
    }
    
    /** 根据 {@link #getRedoType()} 判断是否需要执行 redo。 */
    public boolean isNeedRedo() {
        return !RedoType.NONE.equals(getRedoType());
    }
    
    /**
     * 根据当前状态与期望状态计算 redo 类型。
     *
     * <ul>
     *     <li>已注册且非注销中：期望仍注册则 {@link RedoType#NONE}，否则 {@link RedoType#UNREGISTER}。</li>
     *     <li>已注册且注销中：执行 {@link RedoType#UNREGISTER}。</li>
     *     <li>未注册且非注销中：执行 {@link RedoType#REGISTER}。</li>
     *     <li>未注册且注销中：期望注册则 {@link RedoType#REGISTER}，否则 {@link RedoType#REMOVE}。</li>
     * </ul>
     *
     * @return redo 类型
     */
    public RedoType getRedoType() {
        if (isRegistered() && !isUnregistering()) {
            return expectedRegistered ? RedoType.NONE : RedoType.UNREGISTER;
        } else if (isRegistered() && isUnregistering()) {
            return RedoType.UNREGISTER;
        } else if (!isRegistered() && !isUnregistering()) {
            return RedoType.REGISTER;
        } else {
            return expectedRegistered ? RedoType.REGISTER : RedoType.REMOVE;
        }
    }
    
    public enum RedoType {
        
        /** 需要重新注册。 */
        REGISTER,
        
        /** 需要重新注销。 */
        UNREGISTER,
        
        /** 无需 redo。 */
        NONE,
        
        /** 从 redo 缓存中移除。 */
        REMOVE;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RedoData<?> redoData = (RedoData<?>) o;
        return registered == redoData.registered && unregistering == redoData.unregistering
            && Objects.equals(data,
                redoData.data);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(registered, unregistering, data);
    }
}
