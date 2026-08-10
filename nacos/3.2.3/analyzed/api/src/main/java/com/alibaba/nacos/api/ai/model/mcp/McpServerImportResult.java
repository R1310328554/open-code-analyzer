/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

import java.io.Serializable;

/**
 * 单个 MCP Server 的导入结果，记录名称、ID、状态及错误信息。
 *
 * @author nacos
 */
public class McpServerImportResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** Server 名称。 */
    private String serverName;
    
    /** 导入后的 Server ID。 */
    private String serverId;
    
    /** 导入状态：success、failed 或 skipped。 */
    private String status;
    
    /** 失败时的错误信息。 */
    private String errorMessage;
    
    /** 跳过时记录的冲突类型。 */
    private String conflictType;
    
    public String getServerName() {
        return serverName;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public String getServerId() {
        return serverId;
    }
    
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getConflictType() {
        return conflictType;
    }
    
    public void setConflictType(String conflictType) {
        this.conflictType = conflictType;
    }
}
