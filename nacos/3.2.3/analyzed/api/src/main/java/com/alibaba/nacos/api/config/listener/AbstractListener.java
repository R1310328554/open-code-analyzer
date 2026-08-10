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

package com.alibaba.nacos.api.config.listener;

import java.util.concurrent.Executor;

/**
 * 配置监听器适配器，使用默认通知线程。
 *
 * <p>继承 {@link Listener} 并固定 {@link #getExecutor()} 返回 {@code null}，
 * 子类只需实现 {@link Listener#receiveConfigInfo} 即可接收配置变更。</p>
 *
 * @author water.lyl
 */
public abstract class AbstractListener implements Listener {
    
    /** 使用 Nacos 客户端默认通知线程执行回调。 */
    @Override
    public Executor getExecutor() {
        return null;
    }
    
}
