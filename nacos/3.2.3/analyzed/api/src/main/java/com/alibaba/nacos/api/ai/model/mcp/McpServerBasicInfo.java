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

package com.alibaba.nacos.api.ai.model.mcp;

import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.ai.model.mcp.registry.Icon;
import com.alibaba.nacos.api.ai.model.mcp.registry.Package;
import com.alibaba.nacos.api.ai.model.mcp.registry.Repository;
import com.alibaba.nacos.api.ai.model.mcp.registry.ServerVersionDetail;

import java.util.List;
import java.util.Map;

/**
 * Nacos 中 MCP Server 基础信息模型，承载名称、协议、版本与能力等核心元数据。
 *
 * <p>作为 {@link McpServerDetailInfo}、{@link McpServerVersionInfo} 的父类，
 * 包含创建/更新 Server 所需的公共字段，不含端点与工具/资源规范详情。</p>
 *
 * @author xiweng.yy
 */
public class McpServerBasicInfo {
    
    /** 所属命名空间 ID。 */
    private String namespaceId;
    
    /** MCP Server 唯一标识。 */
    private String id;
    
    /** MCP Server 显示名称。 */
    private String name;
    
    /**
     * 后端通信协议，应为 {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}、{@link AiConstants.Mcp#MCP_PROTOCOL_SSE}、
     * {@link AiConstants.Mcp#MCP_PROTOCOL_STREAMABLE}、{@link AiConstants.Mcp#MCP_PROTOCOL_HTTP} 或 {@link AiConstants.Mcp#MCP_PROTOCOL_DUBBO} 之一。
     */
    private String protocol;
    
    /** 面向客户端的前端协议。 */
    private String frontProtocol;
    
    /** MCP Server 功能描述。 */
    private String description;
    
    /** 源码仓库信息。 */
    private Repository repository;
    
    /** 安装包列表。 */
    private List<Package> packages;
    
    /** 图标列表。 */
    private List<Icon> icons;
    
    /** 官方网站 URL。 */
    private String websiteUrl;
    
    /** 当前版本明细（推荐使用，替代 {@link #version}）。 */
    private ServerVersionDetail versionDetail;
    
    /** 版本号字符串，请优先使用 {@link #versionDetail}。 */
    private String version;
    
    /** 远程服务配置，协议非 {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO} 时需设置。 */
    private McpServerRemoteServiceConfig remoteServerConfig;
    
    /** 本地进程配置，协议为 {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO} 时需设置。 */
    private Map<String, Object> localServerConfig;
    
    /** 是否启用，默认 {@code true}。 */
    private boolean enabled = true;
    
    /**
     * MCP Server 生命周期状态，应为 {@link AiConstants.Mcp#MCP_STATUS_ACTIVE} 或
     * {@link AiConstants.Mcp#MCP_STATUS_DEPRECATED}，默认 {@link AiConstants.Mcp#MCP_STATUS_ACTIVE}。
     */
    private String status = AiConstants.Mcp.MCP_STATUS_ACTIVE;
    
    /** Nacos 自动探测的能力列表，创建/更新时无需手动设置。 */
    private List<McpCapability> capabilities;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public McpServerRemoteServiceConfig getRemoteServerConfig() {
        return remoteServerConfig;
    }
    
    public void setRemoteServerConfig(McpServerRemoteServiceConfig remoteServerConfig) {
        this.remoteServerConfig = remoteServerConfig;
    }
    
    public Map<String, Object> getLocalServerConfig() {
        return localServerConfig;
    }
    
    public void setLocalServerConfig(Map<String, Object> localServerConfig) {
        this.localServerConfig = localServerConfig;
    }
    
    public String getFrontProtocol() {
        return frontProtocol;
    }
    
    public void setFrontProtocol(String frontProtocol) {
        this.frontProtocol = frontProtocol;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public List<McpCapability> getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(List<McpCapability> capabilities) {
        this.capabilities = capabilities;
    }
    
    public ServerVersionDetail getVersionDetail() {
        return versionDetail;
    }
    
    public void setVersionDetail(ServerVersionDetail versionDetail) {
        this.versionDetail = versionDetail;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Repository getRepository() {
        return repository;
    }
    
    public void setRepository(Repository repository) {
        this.repository = repository;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public List<Package> getPackages() {
        return packages;
    }
    
    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public List<Icon> getIcons() {
        return icons;
    }
    
    public void setIcons(List<Icon> icons) {
        this.icons = icons;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
}
