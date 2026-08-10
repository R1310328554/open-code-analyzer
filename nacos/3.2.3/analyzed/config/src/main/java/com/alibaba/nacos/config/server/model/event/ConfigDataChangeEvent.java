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

package com.alibaba.nacos.config.server.model.event;

import com.alibaba.nacos.common.notify.Event;

/**
 * 配置数据变更事件：当持久层配置发生增删改时发布，
 * 携带 dataId、group、tenant 及最后修改时间，供监听推送链路消费。
 * ConfigDataChangeEvent.
 *
 * @author Nacos
 */
public class ConfigDataChangeEvent extends Event {
    
    /** 变更配置的 dataId */
    public String dataId;
    
    /** 变更配置的 group */
    public String group;
    
    /** 变更配置所属命名空间（tenant） */
    public String tenant;
    
    /** 灰度配置名称，非灰度变更时为 null */
    public String grayName;
    
    /** 配置最后修改时间戳（毫秒） */
    public final long lastModifiedTs;
    
    /**
     * 构造标准配置变更事件。
     *
     * @param dataId       配置 dataId，不可为 null
     * @param group        配置 group，不可为 null
     * @param tenant       命名空间 ID
     * @param gmtModified  最后修改时间戳
     */
    public ConfigDataChangeEvent(String dataId, String group, String tenant, long gmtModified) {
        if (null == dataId || null == group) {
            throw new IllegalArgumentException("dataId is null or group is null");
        }
        this.dataId = dataId;
        this.group = group;
        this.tenant = tenant;
        this.lastModifiedTs = gmtModified;
    }
    
    /**
     * 构造带灰度名称的配置变更事件。
     *
     * @param dataId       配置 dataId
     * @param group        配置 group
     * @param tenant       命名空间 ID
     * @param grayName     灰度配置名称
     * @param gmtModified  最后修改时间戳
     */
    public ConfigDataChangeEvent(String dataId, String group, String tenant, String grayName,
        long gmtModified) {
        this(dataId, group, tenant, gmtModified);
        this.grayName = grayName;
    }
    
}
