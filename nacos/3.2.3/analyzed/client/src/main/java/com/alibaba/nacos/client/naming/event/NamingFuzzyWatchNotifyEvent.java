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

package com.alibaba.nacos.client.naming.event;

import com.alibaba.nacos.common.notify.Event;

/**
 * 命名模糊监听变更通知事件。
 *
 * <p>服务端推送单条 serviceKey 的增删改或初始同步信息，由 {@link NamingFuzzyWatchServiceListHolder} 消费并更新 {@link NamingFuzzyWatchContext}。</p>
 *
 * @author tanyongquan
 */
public class NamingFuzzyWatchNotifyEvent extends Event {
    
    /** 事件作用域。 */
    private final String scope;
    
    /** 目标监听器 UUID，null 表示广播全部。 */
    private String watcherUuid;
    
    /** 变更的服务键。 */
    private String serviceKey;
    
    /** 匹配的模糊模式。 */
    private String pattern;
    
    /** 变更类型（如 ADD/DELETE/MODIFY）。 */
    private final String changedType;
    
    /** 同步类型（增量或全量对账）。 */
    private final String syncType;
    
    private NamingFuzzyWatchNotifyEvent(String scope, String pattern, String serviceKey,
        String changedType,
        String syncType, String watcherUuid) {
        this.scope = scope;
        this.pattern = pattern;
        this.serviceKey = serviceKey;
        this.changedType = changedType;
        this.syncType = syncType;
        this.watcherUuid = watcherUuid;
    }
    
    /** 构建广播型模糊监听通知（不指定 watcherUuid）。 */
    public static NamingFuzzyWatchNotifyEvent build(String eventScope, String pattern,
        String serviceKey,
        String changedType, String syncType) {
        return new NamingFuzzyWatchNotifyEvent(eventScope, pattern, serviceKey, changedType,
            syncType, null);
    }
    
    /** 构建可定向到单个监听器的模糊监听通知。 */
    public static NamingFuzzyWatchNotifyEvent build(String eventScope, String pattern,
        String serviceKey,
        String changedType, String syncType, String watcherUuid) {
        return new NamingFuzzyWatchNotifyEvent(eventScope, pattern, serviceKey, changedType,
            syncType, watcherUuid);
    }
    
    /** 获取模糊匹配模式。 */
    public String getPattern() {
        return pattern;
    }
    
    /** 获取变更类型。 */
    public String getChangedType() {
        return changedType;
    }
    
    /** 返回事件作用域。 */
    @Override
    public String scope() {
        return this.scope;
    }
    
    /** 获取目标监听器 UUID。 */
    public String getWatcherUuid() {
        return watcherUuid;
    }
    
    /** 获取变更的服务键。 */
    public String getServiceKey() {
        return serviceKey;
    }
    
    /** 获取作用域（同 scope()）。 */
    public String getScope() {
        return scope;
    }
    
    /** 获取同步类型。 */
    public String getSyncType() {
        return syncType;
    }
}
