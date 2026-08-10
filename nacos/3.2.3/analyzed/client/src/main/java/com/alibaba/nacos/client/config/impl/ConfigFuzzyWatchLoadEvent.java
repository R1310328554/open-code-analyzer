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

package com.alibaba.nacos.client.config.impl;

import com.alibaba.nacos.common.notify.Event;

/**
 * 模糊监听负载/限流事件。
 *
 * <p>当模糊模式数量或匹配配置数超过服务端限制而被抑制时发布，供 {@link FuzzyWatchLoadWatcher} 感知并降级处理。</p>
 *
 * @author shiyiyue
 * @date 2025/01/13
 */
public class ConfigFuzzyWatchLoadEvent extends Event {
    
    /** 客户端实例 UUID。 */
    private String clientUuid;
    
    /** 触发限流的 groupKey 模糊匹配模式。 */
    /** The groupKeyPattern of configuration. */
    /** 配置的 groupKey 模式。 */
    private String groupKeyPattern;
    
    /** 限流/抑制错误码。 */
    private int code;
    
    /**
     * Constructs a new ConfigFuzzyWatchLoadEvent.
      * <p>Nacos 客户端配置实现模块；详见上方说明。</p>
     */
    public ConfigFuzzyWatchLoadEvent() {
    }
    
    /**
     * Constructs a new FuzzyListenNotifyEvent with the specified group, dataId, and type.
     *
     * @param code            The type of notification.
     * @param groupKeyPattern The groupKeyPattern of notification.
      * <p>Nacos 客户端配置实现模块；详见上方说明。</p>
     */
    private ConfigFuzzyWatchLoadEvent(int code, String groupKeyPattern, String clientUuid) {
        this.code = code;
        this.groupKeyPattern = groupKeyPattern;
        this.clientUuid = clientUuid;
    }
    
    /**
     * Builds a new FuzzyListenNotifyEvent with the specified group, dataId, and type.
     *
     * @param groupKeyPattern The groupKey of the configuration.
     * @return A new FuzzyListenNotifyEvent instance.
      * <p>Nacos 客户端配置实现模块；详见上方说明。</p>
     */
    public static ConfigFuzzyWatchLoadEvent buildEvent(int code, String groupKeyPattern,
        String clientUuid) {
        return new ConfigFuzzyWatchLoadEvent(code, groupKeyPattern, clientUuid);
    }
    
    public String getClientUuid() {
        return clientUuid;
    }
    
    public String getGroupKeyPattern() {
        return groupKeyPattern;
    }
    
    public int getCode() {
        return code;
    }
}
