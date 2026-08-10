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

package com.alibaba.nacos.ai.param;

import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpec;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.paramcheck.AbstractHttpParamExtractor;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;

/**
 * Nacos AI AgentSpec param extractor.
 * <p>AgentSpec HTTP 请求参数提取器，解析 namespaceId 与 agentSpecName，支持从 agentSpecCard JSON 体反序列化名称。</p>
 *
 * @author nacos
 */
public class AgentSpecHttpParamExtractor extends AbstractHttpParamExtractor {
    
    /** 请求参数名：AgentSpec JSON 载荷。 */
    private static final String AGENTSPEC_CARD_PARAM = "agentSpecCard";
    
    @Override
    public List<ParamInfo> extractParam(HttpServletRequest request) throws NacosException {
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setNamespaceId(request.getParameter("namespaceId")); // 命名空间
        paramInfo.setAgentName(request.getParameter("agentSpecName")); // 显式 agentSpecName 参数
        if (request.getParameterMap().containsKey(AGENTSPEC_CARD_PARAM)) { // 优先从 AgentSpec JSON 解析名称
            paramInfo.setAgentName(
                deserializeAndGetAgentSpecName(request.getParameter(AGENTSPEC_CARD_PARAM)));
        }
        return Collections.singletonList(paramInfo);
    }
    
    /** 反序列化 AgentSpec JSON 并提取 name；失败返回空字符串。 */
    private String deserializeAndGetAgentSpecName(String agentSpecCardJson) {
        try {
            AgentSpec agentSpec = JacksonUtils.toObj(agentSpecCardJson, AgentSpec.class);
            return agentSpec.getName();
        } catch (Exception ignored) {
            return StringUtils.EMPTY;
        }
    }
}
