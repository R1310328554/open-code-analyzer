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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * Nacos AI Mcp Server request detail form, used in create or update.
 * <p>MCP 创建/更新详情表单，继承 {@link McpForm}；必须提供 serverSpecification（McpServerBasicInfo JSON），tool/resource/endpoint 规格为可选扩展字段。</p>
 *
 * @author xiweng.yy
 */
public class McpDetailForm extends McpForm {
    
    @Serial
    private static final long serialVersionUID = 8016131725604983670L;
    
    /** MCP 服务端基础规格 JSON（McpServerBasicInfo），创建/更新时必填。 */
    private String serverSpecification;
    
    /** MCP 工具规格 JSON，描述可调用的 tool 列表。 */
    private String toolSpecification;
    
    /** MCP 资源规格 JSON，描述可访问的 resource 定义。 */
    private String resourceSpecification;
    
    /** MCP 端点规格 JSON，描述连接与传输层配置。 */
    private String endpointSpecification;
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultValue();
        if (StringUtils.isEmpty(serverSpecification)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Required parameter 'serverSpecification' type McpServerBasicInfo is not present");
        }
    }
    
    public String getServerSpecification() {
        return serverSpecification;
    }
    
    public void setServerSpecification(String serverSpecification) {
        this.serverSpecification = serverSpecification;
    }
    
    public String getToolSpecification() {
        return toolSpecification;
    }
    
    public void setToolSpecification(String toolSpecification) {
        this.toolSpecification = toolSpecification;
    }
    
    public String getResourceSpecification() {
        return resourceSpecification;
    }
    
    public void setResourceSpecification(String resourceSpecification) {
        this.resourceSpecification = resourceSpecification;
    }
    
    public String getEndpointSpecification() {
        return endpointSpecification;
    }
    
    public void setEndpointSpecification(String endpointSpecification) {
        this.endpointSpecification = endpointSpecification;
    }
    
}
