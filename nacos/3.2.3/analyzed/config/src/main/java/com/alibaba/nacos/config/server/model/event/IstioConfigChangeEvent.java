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

package com.alibaba.nacos.config.server.model.event;

/**
 * Istio/XDS 配置变更事件：继承 {@link ConfigDataChangeEvent}，
 * 额外携带配置正文与类型，供 Mesh 控制面推送 xDS 资源。
 * XDS config change event.
 *
 * @author PoisonGravity
 */
public class IstioConfigChangeEvent extends ConfigDataChangeEvent {
    
    private static final long serialVersionUID = -2618455009648617192L;
    
    /** Istio 相关配置正文（如 VirtualService、DestinationRule） */
    public final String content;
    
    /** 配置资源类型标识 */
    public final String type;
    
    /**
     * 构造 Istio 配置变更事件。
     *
     * @param dataId       配置 dataId
     * @param group        配置 group
     * @param tenant       命名空间 ID
     * @param gmtModified  最后修改时间戳
     * @param content      配置正文
     * @param type         资源类型
     */
    public IstioConfigChangeEvent(String dataId, String group, String tenant, long gmtModified,
        String content,
        String type) {
        super(dataId, group, tenant, gmtModified);
        this.content = content;
        this.type = type;
    }
    
}
