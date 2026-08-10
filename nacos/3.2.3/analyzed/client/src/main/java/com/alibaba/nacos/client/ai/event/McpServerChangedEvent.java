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

package com.alibaba.nacos.client.ai.event;

import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.client.ai.utils.CacheKeyUtils;
import com.alibaba.nacos.common.notify.Event;

/**
 * Nacos 客户端 MCP 服务变更内部事件。
 *
 * <p>当 {@code NacosMcpServerCacheHolder} 检测到服务端 MCP 配置变更时发布，携带 MCP 名称、版本及完整 {@link McpServerDetailInfo} 详情，供 {@code AiChangeNotifier} 分发给已注册监听器。</p>
 *
 * @author xiweng.yy
 */
public class McpServerChangedEvent extends Event {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 2010793364377243018L;
    
    /** MCP 服务名称。 */
    private final String mcpName;
    
    /** MCP 服务版本标识（latest 或具体版本号）。 */
    private final String version;
    
    /** 变更后的 MCP 服务完整详情。 */
    private final McpServerDetailInfo mcpServer;
    
    /**
     * 根据 MCP 服务详情构造变更事件。
     *
     * @param mcpServer MCP 服务详情
     */
        this.mcpServer = mcpServer;
        this.mcpName = mcpServer.getName();
        this.version = buildVersion(mcpServer);
    }
    
    /** 根据版本详情构建缓存键版本字符串。 */
    private String buildVersion(McpServerDetailInfo mcpServer) {
        return mcpServer.getVersionDetail().getIs_latest() ? CacheKeyUtils.LATEST_VERSION
            : mcpServer.getVersionDetail().getVersion();
    }
    
    /** 返回 MCP 服务名称。 */
    public String getMcpName() {
        return mcpName;
    }
    
    /** 返回 MCP 服务版本标识。 */
    public String getVersion() {
        return version;
    }
    
    /** 返回变更后的 MCP 服务详情。 */
    public McpServerDetailInfo getMcpServer() {
        return mcpServer;
    }
}
