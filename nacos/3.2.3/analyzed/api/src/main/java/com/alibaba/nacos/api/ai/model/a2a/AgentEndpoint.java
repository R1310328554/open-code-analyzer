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

package com.alibaba.nacos.api.ai.model.a2a;

import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.utils.StringUtils;

import java.util.Objects;

/**
 * A2A 协议 Agent 端点模型，将 URL 拆分为地址、端口、路径等独立字段。
 *
 * <p>是 {@link AgentInterface} 的细粒度版本，便于 Nacos 按字段存储与校验
 * 端点配置，再组装为完整访问 URL。</p>
 *
 * @author xiweng.yy
 */
public class AgentEndpoint {
    
    /**
     * 传输层协议，与 {@link AgentInterface#transport} 含义相同，默认 `JSONRPC`。
     */
    private String transport = AiConstants.A2a.A2A_ENDPOINT_DEFAULT_TRANSPORT;
    
    /**
     * 主机地址（IP 或域名），与 {@link #port}、{@link #path}、{@link #protocol} 拼接成完整 URL。
     */
    private String address;
    
    /** 监听端口。 */
    private int port;
    
    /** URL 路径，默认为空字符串。 */
    private String path = StringUtils.EMPTY;
    
    /**
     * 是否启用 TLS；为 {@code true} 时对应 {@link AgentInterface} 应使用 `https`，否则为 `http`，默认 {@code false}。
     */
    private boolean supportTls;
    
    /** Agent 端点关联的版本号。 */
    private String version;
    
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
    
    /**
     * A2A 传输层自定义协议，默认 `HTTP`。
     *
     * @since 3.1.1
     */
    private String protocol = AiConstants.A2a.A2A_ENDPOINT_DEFAULT_PROTOCOL;
    
    /**
     * URL 查询参数字符串（A2A 自定义扩展）。
     *
     * @since 3.1.1
     */
    private String query;
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getTransport() {
        return transport;
    }
    
    public void setTransport(String transport) {
        this.transport = transport;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public boolean isSupportTls() {
        return supportTls;
    }
    
    public void setSupportTls(boolean supportTls) {
        this.supportTls = supportTls;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
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
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    /**
     * 仅比较地址与端口是否相同（忽略路径、TLS 等其它字段）。
     *
     * @param endpoint target endpoint
     * @return {@code true} if is equal, otherwise {@code false}
     */
    public boolean simpleEquals(AgentEndpoint endpoint) {
        return Objects.equals(address, endpoint.address) && Objects.equals(port, endpoint.port);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentEndpoint endpoint = (AgentEndpoint) o;
        return port == endpoint.port && supportTls == endpoint.supportTls
            && Objects.equals(transport,
                endpoint.transport)
            && Objects.equals(address, endpoint.address) && Objects.equals(path, endpoint.path)
            && Objects.equals(version, endpoint.version)
            && Objects.equals(protocol, endpoint.protocol)
            && Objects.equals(query, endpoint.query)
            && Objects.equals(protocolVersion, endpoint.protocolVersion)
            && Objects.equals(tenant, endpoint.tenant);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transport, address, port, path, supportTls, version, protocolVersion,
            tenant, protocol,
            query);
    }
    
    @Override
    public String toString() {
        return "AgentEndpoint{" + "transport='" + transport + '\'' + ", address='" + address + '\''
            + ", port=" + port
            + ", path='" + path + '\'' + ", supportTls=" + supportTls + ", version='" + version
            + '\''
            + ", protocolVersion='" + protocolVersion + '\'' + ", tenant='" + tenant + '\''
            + ", protocol='"
            + protocol + '\'' + ", query='" + query + '\'' + '}';
    }
}
