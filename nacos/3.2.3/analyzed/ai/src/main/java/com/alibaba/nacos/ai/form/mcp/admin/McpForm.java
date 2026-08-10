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

package com.alibaba.nacos.ai.form.mcp.admin;

import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * Nacos AI Mcp Server request form.
 * <p>MCP 管理端请求表单基类，封装 mcpId、mcpName、namespaceId 与 version 等公共字段；校验时要求 mcpId 与 mcpName 至少提供一个，命名空间为空时填充 MCP 默认命名空间。</p>
 *
 * @author xiweng.yy
 */
public class McpForm implements NacosForm {
    
    @Serial
    private static final long serialVersionUID = 1314659974972866397L;
    
    /** MCP 资源唯一标识，与 mcpName 二选一必填。 */
    private String mcpId;
    
    /** 命名空间 ID，为空时使用 {@link com.alibaba.nacos.api.ai.constant.AiConstants.Mcp#MCP_DEFAULT_NAMESPACE}。 */
    private String namespaceId;
    
    /** MCP 服务名称，与 mcpId 二选一必填。 */
    private String mcpName;
    
    /** 版本号，部分查询与更新操作使用。 */
    private String version;
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultValue();
        if (StringUtils.isEmpty(mcpId) && StringUtils.isEmpty(mcpName)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Required parameter 'mcpId' or 'mcpName' type String at lease one is not present");
        }
    }
    
    /** 命名空间为空时填充 MCP 默认命名空间。 */
    protected void fillDefaultValue() {
        if (StringUtils.isEmpty(namespaceId)) {
            namespaceId = AiConstants.Mcp.MCP_DEFAULT_NAMESPACE;
        }
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getMcpId() {
        return mcpId;
    }
    
    public void setMcpId(String id) {
        this.mcpId = id;
    }
    
    public String getMcpName() {
        return mcpName;
    }
    
    public void setMcpName(String name) {
        this.mcpName = name;
    }
}
