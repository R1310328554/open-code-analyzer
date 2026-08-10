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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.console.controller.v3.ai;

import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.paramcheck.AbstractHttpParamExtractor;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.List;

/**
 * Copilot SSE 接口 HTTP 参数提取器，从请求体或查询参数解析 Skill 名称与命名空间，
 * 供 {@link com.alibaba.nacos.core.paramcheck.ExtractorManager} 权限校验使用。
 *
 * Copilot HTTP parameter extractor.
 *
 * @author nacos
 */
public class CopilotHttpParamExtractor extends AbstractHttpParamExtractor {
    
    /** POST 方法常量。 */
    private static final String HTTP_METHOD_POST = "POST";
    
    /** 请求体 JSON 中 skill 字段键名片段，用于快速判断是否含 Skill 对象。 */
    private static final String SKILL_JSON_KEY = "\"skill\"";
    
    /** 从 HTTP 请求提取 Copilot 权限校验参数。 */
    @Override
    public List<ParamInfo> extractParam(HttpServletRequest request) throws NacosException {
        ParamInfo paramInfo = new ParamInfo();
        
        // 尝试从 POST 请求体提取 Skill 名称（用于优化类接口的权限校验）
        if (HTTP_METHOD_POST.equalsIgnoreCase(request.getMethod())) {
            try {
                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }
                }
                
                if (body.length() > 0) {
                    // 解析 JSON 请求体提取 Skill 名称与命名空间
                    String bodyStr = body.toString();
                    if (bodyStr.contains(SKILL_JSON_KEY)) {
                        // 从 skill 嵌套对象反序列化为 Skill 模型
                        try {
                            java.util.Map<String, Object> bodyMap =
                                JacksonUtils.toObj(bodyStr, java.util.Map.class);
                            java.util.Map<String, Object> skillMap =
                                (java.util.Map<String, Object>) bodyMap.get("skill");
                            if (skillMap != null) {
                                Skill skill =
                                    JacksonUtils.toObj(JacksonUtils.toJson(skillMap), Skill.class);
                                if (skill != null && StringUtils.isNotBlank(skill.getName())) {
                                    paramInfo.setAgentName(skill.getName());
                                    paramInfo.setNamespaceId(skill.getNamespaceId());
                                }
                            }
                        } catch (Exception e) {
                            // 解析失败时忽略，回退至查询参数
                        }
                    }
                }
            } catch (Exception e) {
                // 读取请求体失败时忽略
            }
        }
        
        // 回退至查询参数 skillName / namespaceId
        if (StringUtils.isBlank(paramInfo.getAgentName())) {
            paramInfo.setAgentName(request.getParameter("skillName"));
        }
        if (StringUtils.isBlank(paramInfo.getNamespaceId())) {
            paramInfo.setNamespaceId(request.getParameter("namespaceId"));
        }
        
        return Collections.singletonList(paramInfo);
    }
}
