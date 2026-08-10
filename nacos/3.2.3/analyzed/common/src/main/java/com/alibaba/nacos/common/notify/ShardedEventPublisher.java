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

package com.alibaba.nacos.common.notify;

import com.alibaba.nacos.common.notify.listener.Subscriber;

/**
 * Sharded event publisher.
 * <p>分片事件发布器：单个发布器实例可承载多种事件类型，订阅/取消订阅时需显式指定 {@code subscribeType} 以区分分片。</p>
 * <p>To support one publisher for different events.
 *
 * @author xiweng.yy
 */
public interface ShardedEventPublisher extends EventPublisher {
    
    /**
     * Add listener for default share publisher.
     * <p>为指定事件类型注册订阅者。</p>
     *
     * @param subscriber    {@link Subscriber}
     * @param subscribeType subscribe event type, such as slow event or general event.
     */
    void addSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType);
    
    /**
     * Remove listener for default share publisher.
     * <p>从指定事件类型分片移除订阅者。</p>
     *
     * @param subscriber    {@link Subscriber}
     * @param subscribeType subscribe event type, such as slow event or general event.
     */
    void removeSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType);
}
