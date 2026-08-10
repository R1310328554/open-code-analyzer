/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.utils;

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.ai.model.mcp.FrontEndpointConfig;
import com.alibaba.nacos.api.ai.model.mcp.McpEndpointSpec;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainResponse;

import java.util.Arrays;

import static com.alibaba.nacos.ai.constant.Constants.MCP_SERVER_CONFIG_MARK;

/**
 * Mcp config utils.
 * <p>MCP 服务器配置工具：格式化 dataId、构建配置标签、判断配置查询状态、将前端端点配置转为 {@link McpEndpointSpec} 等。</p>
 * @author xinluo
 */
public class McpConfigUtils {
    
    private static final String MCP_ENDPOINT_CONFIG_DIRECT_SPLIT = ":";
    
    private static final String MCP_ENDPOINT_SPEC_ADDRESS_KEY = "address";
    
    private static final String MCP_ENDPOINT_SPEC_PORT_KEY = "port";
    
    private static final int MCP_ENDPOINT_SPEC_PORT_KEY_LENGTH = 2;
    
    /**
     * Format the Mcp server version info config data id.
     * <p>格式化 MCP 服务器版本信息配置的 dataId。</p>
     * @param id server id
     * @return mcp server version info config data id
     */
    public static String formatServerVersionInfoDataId(String id) {
        return String.format(Constants.SERVER_VERSION_CONFIG_DATA_ID_TEMPLATE, id);
    }
    
    /** 格式化 MCP 服务器规格信息配置的 dataId。 */
    public static String formatServerSpecInfoDataId(String id, String version) {
        return String.format(Constants.SERVER_SPECIFICATION_CONFIG_DATA_ID_TEMPLATE, id, version);
    }
    
    /** 格式化 MCP 工具规格配置的 dataId。 */
    public static String formatServerToolSpecDataId(String id, String version) {
        return String.format(Constants.SERVER_TOOLS_SPEC_CONFIG_DATA_ID_TEMPLATE, id, version);
    }
    
    /** 格式化 MCP 资源规格配置的 dataId。 */
    public static String formatServerResourceSpecDataId(String id, String version) {
        return String.format(Constants.SERVER_RESOURCE_SPEC_CONFIG_DATA_ID_TEMPLATE, id, version);
    }
    
    /** 构建 MCP 服务器名称标签的模糊搜索值。 */
    public static String formatServerNameTagBlurSearchValue(String serverName) {
        return Constants.MCP_SERVER_NAME_TAG_KEY_PREFIX + Constants.ALL_PATTERN + serverName
            + Constants.ALL_PATTERN;
    }
    
    /** 构建 MCP 服务器名称标签的精确搜索值。 */
    public static String formatServerNameTagAccurateSearchValue(String serverName) {
        return Constants.MCP_SERVER_NAME_TAG_KEY_PREFIX + serverName;
    }
    
    /** 判断配置查询状态是否为“已找到正式配置”。 */
    public static boolean isConfigFound(ConfigQueryChainResponse.ConfigQueryStatus status) {
        return ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_FOUND_FORMAL.equals(status);
    }
    
    /** 判断配置查询状态是否为“未找到”。 */
    public static boolean isConfigNotFound(ConfigQueryChainResponse.ConfigQueryStatus status) {
        return ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_NOT_FOUND.equals(status);
    }
    
    /** 构建 MCP 服务器版本配置的 tags 字符串（含配置标记与名称标签）。 */
    public static String buildMcpServerVersionConfigTags(String serverName) {
        return StringUtils.join(Arrays.asList(MCP_SERVER_CONFIG_MARK,
            Constants.MCP_SERVER_NAME_TAG_KEY_PREFIX + serverName), ",");
    }
    
    /**
     * Convert {@link FrontEndpointConfig} to {@link McpEndpointSpec}.
     * <p>将 {@link FrontEndpointConfig} 转为 {@link McpEndpointSpec}，当前仅支持 DIRECT 类型（host:port 格式 endpointData）。</p>
     * @param frontEndpointConfig front endpoint config
     * @return mcp endpoint spec
     * @throws IllegalArgumentException if convert failed
     */
    public static McpEndpointSpec convertFrontEndpointConfig(
        FrontEndpointConfig frontEndpointConfig) {
        Object epDataObj = frontEndpointConfig.getEndpointData();
        String epData = (String) epDataObj;
        McpEndpointSpec endpointSpec = new McpEndpointSpec();
        String[] hp = epData.split(MCP_ENDPOINT_CONFIG_DIRECT_SPLIT);
        if (hp.length != MCP_ENDPOINT_SPEC_PORT_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid endpoint data: " + epData);
        }
        endpointSpec.setType(AiConstants.Mcp.MCP_ENDPOINT_TYPE_DIRECT);
        endpointSpec.getData().put(MCP_ENDPOINT_SPEC_ADDRESS_KEY, hp[0]);
        endpointSpec.getData().put(MCP_ENDPOINT_SPEC_PORT_KEY, hp[1]);
        endpointSpec.getData().put(Constants.MCP_BACKEND_INSTANCE_PROTOCOL_KEY,
            frontEndpointConfig.getProtocol());
        return endpointSpec;
    }
}
