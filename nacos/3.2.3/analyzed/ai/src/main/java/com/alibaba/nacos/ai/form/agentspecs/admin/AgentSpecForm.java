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

package com.alibaba.nacos.ai.form.agentspecs.admin;

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * AgentSpec form base class.
 * <p>AgentSpec 管理端请求表单基类，封装命名空间、名称与版本等公共字段，并实现 {@link NacosForm#validate()} 校验逻辑；命名空间为空时自动填充 {@link Constants.AgentSpecs#AGENTSPEC_DEFAULT_NAMESPACE}。</p>
 *
 * @author nacos
 */
public class AgentSpecForm implements NacosForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 命名空间 ID，为空时使用 AgentSpec 默认命名空间。 */
    private String namespaceId;
    
    /** AgentSpec 资源名称，多数管理端操作必填。 */
    private String agentSpecName;
    
    /** 版本号，部分操作（发布、上下线等）使用。 */
    private String version;
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultNamespaceId();
        if (StringUtils.isEmpty(agentSpecName)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Required parameter 'agentSpecName' type String is not present");
        }
    }
    
    /** 命名空间为空时填充 AgentSpec 默认命名空间。 */
    protected void fillDefaultNamespaceId() {
        if (StringUtils.isEmpty(namespaceId)) {
            namespaceId = Constants.AgentSpecs.AGENTSPEC_DEFAULT_NAMESPACE;
        }
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getAgentSpecName() {
        return agentSpecName;
    }
    
    public void setAgentSpecName(String agentSpecName) {
        this.agentSpecName = agentSpecName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
}
