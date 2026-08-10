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

package com.alibaba.nacos.client.selector;

import com.alibaba.nacos.api.selector.client.Selector;
import com.alibaba.nacos.common.notify.Event;

import java.util.Objects;

/**
 * 选择器包装器抽象基类。
 *
 * <p>将 {@link Selector} 与 {@link ListenerInvoker} 组合：过滤原始事件、构建回调事件并在同步块内通知监听器。</p>
 *
 * @param <S> 选择器类型
 * @param <T> 原始事件类型
 * @param <E> 监听器回调事件类型
 * @author lideyou
 */
public abstract class AbstractSelectorWrapper<S extends Selector<?, ?>, E, T extends Event> {
    
    /** 绑定的选择器实例。 */
    private final S selector;
    
    /** 监听器调用器（含是否已回调状态）。 */
    private final ListenerInvoker<E> listener;
    
    /** 绑定选择器与监听器调用器。 */
    public AbstractSelectorWrapper(S selector, ListenerInvoker<E> listener) {
        this.selector = selector;
        this.listener = listener;
    }
    
    /**
     * 判断原始事件是否可进入选择与回调流程。
     *
     * @param event 原始事件
     * @return 可处理时返回 true
     */
    protected abstract boolean isSelectable(T event);
    
    /**
     * 判断选择/构建后的结果是否应回调监听器。
     *
     * @param event 选择结果事件
     * @return 应回调时返回 true
     */
    protected abstract boolean isCallable(E event);
    
    /**
     * 由原始事件构建监听器收到的事件对象。
     *
     * @param event 原始事件
     * @return 监听器事件
     */
    protected abstract E buildListenerEvent(T event);
    
    /**
     * 若事件可选且结果可回调，则同步调用监听器。
     *
     * @param event 原始事件
     */
    public void notifyListener(T event) {
        if (!isSelectable(event)) {
            return;
        }
        E newEvent = buildListenerEvent(event);
        if (isCallable(newEvent)) {
            // 同步 listener，保证 isInvoked 状态线程安全
            synchronized (listener) {
                listener.invoke(newEvent);
            }
        }
    }
    
    /**
     * 仅在监听器尚未被调用时通知（用于首次推送补偿）。
     *
     * @param event 原始事件
     */
    public void notifyIfListenerIfNotNotified(T event) {
        if (!isSelectable(event)) {
            return;
        }
        E newEvent = buildListenerEvent(event);
        synchronized (listener) {
            if (!listener.isInvoked()) {
                listener.invoke(newEvent);
            }
        }
    }
    
    /** 返回监听器调用器。 */
    public ListenerInvoker<E> getListener() {
        return this.listener;
    }
    
    /** 返回绑定的选择器。 */
    public S getSelector() {
        return this.selector;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractSelectorWrapper<?, ?, ?> that = (AbstractSelectorWrapper<?, ?, ?>) o;
        return Objects.equals(selector, that.selector) && Objects.equals(listener, that.listener);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(selector, listener);
    }
}
