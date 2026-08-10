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

package com.alibaba.nacos.api.config.listener;

import java.util.concurrent.Executor;

/**
 * 模糊监听配置变更的观察者接口。
 *
 * <p>注册模糊匹配模式后，当匹配的配置发生变更时回调 {@link #onEvent}。</p>
 *
 * @author stone-98
 * @date 2024/3/4
 */
public interface FuzzyWatchEventWatcher {
    
    /**
     * 模糊配置变更事件回调。
     *
     * @param event 变更事件详情
     */
    void onEvent(ConfigFuzzyWatchChangeEvent event);
    
    /**
     * 获取执行回调的线程池。
     *
     * @return 自定义 {@link Executor}，或 {@code null} 使用默认通知线程
     */
    Executor getExecutor();
    
}
