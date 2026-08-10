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

package com.alibaba.nacos.api.ai.listener;

import java.util.concurrent.Executor;

/**
 * Nacos AI 模块通用事件监听器接口。
 *
 * <p>订阅 Prompt、Skill、MCP 服务器或 Agent 等资源变更时实现此接口；
 * 可通过 {@link #getExecutor()} 指定回调线程池，返回 null 则使用客户端默认执行器。</p>
 *
 * @author Nacos
 */
public interface NacosAiListener<E extends NacosAiEvent> {
    
    /**
     * 收到 AI 模块变更事件时的回调方法。
     *
     * @param event event
     */
    void onEvent(E event);
    
    /**
     * 获取执行本监听器回调的线程池；返回 null 时使用客户端默认执行器。
     *
     * @return Executor
     */
    default Executor getExecutor() {
        return null;
    }
}
