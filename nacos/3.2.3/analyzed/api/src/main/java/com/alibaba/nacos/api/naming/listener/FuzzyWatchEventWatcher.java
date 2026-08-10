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

package com.alibaba.nacos.api.naming.listener;

import java.util.concurrent.Executor;

/**
 * 模糊订阅事件监听器接口，支持使用自定义线程池异步处理事件。
 *
 * <p>实现类可指定 {@link #getExecutor()} 控制回调线程；返回 {@code null} 时由 Nacos 内部通知器执行。</p>
 *
 * @author tanyongquan
 */
public interface FuzzyWatchEventWatcher {
    
    /**
     * 获取用于通知事件的执行器；为 {@code null} 时使用 Nacos 内部默认通知器。
     *
     * @return 事件通知所用执行器
     */
    Executor getExecutor();
    
    /**
     * 处理模糊订阅变更事件 {@link FuzzyWatchChangeEvent}。
     *
     * @param event 模糊订阅变更事件
     */
    void onEvent(FuzzyWatchChangeEvent event);
    
}
