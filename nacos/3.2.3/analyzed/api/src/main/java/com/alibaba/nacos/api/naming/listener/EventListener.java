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

/**
 * 命名服务实例变更监听器。
 *
 * <p>通过 {@link NamingService#subscribe} 注册后，在实例上下线或健康状态变化时收到 {@link Event} 回调。</p>
 *
 * @author Nacos
 */
public interface EventListener {
    
    /**
     * 实例或服务变更时的回调入口。
     *
     * @param event 变更事件（通常为 {@code NamingEvent}）
     */
    void onEvent(Event event);
}
