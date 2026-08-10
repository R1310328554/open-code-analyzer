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

import com.alibaba.nacos.api.common.Constants;

/**
 * 模糊监听配置变更事件。
 *
 * <p>当匹配模糊监听模式的配置发生新增或删除时，由服务端推送至
 * {@link FuzzyWatchEventWatcher#onEvent} 回调。</p>
 *
 * @author stone-98
 * @date 2024/3/12
 */
public class ConfigFuzzyWatchChangeEvent {
    
    /** 发生变更的配置 group。 */
    private String group;
    
    /** 发生变更的配置 dataId。 */
    private String dataId;
    
    /** 发生变更的配置命名空间。 */
    private String namespace;
    
    /** 本地监听器侧的变更类型，如 {@code ADD_CONFIG}、{@code DELETE_CONFIG}；参见 {@link Constants.ConfigChangedType}。 */
    private String changedType;
    
    /** 触发本次变更的同步类型，如 {@code FUZZY_WATCH_INIT_NOTIFY}、{@code FUZZY_WATCH_RESOURCE_CHANGED}、{@code FUZZY_WATCH_DIFF_SYNC_NOTIFY}。 */
    private String syncType;
    
    /**
     * 私有构造器，请使用 {@link #build} 创建实例。
     *
     * @param namespace   命名空间
     * @param group       配置 group
     * @param dataId      配置 dataId
     * @param changedType 变更类型
     * @param syncType    同步类型
     */
    private ConfigFuzzyWatchChangeEvent(String namespace, String group, String dataId,
        String changedType,
        String syncType) {
        this.group = group;
        this.dataId = dataId;
        this.namespace = namespace;
        this.changedType = changedType;
        this.syncType = syncType;
    }
    
    /**
     * 构建模糊监听变更事件。
     *
     * @param namespace   命名空间
     * @param group       配置 group
     * @param dataId      配置 dataId
     * @param changedType 变更类型
     * @param syncType    同步类型
     * @return 新的事件实例
     */
    public static ConfigFuzzyWatchChangeEvent build(String namespace, String group, String dataId,
        String changedType,
        String syncType) {
        return new ConfigFuzzyWatchChangeEvent(namespace, group, dataId, changedType, syncType);
    }
    
    /** 获取命名空间。 */
    public String getNamespace() {
        return namespace;
    }
    
    /** 获取配置 group。 */
    public String getGroup() {
        return group;
    }
    
    /** 获取配置 dataId。 */
    public String getDataId() {
        return dataId;
    }
    
    /** 获取变更类型。 */
    public String getChangedType() {
        return changedType;
    }
    
    /** 获取触发本次变更的同步类型。 */
    public String getSyncType() {
        return syncType;
    }
    
    @Override
    public String toString() {
        return "ConfigFuzzyWatchChangeEvent{" + "group='" + group + '\'' + ", dataId='" + dataId
            + '\''
            + ", namespace='" + namespace + '\'' + ", changedType='" + changedType + '\''
            + ", syncType='"
            + syncType + '\'' + '}';
    }
}
