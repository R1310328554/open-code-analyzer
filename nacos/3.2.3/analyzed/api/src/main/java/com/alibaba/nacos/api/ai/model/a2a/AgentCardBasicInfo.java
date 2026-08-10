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

package com.alibaba.nacos.api.ai.model.a2a;

import java.util.List;
import java.util.Objects;

/**
 * Agent Card 基础信息模型，承载 A2A Agent 的名称、版本、能力与技能列表等核心元数据。
 *
 * <p>作为 {@link AgentCard}、{@link AgentCardVersionInfo} 等模型的父类，
 * 仅包含卡片级公共字段，不含端点 URL 与安全方案等扩展属性。</p>
 *
 * @author xiweng.yy
 */
public class AgentCardBasicInfo {
    
    /**
     * 旧版 A2A 协议兼容字段，后续版本可能移除；A2A 1.0.0 请改用 {@link AgentCard#getSupportedInterfaces()}。
     *
     * @deprecated For old A2A protocol compatibility only.
     */
    @Deprecated
    private String protocolVersion;
    
    /** Agent 显示名称。 */
    private String name;
    
    /** Agent 功能描述。 */
    private String description;
    
    /** Agent 版本号。 */
    private String version;
    
    /** Agent 图标 URL。 */
    private String iconUrl;
    
    /** Agent 能力声明（流式、推送、扩展卡片等）。 */
    private AgentCapabilities capabilities;
    
    /** Agent 暴露的技能列表。 */
    private List<AgentSkill> skills;
    
    public String getProtocolVersion() {
        return protocolVersion;
    }
    
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getIconUrl() {
        return iconUrl;
    }
    
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    
    public AgentCapabilities getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(AgentCapabilities capabilities) {
        this.capabilities = capabilities;
    }
    
    public List<AgentSkill> getSkills() {
        return skills;
    }
    
    public void setSkills(List<AgentSkill> skills) {
        this.skills = skills;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentCardBasicInfo that = (AgentCardBasicInfo) o;
        return Objects.equals(protocolVersion, that.protocolVersion)
            && Objects.equals(name, that.name)
            && Objects.equals(description, that.description)
            && Objects.equals(version, that.version)
            && Objects.equals(iconUrl, that.iconUrl)
            && Objects.equals(capabilities, that.capabilities)
            && Objects.equals(skills, that.skills);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(protocolVersion, name, description, version, iconUrl, capabilities,
            skills);
    }
}
