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

package com.alibaba.nacos.ai.form.skills.admin;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * Skill detail form (for create and detail).
 * <p>Skill 详情/创建表单，skillCard 为完整 Skill 信息的 JSON 字符串，创建时 skillName 可省略（从 skillCard 解析）。</p>
 *
 * @author nacos
 */
public class SkillDetailForm extends SkillForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * Skill card JSON string, contains complete Skill information.
     * <p>Skill 卡片 JSON，包含 Skill 完整定义信息，创建/详情场景必填。</p>
     */
    private String skillCard;
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultNamespaceId();
        // 创建/详情场景下 skillName 可选（可从 skillCard 解析）
        // 仅 skillCard 为必填项
        if (StringUtils.isEmpty(skillCard)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Request parameter `skillCard` should not be `null` or empty.");
        }
    }
    
    public String getSkillCard() {
        return skillCard;
    }
    
    public void setSkillCard(String skillCard) {
        this.skillCard = skillCard;
    }
}
