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

package com.alibaba.nacos.api.ai.model.skills;

import java.util.Map;

/**
 * Claude Skill 实体，用于独立 Skill 管理。
 *
 * <p>精简结构，仅包含 SKILL.md 正文与资源映射等核心字段。</p>
 *
 * @author nacos
 */
public class Skill extends SkillBase {
    
    /** 完整 SKILL.md Markdown 正文。 */
    private String skillMd;
    
    /** 资源映射（字段名为 resource，键为资源名）。 */
    private Map<String, SkillResource> resource;
    
    public String getSkillMd() {
        return skillMd;
    }
    
    public void setSkillMd(String skillMd) {
        this.skillMd = skillMd;
    }
    
    public Map<String, SkillResource> getResource() {
        return resource;
    }
    
    public void setResource(Map<String, SkillResource> resource) {
        this.resource = resource;
    }
}
