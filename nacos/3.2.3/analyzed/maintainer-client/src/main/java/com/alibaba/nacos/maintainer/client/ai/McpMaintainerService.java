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

package com.alibaba.nacos.maintainer.client.ai;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.ai.model.mcp.McpEndpointSpec;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerRemoteServiceConfig;
import com.alibaba.nacos.api.ai.model.mcp.McpToolSpecification;
import com.alibaba.nacos.api.ai.model.mcp.registry.ServerVersionDetail;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * MCP 维护服务接口：管理 Nacos 中 MCP Server 的注册、查询、更新与删除。
 *
 * <p>区分本地（stdio）与远程 MCP，提供多层默认方法简化命名空间与分页参数。</p>
 *
 * @author xiweng.yy
 */
public interface McpMaintainerService {
    
    /**
     * 列出 Nacos 中前 100 个 MCP Server。
     *
     * @return Fist 100 mcp server list.
     * @throws NacosException if fail to list mcp server
     */
    @Since("3.0.0")
    default Page<McpServerBasicInfo> listMcpServer() throws NacosException {
        return listMcpServer(1, 100);
    }
    
    /**
     * 分页列出 MCP Server。
     *
     * @param pageNo   the page number of mcp Servers
     * @param pageSize the size of each page
     * @return paged mcp Server list
     * @throws NacosException if fail to list mcp server
     */
    @Since("3.0.0")
    default Page<McpServerBasicInfo> listMcpServer(int pageNo, int pageSize) throws NacosException {
        return listMcpServer(StringUtils.EMPTY, pageNo, pageSize);
    }
    
    /**
     * List Mcp Servers in Nacos with page.
     *
     * @param mcpName  mcpName pattern, if empty string or null, will list all Mcp Servers.
     * @param pageNo   the page number of mcp Servers
     * @param pageSize the size of each page
     * @return paged mcp Server list
     * @throws NacosException if fail to list mcp server
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.0")
    default Page<McpServerBasicInfo> listMcpServer(String mcpName, int pageNo, int pageSize)
        throws NacosException {
        return listMcpServer(Constants.DEFAULT_NAMESPACE_ID, mcpName, pageNo, pageSize);
    }
    
    /**
     * List Mcp Servers in Nacos with page.
     *
     * @param namespaceId namespaceId
     * @param mcpName  mcpName pattern, if empty string or null, will list all Mcp Servers.
     * @param pageNo   the page number of mcp Servers
     * @param pageSize the size of each page
     * @return paged mcp Server list
     * @throws NacosException if fail to list mcp server
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.1")
    Page<McpServerBasicInfo> listMcpServer(String namespaceId, String mcpName, int pageNo,
        int pageSize) throws NacosException;
    
    /**
     * 按名称模糊搜索 MCP Server（默认前 100 条）。
     *
     * @param mcpName mcpName pattern, if empty string or null, will list all Mcp Servers.
     * @return First 100 mcp server list matched input mcpName pattern.
     * @throws NacosException if fail to search mcp server
     */
    @Since("3.0.0")
    default Page<McpServerBasicInfo> searchMcpServer(String mcpName) throws NacosException {
        return searchMcpServer(mcpName, 1, 100);
    }
    
    /**
     * Blur search first 100 Mcp Servers in Nacos with mcp name pattern.
     *
     * @param mcpName  mcpName pattern, if empty string or null, will list all Mcp Servers.
     * @param pageNo   the page number of mcp Servers
     * @param pageSize the size of each page
     * @return paged mcp Server list matched input mcpName pattern.
     * @throws NacosException if fail to search mcp server
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.0")
    default Page<McpServerBasicInfo> searchMcpServer(String mcpName, int pageNo, int pageSize)
        throws NacosException {
        return searchMcpServer(Constants.DEFAULT_NAMESPACE_ID, mcpName, pageNo, pageSize);
    }
    
    /**
     * Blur search first 100 Mcp Servers in Nacos with mcp name pattern.
     * 
     * @param namespaceId namespaceId
     * @param mcpName  mcpName pattern, if empty string or null, will list all Mcp Servers.
     * @param pageNo   the page number of mcp Servers
     * @param pageSize the size of each page
     * @return paged mcp Server list matched input mcpName pattern.
     * @throws NacosException if fail to search mcp server
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.1")
    Page<McpServerBasicInfo> searchMcpServer(String namespaceId, String mcpName, int pageNo,
        int pageSize) throws NacosException;
    
    /**
     * 从 Nacos 获取 MCP Server 详情。
     *
     * @param mcpName the mcp server name
     * @return detail information for this mcp server
     * @throws NacosException if fail to get mcp server
     */
    @Since("3.0.0")
    default McpServerDetailInfo getMcpServerDetail(String mcpName) throws NacosException {
        return getMcpServerDetail(mcpName, null);
    }
    
    /**
     * Get mcp server detail information from Nacos.
     *
     * @param mcpName the mcp server name
     * @param version the mcp server version
     * @return detail information for this mcp server
     * @throws NacosException if fail to get mcp server
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.1")
    default McpServerDetailInfo getMcpServerDetail(String mcpName, String version)
        throws NacosException {
        return getMcpServerDetail(Constants.DEFAULT_NAMESPACE_ID, mcpName, null, version);
    }
    
    /**
     * 按命名空间与版本获取 MCP Server 详情。
     *
     * @param namespaceId the namespace id
     * @param mcpName     the mcp name
     * @param version     the version
     * @return the mcp server detail
     * @throws NacosException the nacos exception
     */
    @Since("3.0.1")
    default McpServerDetailInfo getMcpServerDetail(String namespaceId, String mcpName,
        String version) throws NacosException {
        return getMcpServerDetail(namespaceId, mcpName, null, version);
    }
    
    /**
     * Get mcp server detail information from Nacos.
     *
     * @param namespaceId namespaceId
     * @param mcpName the mcp server name
     * @param mcpId the mcp server id
     * @param version the mcp server version
     * @return detail information for this mcp server
     * @throws NacosException if fail to get mcp server
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.2")
    McpServerDetailInfo getMcpServerDetail(String namespaceId, String mcpName, String mcpId,
        String version)
        throws NacosException;
    
    /**
     * 在 Nacos 创建本地（stdio）MCP Server。
     *
     * @param mcpName mcp server name of the new mcp server
     * @param version version of the new mcp server
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
     */
    @Since("3.0.0")
    default String createLocalMcpServer(String mcpName, String version) throws NacosException {
        return createLocalMcpServer(mcpName, version, null);
    }
    
    /**
     * Create new local mcp server to Nacos.
     *
     * @param mcpName     mcp server name of the new mcp server
     * @param version     version of the new mcp server
     * @param description description of the new mcp server
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.0")
    default String createLocalMcpServer(String mcpName, String version, String description)
        throws NacosException {
        return createLocalMcpServer(mcpName, version, description, null);
    }
    
    /**
     * Create new local mcp server to Nacos.
     *
     * @param mcpName     mcp server name of the new mcp server
     * @param version     version of the new mcp server
     * @param description description of the new mcp server
     * @param toolSpec    mcp server tools specification, see {@link McpToolSpecification}, nullable.
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.0")
    default String createLocalMcpServer(String mcpName, String version, String description,
        McpToolSpecification toolSpec) throws NacosException {
        return createLocalMcpServer(mcpName, version, description, null, toolSpec);
    }
    
    /**
     * Create new local mcp server to Nacos.
     *
     * @param mcpName           mcp server name of the new mcp server
     * @param version           version of the new mcp server
     * @param description       description of the new mcp server
     * @param localServerConfig custom config of the new mcp server
     * @param toolSpec          mcp server tools specification, see {@link McpToolSpecification}, nullable.
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.0")
    default String createLocalMcpServer(String mcpName, String version, String description,
        Map<String, Object> localServerConfig, McpToolSpecification toolSpec)
        throws NacosException {
        McpServerBasicInfo serverSpec = new McpServerBasicInfo();
        serverSpec.setName(mcpName);
        serverSpec.setProtocol(AiConstants.Mcp.MCP_PROTOCOL_STDIO);
        ServerVersionDetail versionDetail = new ServerVersionDetail();
        versionDetail.setVersion(version);
        serverSpec.setVersionDetail(versionDetail);
        serverSpec.setDescription(description);
        serverSpec.setLocalServerConfig(localServerConfig);
        return createLocalMcpServer(mcpName, serverSpec, toolSpec);
    }
    
    /**
     * Create new local mcp server to Nacos.
     *
     * @param mcpName    mcp server name of the new mcp server
     * @param serverSpec mcp server specification, see {@link McpServerBasicInfo} which `type` is
     *                   {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @param toolSpec   mcp server tools specification, see {@link McpToolSpecification}, nullable.
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.0")
    default String createLocalMcpServer(String mcpName, McpServerBasicInfo serverSpec,
        McpToolSpecification toolSpec)
        throws NacosException {
        if (Objects.isNull(serverSpec)) {
            throw new NacosException(NacosException.INVALID_PARAM,
                "Mcp server specification cannot be null.");
        }
        if (!AiConstants.Mcp.MCP_PROTOCOL_STDIO.equalsIgnoreCase(serverSpec.getProtocol())) {
            throw new NacosException(NacosException.INVALID_PARAM,
                String.format("Mcp server type must be `local`, input is `%s`",
                    serverSpec.getProtocol()));
        }
        return createMcpServer(mcpName, serverSpec, toolSpec, null);
    }
    
    /**
     * 在 Nacos 创建远程 MCP Server。
     *
     * @param mcpName      mcp server name of the new mcp server
     * @param version      version of the new mcp server
     * @param protocol     mcp protocol type not {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @param endpointSpec mcp server endpoint specification, see {@link McpEndpointSpec}, can't be null.
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
     */
    @Since("3.0.0")
    default String createRemoteMcpServer(String mcpName, String version, String protocol,
        McpEndpointSpec endpointSpec) throws NacosException {
        return createRemoteMcpServer(mcpName, version, protocol, new McpServerRemoteServiceConfig(),
            endpointSpec);
    }
    
    /**
     * Create new remote mcp server to Nacos.
     *
     * @param mcpName             mcp server name of the new mcp server
     * @param version             version of the new mcp server
     * @param protocol            mcp protocol type not {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @param remoteServiceConfig remote service configuration, see {@link McpServerRemoteServiceConfig}.
     * @param endpointSpec        mcp server endpoint specification, see {@link McpEndpointSpec}, can't be null.
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.0")
    default String createRemoteMcpServer(String mcpName, String version, String protocol,
        McpServerRemoteServiceConfig remoteServiceConfig, McpEndpointSpec endpointSpec)
        throws NacosException {
        return createRemoteMcpServer(mcpName, version, null, protocol, remoteServiceConfig,
            endpointSpec);
    }
    
    /**
     * Create new remote mcp server to Nacos.
     *
     * @param mcpName             mcp server name of the new mcp server
     * @param version             version of the new mcp server
     * @param description         description of the new mcp server
     * @param protocol            mcp protocol type not {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @param remoteServiceConfig remote service configuration, see {@link McpServerRemoteServiceConfig}.
     * @param endpointSpec        mcp server endpoint specification, see {@link McpEndpointSpec}, can't be null.
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.0")
    default String createRemoteMcpServer(String mcpName, String version, String description,
        String protocol,
        McpServerRemoteServiceConfig remoteServiceConfig, McpEndpointSpec endpointSpec)
        throws NacosException {
        return createRemoteMcpServer(mcpName, version, description, protocol, remoteServiceConfig,
            endpointSpec, null);
    }
    
    /**
     * Create new remote mcp server to Nacos.
     *
     * @param mcpName             mcp server name of the new mcp server
     * @param version             version of the new mcp server
     * @param description         description of the new mcp server
     * @param protocol            mcp protocol type not {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @param remoteServiceConfig remote service configuration, see {@link McpServerRemoteServiceConfig}.
     * @param endpointSpec        mcp server endpoint specification, see {@link McpEndpointSpec}, can't be null.
     * @param toolSpec            mcp server tools specification, see {@link McpToolSpecification}, nullable.
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.0")
    default String createRemoteMcpServer(String mcpName, String version, String description,
        String protocol,
        McpServerRemoteServiceConfig remoteServiceConfig, McpEndpointSpec endpointSpec,
        McpToolSpecification toolSpec)
        throws NacosException {
        McpServerBasicInfo serverSpec = new McpServerBasicInfo();
        serverSpec.setName(mcpName);
        serverSpec.setProtocol(protocol);
        ServerVersionDetail detail = new ServerVersionDetail();
        detail.setVersion(version);
        serverSpec.setVersionDetail(detail);
        serverSpec.setDescription(description);
        serverSpec.setRemoteServerConfig(remoteServiceConfig);
        return createRemoteMcpServer(mcpName, serverSpec, toolSpec, endpointSpec);
    }
    
    /**
     * Create new remote mcp server to Nacos.
     *
     * @param mcpName      mcp server name of the new mcp server
     * @param serverSpec   mcp server specification, see {@link McpServerBasicInfo} which `type` is not
     *                     {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @param endpointSpec mcp server endpoint specification, see {@link McpEndpointSpec}, can't be null.
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.0")
    default String createRemoteMcpServer(String mcpName, McpServerBasicInfo serverSpec,
        McpEndpointSpec endpointSpec)
        throws NacosException {
        return createRemoteMcpServer(mcpName, serverSpec, null, endpointSpec);
    }
    
    /**
     * Create new remote mcp server to Nacos.
     *
     * @param mcpName      mcp server name of the new mcp server
     * @param serverSpec   mcp server specification, see {@link McpServerBasicInfo} which `type` is not
     *                     {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @param toolSpec     mcp server tools specification, see {@link McpToolSpecification}, nullable.
     * @param endpointSpec mcp server endpoint specification, see {@link McpEndpointSpec}, nullable.
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.0")
    default String createRemoteMcpServer(String mcpName, McpServerBasicInfo serverSpec,
        McpToolSpecification toolSpec,
        McpEndpointSpec endpointSpec) throws NacosException {
        if (Objects.isNull(serverSpec)) {
            throw new NacosException(NacosException.INVALID_PARAM,
                "Mcp server specification cannot be null.");
        }
        if (AiConstants.Mcp.MCP_PROTOCOL_STDIO.equalsIgnoreCase(serverSpec.getProtocol())) {
            throw new NacosException(NacosException.INVALID_PARAM,
                "Mcp server type cannot be `local` or empty.");
        }
        if (Objects.isNull(endpointSpec)) {
            throw new NacosException(NacosException.INVALID_PARAM,
                "Mcp server endpoint specification cannot be null.");
        }
        return createMcpServer(mcpName, serverSpec, toolSpec, endpointSpec);
    }
    
    /**
     * 在 Nacos 创建 MCP Server（本地或远程由 serverSpec 决定）。
     *
     * @param mcpName      mcp server name of the new mcp server
     * @param serverSpec   mcp server specification, see {@link McpServerBasicInfo}
     * @param toolSpec     mcp server tools specification, see {@link McpToolSpecification}, nullable.
     * @param endpointSpec mcp server endpoint specification, see {@link McpEndpointSpec}, nullable if `type` is
     *                     {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
     */
    @Since("3.0.0")
    default String createMcpServer(String mcpName, McpServerBasicInfo serverSpec,
        McpToolSpecification toolSpec,
        McpEndpointSpec endpointSpec) throws NacosException {
        return createMcpServer(Constants.DEFAULT_NAMESPACE_ID, mcpName, serverSpec, toolSpec,
            endpointSpec);
    }
    
    /**
     * Create new mcp server to Nacos.
     *
     * @param namespaceId namespaceId
     * @param mcpName      mcp server name of the new mcp server
     * @param serverSpec   mcp server specification, see {@link McpServerBasicInfo}
     * @param toolSpec     mcp server tools specification, see {@link McpToolSpecification}, nullable.
     * @param endpointSpec mcp server endpoint specification, see {@link McpEndpointSpec}, nullable if `type` is
     *                     {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @return mcp server id of the new mcp server
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.1")
    String createMcpServer(String namespaceId, String mcpName, McpServerBasicInfo serverSpec,
        McpToolSpecification toolSpec,
        McpEndpointSpec endpointSpec) throws NacosException;
    
    /**
     * 更新默认命名空间下已存在的 MCP Server（全量覆盖）。
     * <p>
     * Please Query Full information by {@link #getMcpServerDetail(String)} and input Full information to this method.
     * This method will full cover update the old information.
     * </p>
     *
     * @param mcpName      mcp server name of the new mcp server
     * @param serverSpec   mcp server specification, see {@link McpServerBasicInfo}
     * @param toolSpec     mcp server tools specification, see {@link McpToolSpecification}, nullable.
     * @param endpointSpec mcp server endpoint specification, see {@link McpEndpointSpec}, nullable if `type` is
     *                     {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @return {@code true} if create success, {@code false} otherwise
     * @throws NacosException if fail to create mcp server.
     */
    @Since("3.0.0")
    default boolean updateMcpServer(String mcpName, McpServerBasicInfo serverSpec,
        McpToolSpecification toolSpec,
        McpEndpointSpec endpointSpec) throws NacosException {
        return updateMcpServer(mcpName, true, serverSpec, toolSpec, endpointSpec);
    }
    
    /**
     * Update existed mcp server to Nacos Default namespace.
     * <p>
     * Please Query Full information by {@link #getMcpServerDetail(String)} and input Full information to this method.
     * This method will full cover update the old information.
     * </p>
     *
     * @param mcpName      mcp server name of the new mcp server
     * @param isLatest     publish current version to latest
     * @param serverSpec   mcp server specification, see {@link McpServerBasicInfo}
     * @param toolSpec     mcp server tools specification, see {@link McpToolSpecification}, nullable.
     * @param endpointSpec mcp server endpoint specification, see {@link McpEndpointSpec}, nullable if `type` is
     *                     {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @return {@code true} if create success, {@code false} otherwise
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.1")
    default boolean updateMcpServer(String mcpName, boolean isLatest, McpServerBasicInfo serverSpec,
        McpToolSpecification toolSpec,
        McpEndpointSpec endpointSpec) throws NacosException {
        return updateMcpServer(Constants.DEFAULT_NAMESPACE_ID, mcpName, isLatest, serverSpec,
            toolSpec, endpointSpec);
    }
    
    /**
     * 更新指定命名空间下已存在的 MCP Server。
     * <p>
     * Please Query Full information by {@link #getMcpServerDetail(String)} and input Full information to this method.
     * This method will full cover update the old information.
     * </p>
     *
     * @param namespaceId  namespaceId
     * @param mcpName      mcp server name of the new mcp server
     * @param isLatest     publish current version to latest
     * @param serverSpec   mcp server specification, see {@link McpServerBasicInfo}
     * @param toolSpec     mcp server tools specification, see {@link McpToolSpecification}, nullable.
     * @param endpointSpec mcp server endpoint specification, see {@link McpEndpointSpec}, nullable if `type` is
     *                     {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @return {@code true} if create success, {@code false} otherwise
     * @throws NacosException if fail to create mcp server.
     */
    @Since("3.0.1")
    default boolean updateMcpServer(String namespaceId, String mcpName, boolean isLatest,
        McpServerBasicInfo serverSpec,
        McpToolSpecification toolSpec, McpEndpointSpec endpointSpec) throws NacosException {
        return updateMcpServer(namespaceId, mcpName, isLatest, serverSpec, toolSpec, endpointSpec,
            false);
    }
    
    /**
     * Update existed mcp server to Nacos.
     * <p>
     * Please Query Full information by {@link #getMcpServerDetail(String)} and input Full information to this method.
     * This method will full cover update the old information.
     * </p>
     *
     * @param namespaceId  namespaceId
     * @param mcpName      mcp server name of the new mcp server
     * @param isLatest     publish current version to latest
     * @param serverSpec   mcp server specification, see {@link McpServerBasicInfo}
     * @param toolSpec     mcp server tools specification, see {@link McpToolSpecification}, nullable.
     * @param endpointSpec mcp server endpoint specification, see {@link McpEndpointSpec}, nullable if `type` is
     *                     {@link AiConstants.Mcp#MCP_PROTOCOL_STDIO}.
     * @param overrideExisting  if replace all the instances when update the mcp server
     * @return {@code true} if create success, {@code false} otherwise
     * @throws NacosException if fail to create mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.1.1")
    boolean updateMcpServer(String namespaceId, String mcpName, boolean isLatest,
        McpServerBasicInfo serverSpec, McpToolSpecification toolSpec,
        McpEndpointSpec endpointSpec, boolean overrideExisting) throws NacosException;
    
    /**
     * 从 Nacos 删除 MCP Server。
     *
     * @param mcpName mcp server name of the new mcp server
     * @return {@code true} if delete success, {@code false} otherwise
     * @throws NacosException if fail to delete mcp server.
     */
    @Since("3.0.0")
    default boolean deleteMcpServer(String mcpName) throws NacosException {
        return deleteMcpServer(Constants.DEFAULT_NAMESPACE_ID, mcpName, null, null);
    }
    
    /**
     * Delete existed mcp server from Nacos.
     *
     * @param namespaceId namespaceId
     * @param mcpName mcp server name of the new mcp server
     * @param mcpId mcp server id of the new mcp server
     * @param version mcp version of the new mcp server
     * @return {@code true} if delete success, {@code false} otherwise
     * @throws NacosException if fail to delete mcp server.
      * <p>MCP Server 维护 API；详见接口说明。</p>
     */
    @Since("3.0.2")
    boolean deleteMcpServer(String namespaceId, String mcpName, String mcpId, String version)
        throws NacosException;
}
