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
 * 模糊监听（Fuzzy Watch）事件观察者的抽象基类。
 *
 * <p>同时实现 {@link FuzzyWatchEventWatcher} 与 {@link FuzzyWatchLoadWatcher}，
 * 提供默认空实现的负载上限回调，子类只需关注 {@link FuzzyWatchEventWatcher#onEvent}。</p>
 *
 * @author stone-98
 * @date 2024/3/4
 */
public abstract class AbstractFuzzyWatchEventWatcher
    implements FuzzyWatchEventWatcher, FuzzyWatchLoadWatcher {
    
    /**
     * 获取执行回调的线程池。
     *
     * <p>默认返回 {@code null}，由 Nacos 客户端使用内置通知线程执行。</p>
     *
     * @return 自定义 {@link Executor}，或 {@code null} 使用默认线程
     */
    public Executor getExecutor() {
        return null;
    }
    
    @Override
    public void onPatternOverLimit() {
        // 默认空实现，子类可按需覆盖
    }
    
    @Override
    public void onConfigReachUpLimit() {
        // do nothing default
    }
}
