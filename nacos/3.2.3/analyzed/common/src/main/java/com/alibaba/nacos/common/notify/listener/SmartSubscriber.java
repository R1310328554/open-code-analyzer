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

import java.util.List;

/**
 * Subscribers to multiple events can be listened to.
 * <p>智能订阅者：通过 {@link #subscribeTypes()} 一次声明多种关注事件，由 {@link NotifyCenter} 分别注册到对应发布器。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @author zongtanghu
 */
public abstract class SmartSubscriber extends Subscriber<Event> {
    
    /**
     * Returns which event type are smart subscriber interested in.
     * <p>返回本订阅者关注的所有事件类型列表。</p>
     *
     * @return The interested event types.
     */
    public abstract List<Class<? extends Event>> subscribeTypes();
    
    /** 多事件订阅模式下不使用单一 subscribeType，固定返回 null */
    @Override
    public final Class<? extends Event> subscribeType() {
        return null;
    }
    
    @Override
    public final boolean ignoreExpireEvent() {
        return false;
    }
}
