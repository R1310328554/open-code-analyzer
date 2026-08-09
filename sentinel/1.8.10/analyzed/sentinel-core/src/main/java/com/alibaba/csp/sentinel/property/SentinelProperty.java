/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.property;

/**
 * <p>
 * 保存配置的当前值，并在配置更新时通知所有已注册的 {@link PropertyListener}。
 * </p>
 * <p>
 * 注意：并非每次 {@link #updateValue(Object newValue)} 都需通知监听器，
 * 仅当 {@code newValue} 与旧值不相等时才通知。
 * </p>
 *
 * @param <T> 配置值类型
 * @author Carpenter Lee
 */
public interface SentinelProperty<T> {

    /**
     * <p>
     * 向本 {@link SentinelProperty} 添加 {@link PropertyListener}。
     * 添加后，{@link #updateValue(Object)} 在需要时会通知该监听器。
     * </p>
     * <p>
     * 可多次调用以添加多个监听器。
     * </p>
     *
     * @param listener 要添加的监听器
     */
    void addListener(PropertyListener<T> listener);

    /**
     * 移除本属性上的 {@link PropertyListener}。移除后 {@link #updateValue(Object)}
     * 将不再通知该监听器。
     *
     * @param listener 要移除的监听器
     */
    void removeListener(PropertyListener<T> listener);

    /**
     * 将 {@code newValue} 更新为当前值；仅当新值与旧值不相等时通知所有 {@link PropertyListener}。
     *
     * @param newValue 新值
     * @return 若属性值已更新返回 true，否则 false
     */
    boolean updateValue(T newValue);
}
