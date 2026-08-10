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

package com.alibaba.nacos.common.notify.listener;

import com.alibaba.nacos.common.notify.Event;

import java.util.concurrent.Executor;

/**
 * An abstract subscriber class for subscriber interface.
 * <p>事件订阅者抽象基类：定义 {@link #onEvent} 回调、订阅类型、可选异步 {@link Executor} 及 scope/过期过滤策略。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @author zongtanghu
 */
public abstract class Subscriber<T extends Event> {
    
    /**
     * Event callback.
     * <p>收到匹配事件时的业务处理入口。</p>
     *
     * @param event {@link Event}
     */
    public abstract void onEvent(T event);
    
    /**
     * Type of this subscriber's subscription.
     * <p>返回本订阅者监听的事件类型。</p>
     *
     * @return Class which extends {@link Event}
     */
    public abstract Class<? extends Event> subscribeType();
    
    /**
     * It is up to the listener to determine whether the callback is asynchronous or synchronous.
     * <p>返回非 null 时发布器在指定线程池异步回调，否则同步执行。</p>
     *
     * @return {@link Executor}
     */
    public Executor executor() {
        return null;
    }
    
    /**
     * Whether to ignore expired events.
     * <p>为 true 时跳过序号小于已处理最大序号的事件。</p>
     *
     * @return default value is {@link Boolean#FALSE}
     */
    public boolean ignoreExpireEvent() {
        return false;
    }
    
    /**
     * Whether the event's scope matches current subscriber. Default implementation is all scopes matched.
     * If you override this method, it better to override related {@link com.alibaba.nacos.common.notify.Event#scope()}.
     * <p>判断事件 scope 是否与本订阅者匹配，默认接受所有 scope。</p>
     *
     * @param event {@link Event}
     * @return Whether the event's scope matches current subscriber
     */
    public boolean scopeMatches(T event) {
        return true;
    }
}
