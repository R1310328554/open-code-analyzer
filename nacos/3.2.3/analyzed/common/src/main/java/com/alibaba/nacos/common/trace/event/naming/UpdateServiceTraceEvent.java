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

package com.alibaba.nacos.common.trace.event.naming;

import java.util.Map;

/**
 * 命名服务「更新服务」链路追踪事件：记录服务级元数据变更，
 * 继承 {@link NamingTraceEvent}，携带 namespace、group、serviceName 及新 metadata。
 * Naming update service trace event.
 *
 * @author stone-98
 * @date 2023/8/31
 */
public class UpdateServiceTraceEvent extends NamingTraceEvent {
    
    private static final long serialVersionUID = -6792054530665003857L;
    
    /** 服务更新后的元数据键值对 */
    private final Map<String, String> metadata;
    
    /** 返回服务元数据 */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /**
     * 构造更新服务追踪事件。
     *
     * @param eventTime         事件发生时间戳
     * @param serviceNamespace  服务命名空间
     * @param serviceGroup      服务分组
     * @param serviceName       服务名
     * @param metadata          更新后的元数据
     */
    public UpdateServiceTraceEvent(long eventTime, String serviceNamespace, String serviceGroup,
        String serviceName,
        Map<String, String> metadata) {
        super("UPDATE_SERVICE_TRACE_EVENT", eventTime, serviceNamespace, serviceGroup, serviceName);
        this.metadata = metadata;
    }
}
