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
 *
 */

package com.alibaba.nacos.api.ai.model.a2a;

import java.util.Objects;

/**
 * A2A Agent 接口描述，定义 Agent 对外暴露的访问 URL 与协议绑定信息。
 *
 * <p>A2A 1.0.0 起以 {@link #protocolBinding} 替代旧版 {@link #transport} 字段；
 * 可出现在 {@link AgentCard#getSupportedInterfaces()} 列表中。</p>
 *
 * @author KiteSoar
 */
public class AgentInterface {
    
    /**
     * Agent 接口完整 URL（A2A 1.0.0 新增）。
     *
     * @since 3.2.1
     */
    private String url;
    
    /**
     * 旧版传输协议字段，A2A 1.0.0 请改用 {@link #protocolBinding}。
     *
     * @deprecated For old A2A protocol compatibility only.
     */
    @Deprecated
    private String transport;
    
    /**
     * 协议绑定标识，如 JSONRPC、HTTP 等（A2A 1.0.0 新增）。
     *
     * @since 3.2.1
     */
    private String protocolBinding;
    
    /**
     * A2A 协议版本号（1.0.0 新增）。
     *
     * @since 3.2.1
     */
    private String protocolVersion;
    
    /**
     * 租户标识（A2A 1.0.0 新增）。
     *
     * @since 3.2.1
     */
    private String tenant;
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getTransport() {
        return transport;
    }
    
    public void setTransport(String transport) {
        this.transport = transport;
    }
    
    public String getProtocolBinding() {
        return protocolBinding;
    }
    
    public void setProtocolBinding(String protocolBinding) {
        this.protocolBinding = protocolBinding;
    }
    
    public String getProtocolVersion() {
        return protocolVersion;
    }
    
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    public String getTenant() {
        return tenant;
    }
    
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentInterface that = (AgentInterface) o;
        return Objects.equals(url, that.url) && Objects.equals(transport, that.transport)
            && Objects.equals(
                protocolBinding, that.protocolBinding)
            && Objects.equals(protocolVersion, that.protocolVersion)
            && Objects.equals(tenant, that.tenant);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(url, transport, protocolBinding, protocolVersion, tenant);
    }
}
