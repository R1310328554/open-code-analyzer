/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.remote.request;

import java.util.HashMap;
import java.util.Map;

/**
 * gRPC 连接建立握手请求。
 *
 * <p>客户端在连接建立后首包发送，携带 {@link #clientVersion}、{@link #tenant}、{@link #labels} 与 {@link #abilityTable}，供服务端识别客户端身份与能力。</p>
 *
 * @author liuzunfei
 * @version $Id: ConnectionSetupRequest.java, v 0.1 2020年08月06日 2:42 PM liuzunfei Exp $
 */
public class ConnectionSetupRequest extends InternalRequest {
    
    /** 客户端 SDK 版本号。 */
    private String clientVersion;
    
    /** 租户/命名空间标识。 */
    private String tenant;
    
    /** 连接元数据标签（来源、模块等，见 {@link RemoteConstants}）。 */
    private Map<String, String> labels = new HashMap<>();
    
    /** 客户端能力表（特性名 → 是否支持）。 */
    private Map<String, Boolean> abilityTable;
    
    /** 无参构造，供序列化框架使用。 */
    public ConnectionSetupRequest() {
    }
    
    /** 返回客户端版本号。 */
    public String getClientVersion() {
        return clientVersion;
    }
    
    /** 设置客户端版本号。 */
    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }
    
    /** 返回连接标签映射。 */
    public Map<String, String> getLabels() {
        return labels;
    }
    
    /** 设置连接标签映射。 */
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
    
    /** 返回租户/命名空间标识。 */
    public String getTenant() {
        return tenant;
    }
    
    /** 设置租户/命名空间标识。 */
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
    /** 返回客户端能力表。 */
    public Map<String, Boolean> getAbilityTable() {
        return abilityTable;
    }
    
    /** 设置客户端能力表。 */
    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }
}
