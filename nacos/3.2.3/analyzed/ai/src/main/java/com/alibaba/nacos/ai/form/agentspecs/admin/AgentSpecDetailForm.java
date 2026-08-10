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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * AgentSpec 详情/创建表单。
 *
 * <p>{@code agentSpecCard} 必填，包含完整 AgentSpec 信息；agentSpecName 可省略（从 card 中解析）。</p>
 *
 * @author nacos
 */
public class AgentSpecDetailForm extends AgentSpecForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * AgentSpec Card JSON 字符串，包含完整 AgentSpec 信息。
     */
    private String agentSpecCard;
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultNamespaceId();
        // 创建/详情场景 agentSpecName 可选（可含于 agentSpecCard）
        // 仅 agentSpecCard 必填
        if (StringUtils.isEmpty(agentSpecCard)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Request parameter `agentSpecCard` should not be `null` or empty.");
        }
    }
    
    public String getAgentSpecCard() {
        return agentSpecCard;
    }
    
    public void setAgentSpecCard(String agentSpecCard) {
        this.agentSpecCard = agentSpecCard;
    }
}
