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

import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.paramcheck.AbstractHttpParamExtractor;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;

/**
 * Nacos AI Skill param extractor.
 * <p>Skill 资源 HTTP 请求参数提取器，支持 skillName/name 参数及 skillCard JSON 体解析名称。</p>
 *
 * @author nacos
 */
public class SkillHttpParamExtractor extends AbstractHttpParamExtractor {
    
    /** 请求参数名：SkillCard JSON 载荷。 */
    private static final String SKILL_CARD_PARAM = "skillCard";
    
    /** 管理端 Skill 名称参数。 */
    private static final String SKILL_NAME_PARAM = "skillName";
    
    /** 客户端 Skill 名称参数。 */
    private static final String SKILL_CLIENT_NAME_PARAM = "name";
    
    @Override
    public List<ParamInfo> extractParam(HttpServletRequest request) throws NacosException {
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setNamespaceId(request.getParameter("namespaceId")); // 命名空间
        String skillName = resolveSkillName(request);
        paramInfo.setSkillName(skillName);
        if (request.getParameterMap().containsKey(SKILL_CARD_PARAM)) { // 优先从 SkillCard JSON 解析
            String parsedSkillName =
                deserializeAndGetSkillName(request.getParameter(SKILL_CARD_PARAM));
            paramInfo.setSkillName(parsedSkillName);
        }
        return Collections.singletonList(paramInfo);
    }
    
    /** 解析 Skill 名称：优先 skillName，否则取客户端 name 参数。 */
    private String resolveSkillName(HttpServletRequest request) {
        String skillName = request.getParameter(SKILL_NAME_PARAM);
        return StringUtils.isNotBlank(skillName) ? skillName
            : request.getParameter(SKILL_CLIENT_NAME_PARAM);
    }
    
    /** 反序列化 SkillCard JSON 并提取 name；失败返回空字符串。 */
    private String deserializeAndGetSkillName(String skillCardJson) {
        try {
            Skill skill = JacksonUtils.toObj(skillCardJson, Skill.class);
            return skill.getName();
        } catch (Exception ignored) {
            return StringUtils.EMPTY;
        }
    }
}
