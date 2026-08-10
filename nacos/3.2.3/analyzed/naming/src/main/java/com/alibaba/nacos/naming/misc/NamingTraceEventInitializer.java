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

package com.alibaba.nacos.naming.misc;

import com.alibaba.nacos.common.trace.event.naming.NamingTraceEvent;
import com.alibaba.nacos.core.trace.NacosCombinedTraceSubscriber;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 命名链路追踪事件初始化器。
 *
 * <p>Spring 容器启动后注册 {@link NamingTraceEvent} 的 {@link NacosCombinedTraceSubscriber}，使命名操作可接入统一追踪体系。</p>
 *
 * @author xiweng.yy
 */
@Component
public class NamingTraceEventInitializer {
    
    /** 容器就绪后订阅命名追踪事件类型。 */
    @PostConstruct
    public void registerSubscriberForNamingEvent() {
        new NacosCombinedTraceSubscriber(NamingTraceEvent.class);
    }
}
