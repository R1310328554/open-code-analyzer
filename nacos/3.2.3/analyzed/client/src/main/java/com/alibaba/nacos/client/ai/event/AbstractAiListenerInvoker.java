/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.ai.event;

import com.alibaba.nacos.api.ai.listener.NacosAiEvent;
import com.alibaba.nacos.api.ai.listener.NacosAiListener;
import com.alibaba.nacos.client.selector.ListenerInvoker;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Nacos AI 模块监听器调用器抽象基类。
 *
 * <p>封装 {@link NacosAiListener} 的异步/同步回调执行、首次调用标记与
 * 基于 listener 实例的相等性比较，供各类 AI 资源监听器复用。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractAiListenerInvoker<E extends NacosAiEvent, L extends NacosAiListener<E>>
    implements ListenerInvoker<E> {
    
    /** 被包装的 AI 事件监听器实例。 */
    protected final L listener;
    
    /** 标记是否已至少调用过一次，避免订阅时重复推送初始快照。 */
    private final AtomicBoolean invoked = new AtomicBoolean(false);
    
    public AbstractAiListenerInvoker(L listener) {
        this.listener = listener;
    }
    
    @Override
    public void invoke(E event) {
        invoked.set(true);
        logInvoke(event);
        if (listener.getExecutor() != null) {
            listener.getExecutor().execute(() -> listener.onEvent(event));
        } else {
            listener.onEvent(event);
        }
    }
    
    /**
     * 记录监听器被调用的日志。
     *
     * @param event event
     */
    protected abstract void logInvoke(E event);
    
    @Override
    public boolean isInvoked() {
        return invoked.get();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        if (this == o) {
            return true;
        }
        
        AbstractAiListenerInvoker that = (AbstractAiListenerInvoker) o;
        return Objects.equals(listener, that.listener);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(listener);
    }
}
