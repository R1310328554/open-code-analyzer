/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.model.response;

import java.util.Map;

/**
 * Nacos 客户端连接信息。
 *
 * <p>汇总连接追踪状态、客户端能力表及 {@link ConnectionMetaInfo} 元数据，供运维与诊断接口返回。</p>
 *
 * @author Nacos
 */
public class ConnectionInfo {
    
    /** 是否已启用连接追踪。 */
    private boolean traced = false;
    
    /** 客户端能力协商表（能力名 → 是否支持）。 */
    private Map<String, Boolean> abilityTable;
    
    /** 连接元数据详情。 */
    private ConnectionMetaInfo metaInfo;
    
    /** 是否已启用连接追踪。 */
    public boolean isTraced() {
        return traced;
    }
    
    /** 设置连接追踪开关。 */
    public void setTraced(boolean traced) {
        this.traced = traced;
    }
    
    /** 设置客户端能力表。 */
    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }
    
    /** 获取客户端能力表。 */
    public Map<String, Boolean> getAbilityTable() {
        return this.abilityTable;
    }
    
    /** 获取连接元数据。 */
    public ConnectionMetaInfo getMetaInfo() {
        return metaInfo;
    }
    
    /** 设置连接元数据。 */
    public void setMetaInfo(ConnectionMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }
}
