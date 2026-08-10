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
 * 模糊监听（Fuzzy Watch）事件观察者抽象基类。
 *
 * <p>同时实现 {@link FuzzyWatchEventWatcher} 与 {@link FuzzyWatchLoadWatcher}，提供默认空实现的限流回调，子类可覆写 {@link #getExecutor()} 指定执行器。</p>
 *
 * @author tanyongquan
 */
public abstract class AbstractFuzzyWatchEventWatcher
    implements FuzzyWatchEventWatcher, FuzzyWatchLoadWatcher {
    
    /** 默认在通知线程同步处理事件。 */
    @Override
    public Executor getExecutor() {
        return null;
    }
    
    /** 监听模式数量超限时回调（默认无操作）。 */
    @Override
    public void onPatternOverLimit() {
        // 默认不做处理
    }
    
    /** 匹配服务数达到上限时回调（默认无操作）。 */
    @Override
    public void onServiceReachUpLimit() {
        // 默认不做处理
    }
}
