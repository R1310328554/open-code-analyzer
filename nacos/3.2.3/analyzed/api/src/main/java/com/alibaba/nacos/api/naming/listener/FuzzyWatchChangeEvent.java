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

/**
 * 模糊订阅（Fuzzy Watch）变更通知事件。
 *
 * <p>当模糊订阅匹配的服务发生新增、删除或同步状态变化时，客户端通过 {@link Event} 回调收到本事件，
 * 携带服务名、分组、命名空间及变更/同步类型等上下文信息。</p>
 *
 * @author tanyongquan
 */
public class FuzzyWatchChangeEvent implements Event {
    
    /** 发生变更的服务名。 */
    private String serviceName;
    
    /** 服务所属分组名。 */
    private String groupName;
    
    /** 服务所在命名空间 ID。 */
    private String namespace;
    
    /** 本地监听器视角的变更类型，如 ADD_SERVICE、DELETE_SERVICE。 */
    private String changeType;
    
    /** 触发本次变更的同步类型。 */
    private String syncType;
    
    /** 无参构造，供序列化或框架实例化使用。 */
    public FuzzyWatchChangeEvent() {
    }
    
    /**
     * 构造包含完整上下文的模糊订阅变更事件。
     *
     * @param serviceName 服务名
     * @param groupName   分组名
     * @param namespace   命名空间 ID
     * @param changeType  变更类型
     * @param syncType    同步类型
     */
    public FuzzyWatchChangeEvent(String serviceName, String groupName, String namespace,
        String changeType,
        String syncType) {
        this.changeType = changeType;
        this.serviceName = serviceName;
        this.groupName = groupName;
        this.namespace = namespace;
        this.syncType = syncType;
    }
    
    /** 获取发生变更的服务名。 */
    public String getServiceName() {
        return serviceName;
    }
    
    /** 获取服务所属分组名。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 获取服务所在命名空间 ID。 */
    public String getNamespace() {
        return namespace;
    }
    
    /**
     * 获取本地监听器视角的变更类型，取值包含 {@code ADD_SERVICE}、{@code DELETE_SERVICE} 等，
     * 详见 {@code Constants.ServiceChangedType}。
     */
    public String getChangeType() {
        return changeType;
    }
    
    /**
     * 获取触发本次变更的同步类型，取值包含
     * {@code FUZZY_WATCH_INIT_NOTIFY}、{@code FUZZY_WATCH_RESOURCE_CHANGED}、
     * {@code FUZZY_WATCH_DIFF_SYNC_NOTIFY} 等。
     *
     * @return 触发本次变更的同步类型
     */
    public String getSyncType() {
        return syncType;
    }
    
    @Override
    public String toString() {
        return "FuzzyWatchChangeEvent{" + "serviceName='" + serviceName + '\'' + ", groupName='"
            + groupName + '\''
            + ", namespace='" + namespace + '\'' + ", changeType='" + changeType + '\''
            + ", syncType='" + syncType
            + '\'' + '}';
    }
}
