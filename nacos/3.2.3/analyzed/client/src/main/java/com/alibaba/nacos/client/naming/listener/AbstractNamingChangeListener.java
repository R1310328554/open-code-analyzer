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

package com.alibaba.nacos.client.naming.listener;

import com.alibaba.nacos.api.naming.listener.AbstractEventListener;
import com.alibaba.nacos.api.naming.listener.Event;

/**
 * 命名变更事件监听器抽象基类。
 *
 * <p>过滤 {@link NamingChangeEvent} 并回调 {@link #onChange(NamingChangeEvent)}，简化用户实现实例增删改感知逻辑。</p>
 *
 * @author lideyou
 */
public abstract class AbstractNamingChangeListener extends AbstractEventListener {
    
    /** 仅将 NamingChangeEvent 转发至 onChange。 */
    @Override
    public final void onEvent(Event event) {
        if (event instanceof NamingChangeEvent) {
            onChange((NamingChangeEvent) event);
        }
    }
    
    /**
     * 实例列表发生变更时的回调。
     *
     * @param event 命名变更事件
     */
    public abstract void onChange(NamingChangeEvent event);
}
