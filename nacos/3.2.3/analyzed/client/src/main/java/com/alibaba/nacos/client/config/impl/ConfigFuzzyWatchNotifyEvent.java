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
 * 模糊监听变更通知事件。
 *
 * <p>继承 {@link Event}，可异步分发。携带 groupKey、变更类型、同步类型、模式及客户端/监听器 UUID，供 {@link ConfigFuzzyWatchGroupKeyHolder} 内部处理。</p>
 *
 * @author stone-98
 * @date 2024/3/4
 */
public class ConfigFuzzyWatchNotifyEvent extends Event {
    
    private String clientUuid;
    
    /** 触发本次通知的监听器 UUID。 */
    /** The uuid of this watcher for which that this notify event . */
    /** 监听器 UUID。 */
    private String watcherUuid;
    
    /**
     * The groupKeyPattern of configuration.
      * <p>Nacos 客户端配置实现模块；详见上方说明。</p>
     */
    private String groupKeyPattern;
    
    /** 发生变更的具体 groupKey（dataId+group+tenant）。 */
    private String groupKey;
    
    /**
     * The type of notification (e.g., ADD_CONFIG, DELETE_CONFIG).
      * <p>Nacos 客户端配置实现模块；详见上方说明。</p>
     */
    private String changedType;
    
    /** 同步类型（如初始化、差异对账、资源变更）。 */
    private String syncType;
    
    /**
     * Constructs a new FuzzyListenNotifyEvent.
      * <p>Nacos 客户端配置实现模块；详见上方说明。</p>
     */
    public ConfigFuzzyWatchNotifyEvent() {
    }
    
    /**
     * Constructs a new FuzzyListenNotifyEvent with the specified group, dataId, and type.
     *
     * @param groupKey    The groupKey of the configuration.
     * @param changedType The type of notification.
      * <p>Nacos 客户端配置实现模块；详见上方说明。</p>
     */
    private ConfigFuzzyWatchNotifyEvent(String groupKey, String changedType, String syncType,
        String groupKeyPattern,
        String clientUuid, String watcherUuid) {
        this.groupKey = groupKey;
        this.syncType = syncType;
        this.changedType = changedType;
        this.groupKeyPattern = groupKeyPattern;
        this.clientUuid = clientUuid;
        this.watcherUuid = watcherUuid;
    }
    
    /**
     * Builds a new FuzzyListenNotifyEvent with the specified group, dataId, and type.
     *
     * @param groupKey The groupKey of the configuration.
     * @return A new FuzzyListenNotifyEvent instance.
      * <p>Nacos 客户端配置实现模块；详见上方说明。</p>
     */
    public static ConfigFuzzyWatchNotifyEvent buildEvent(String groupKey,
        String groupKeyPattern, String changedType, String syncType, String clientUuid) {
        return buildEvent(groupKey, groupKeyPattern, changedType, syncType, clientUuid, null);
    }
    
    /**
     * Builds a new FuzzyListenNotifyEvent with the specified group, dataId, and type.
     *
     * @param groupKey The groupKey of the configuration.
     * @return A new FuzzyListenNotifyEvent instance.
      * <p>Nacos 客户端配置实现模块；详见上方说明。</p>
     */
    public static ConfigFuzzyWatchNotifyEvent buildEvent(String groupKey,
        String groupKeyPattern, String changedType, String syncType, String clientUuid,
        String watcherUuid) {
        ConfigFuzzyWatchNotifyEvent configFuzzyWatchNotifyEvent =
            new ConfigFuzzyWatchNotifyEvent(groupKey, changedType,
                syncType, groupKeyPattern, clientUuid, watcherUuid);
        return configFuzzyWatchNotifyEvent;
    }
    
    /**
     * Gets the UUID (Unique Identifier) of the listener.
     *
     * @return The UUID of the listener.
      * <p>Nacos 客户端配置实现模块；详见上方说明。</p>
     */
    public String getWatcherUuid() {
        return watcherUuid;
    }
    
    public String getClientUuid() {
        return clientUuid;
    }
    
    public String getGroupKeyPattern() {
        return groupKeyPattern;
    }
    
    public String getGroupKey() {
        return groupKey;
    }
    
    public String getSyncType() {
        return syncType;
    }
    
    /**
     * Gets the type of notification.
     *
     * @return The type of notification.
      * <p>Nacos 客户端配置实现模块；详见上方说明。</p>
     */
    public String getChangedType() {
        return changedType;
    }
}
