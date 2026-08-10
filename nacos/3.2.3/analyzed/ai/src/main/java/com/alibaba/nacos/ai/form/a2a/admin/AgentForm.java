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

package com.alibaba.nacos.ai.form.a2a.admin;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

import static com.alibaba.nacos.api.ai.constant.AiConstants.A2a.A2A_DEFAULT_NAMESPACE;

/**
 * A2A Agent 基础请求表单。
 *
 * <p>包含 namespaceId、agentName、version、registrationType；namespaceId 缺省时使用 A2A 默认命名空间。</p>
 *
 * @author KiteSoar
 **/
public class AgentForm implements NacosForm {
    
    @Serial
    private static final long serialVersionUID = -73912927386186928L;
    
    /** 命名空间 ID */
    private String namespaceId;
    
    /** Agent 名称 */
    private String agentName;
    
    /** Agent 版本号 */
    private String version;
    
    /** 注册类型（URL 或 SERVICE） */
    private String registrationType;
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultNamespaceId();
        if (StringUtils.isEmpty(agentName)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Required parameter 'name' type String is not present");
        }
    }
    
    /** 空 namespaceId 时填充 A2A 默认命名空间 */
    protected void fillDefaultNamespaceId() {
        if (StringUtils.isEmpty(namespaceId)) {
            namespaceId = A2A_DEFAULT_NAMESPACE;
        }
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getAgentName() {
        return agentName;
    }
    
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getRegistrationType() {
        return registrationType;
    }
    
    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }
}
