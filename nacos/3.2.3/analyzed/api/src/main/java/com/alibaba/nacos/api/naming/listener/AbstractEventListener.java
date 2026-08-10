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

package com.alibaba.nacos.api.naming.listener;

import java.util.concurrent.Executor;

/**
 * 命名事件监听器抽象基类。
 *
 * <p>子类可覆写 {@link #getExecutor()} 指定回调线程池，避免在 Nacos 内部线程上执行耗时逻辑。</p>
 *
 * @author horizonzy
 * @since 1.4.1
 */
public abstract class AbstractEventListener implements EventListener {
    
    /**
     * 获取处理 {@link EventListener#onEvent(Event)} 回调的线程池。
     *
     * @return 自定义 {@link Executor}；默认 {@code null} 表示在通知线程同步执行
     */
    public Executor getExecutor() {
        return null;
    }
    
}
