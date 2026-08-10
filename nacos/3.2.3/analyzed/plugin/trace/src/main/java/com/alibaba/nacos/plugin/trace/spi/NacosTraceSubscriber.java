/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.trace.spi;

import com.alibaba.nacos.common.trace.event.TraceEvent;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Nacos 链路追踪事件订阅者 SPI 接口。
 *
 * <p>实现类声明感兴趣的事件类型，并在事件发生时接收回调通知。</p>
 *
 * @author xiweng.yy
 */
public interface NacosTraceSubscriber {
    
    /**
     * 返回插件名称；同名插件后加载者会覆盖先加载者。
     *
     * @return plugin name
     */
    String getName();
    
    /**
     * 追踪事件回调入口。
     *
     * @param event {@link TraceEvent}
     */
    void onEvent(TraceEvent event);
    
    /**
     * 返回本订阅者感兴趣的事件类型列表。
     *
     * @return The interested event types.
     */
    List<Class<? extends TraceEvent>> subscribeTypes();
    
    /**
     * 返回事件回调使用的线程池；返回 {@code null} 表示由框架决定同步或异步执行。
     *
     * @return {@link Executor}
     */
    default Executor executor() {
        return null;
    }
}
