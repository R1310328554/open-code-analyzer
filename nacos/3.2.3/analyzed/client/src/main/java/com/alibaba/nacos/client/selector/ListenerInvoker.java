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

/**
 * Listener invoker.
 * <p>监听器调用器接口：封装对内部 {@link com.alibaba.nacos.api.selector.listener.AbstractSelectorListener} 的回调触发，并跟踪是否至少执行过一次 {@link #invoke(Object)}。</p>
 *
 * @param <E> the type of event received by the listener
 * @author lideyou
 */
public interface ListenerInvoker<E> {
    
    /**
     * Invoke inner listener.
     * <p>将事件分发给内部监听器执行；实现类应在此方法首次成功调用后将 {@link #isInvoked()} 置为 true。</p>
     *
     * @param event event
     */
    void invoke(E event);
    
    /**
     * Mark the listener whether invoked once. It should return {@code true} after {@link #invoke(E)} called at lease once.
     * <p>标识监听器是否已被调用过至少一次，用于避免重复通知或统计首次触发。</p>
     *
     * @return {@code true} if this listener has invoked at least once, {@code false} otherwise
     */
    boolean isInvoked();
}
