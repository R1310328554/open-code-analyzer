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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An abstract class for event.
 * <p>NotifyCenter 事件基类：全局递增序号保证单调性，子类可覆盖 {@link #scope()} 限定订阅范围或标记插件事件。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @author zongtanghu
 */
public abstract class Event implements Serializable {
    
    private static final long serialVersionUID = -3731383194964997493L;
    
    /** 全进程共享的事件序号生成器 */
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    
    /** 实例创建时分配的唯一递增序号 */
    private final long sequence = SEQUENCE.getAndIncrement();
    
    /**
     * Event sequence number, which can be used to handle the sequence of events.
     * <p>返回事件序号，用于订阅者判断是否为过期事件。</p>
     *
     * @return sequence num, It's best to make sure it's monotone.
     */
    public long sequence() {
        return sequence;
    }
    
    /**
     * Event scope.
     * <p>事件作用域；返回 null 表示对所有订阅者可见。</p>
     *
     * @return event scope, return null if for all scope
     */
    public String scope() {
        return null;
    }
    
    /**
     * Whether is plugin event. If so, the event can be dropped when no publish and subscriber without any hint. Default
     * false
     * <p>是否为插件事件；无发布者与订阅者时可静默丢弃，默认 false。</p>
     *
     * @return {@code true} if is plugin event, otherwise {@code false}
     */
    public boolean isPluginEvent() {
        return false;
    }
}
