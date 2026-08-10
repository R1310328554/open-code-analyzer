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
 * 配置变更监听器接口。
 *
 * <p>客户端订阅指定 dataId/group 后，配置内容变化时回调 {@link #receiveConfigInfo}。</p>
 *
 * @author Nacos
 */
public interface Listener {
    
    /**
     * 获取执行回调的线程池。
     *
     * @return 自定义 {@link Executor}，或 {@code null} 使用默认通知线程
     */
    Executor getExecutor();
    
    /**
     * 接收最新配置内容。
     *
     * @param configInfo 变更后的完整配置文本
     */
    void receiveConfigInfo(final String configInfo);
}
