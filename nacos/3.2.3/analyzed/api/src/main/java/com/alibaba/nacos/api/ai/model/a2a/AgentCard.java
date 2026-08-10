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

package com.alibaba.nacos.api.ai.model.a2a;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A2A Agent Card 完整模型，描述 Agent 的接口、安全与能力等元数据。
 *
 * <p>继承 {@link AgentCardBasicInfo}，扩展端点 URL、传输协议、安全方案及
 * A2A 1.0.0 新增的 {@link #supportedInterfaces} 等字段。</p>
 *
 * @author KiteSoar
 */
public class AgentCard extends AgentCardBasicInfo {
    
    /**
     * 旧版 A2A 协议兼容字段，后续版本可能移除；A2A 1.0.0 请改用 {@link #supportedInterfaces}。
     *
     * @deprecated For old A2A protocol compatibility only.
     */
    @Deprecated
    private String url;
    
    /**
     * 旧版 A2A 协议兼容字段；A2A 1.0.0 请改用 {@link AgentInterface#getProtocolBinding()}。
     *
     * @deprecated For old A2A protocol compatibility only.
     */
    @Deprecated
    private String preferredTransport;
    
    /**
     * Legacy field for old A2A protocol compatibility, may be removed in future versions.
     * Use {@link #supportedInterfaces} for A2A 1.0.0.
     *
     * @deprecated For old A2A protocol compatibility only.
      * <p>Nacos AI 监听器/模型 API；详见上方说明。</p>
     */
    @Deprecated
    private List<AgentInterface> additionalInterfaces;
    
    /** A2A 1.0.0 新增字段。
     *
     * @since 3.2.1
     */
    private List<AgentInterface> supportedInterfaces;
    
    /** Agent 提供方信息。 */
    private AgentProvider provider;
    
    /** Agent 文档 URL。 */
    private String documentationUrl;
    
    /** 安全方案定义映射（方案名 → 方案详情）。 */
    private Map<String, SecurityScheme> securitySchemes;
    
    /** 旧版安全要求列表（A2A 1.0.0 前）。 */
    private List<Map<String, List<String>>> security;
    
    /**
     * For A2A 1.0.0.
     *
     * @since 3.2.1
      * <p>Nacos AI 监听器/模型 API；详见上方说明。</p>
     */
    private List<Map<String, List<String>>> securityRequirements;
    
    /** 默认输入模式列表。 */
    private List<String> defaultInputModes;
    
    /** 默认输出模式列表。 */
    private List<String> defaultOutputModes;
    
    /**
     * 旧版 A2A 协议兼容字段；A2A 1.0.0 请改用 {@link AgentCapabilities#getExtendedAgentCard()}。
     *
     * @deprecated For old A2A protocol compatibility only.
     */
    @Deprecated
    private Boolean supportsAuthenticatedExtendedCard;
    
    /**
     * For A2A 1.0.0.
     *
     * @since 3.2.1
      * <p>Nacos AI 监听器/模型 API；详见上方说明。</p>
     */
    private List<Map<String, Object>> signatures;
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getPreferredTransport() {
        return preferredTransport;
    }
    
    public void setPreferredTransport(String preferredTransport) {
        this.preferredTransport = preferredTransport;
    }
    
    public List<AgentInterface> getAdditionalInterfaces() {
        return additionalInterfaces;
    }
    
    public void setAdditionalInterfaces(List<AgentInterface> additionalInterfaces) {
        this.additionalInterfaces = additionalInterfaces;
    }
    
    public List<AgentInterface> getSupportedInterfaces() {
        return supportedInterfaces;
    }
    
    public void setSupportedInterfaces(List<AgentInterface> supportedInterfaces) {
        this.supportedInterfaces = supportedInterfaces;
    }
    
    public AgentProvider getProvider() {
        return provider;
    }
    
    public void setProvider(AgentProvider provider) {
        this.provider = provider;
    }
    
    public String getDocumentationUrl() {
        return documentationUrl;
    }
    
    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }
    
    public Map<String, SecurityScheme> getSecuritySchemes() {
        return securitySchemes;
    }
    
    public void setSecuritySchemes(Map<String, SecurityScheme> securitySchemes) {
        this.securitySchemes = securitySchemes;
    }
    
    public List<Map<String, List<String>>> getSecurity() {
        return security;
    }
    
    public void setSecurity(List<Map<String, List<String>>> security) {
        this.security = security;
    }
    
    public List<Map<String, List<String>>> getSecurityRequirements() {
        return securityRequirements;
    }
    
    public void setSecurityRequirements(List<Map<String, List<String>>> securityRequirements) {
        this.securityRequirements = securityRequirements;
    }
    
    public List<String> getDefaultInputModes() {
        return defaultInputModes;
    }
    
    public void setDefaultInputModes(List<String> defaultInputModes) {
        this.defaultInputModes = defaultInputModes;
    }
    
    public List<String> getDefaultOutputModes() {
        return defaultOutputModes;
    }
    
    public void setDefaultOutputModes(List<String> defaultOutputModes) {
        this.defaultOutputModes = defaultOutputModes;
    }
    
    public Boolean getSupportsAuthenticatedExtendedCard() {
        return supportsAuthenticatedExtendedCard;
    }
    
    public void setSupportsAuthenticatedExtendedCard(Boolean supportsAuthenticatedExtendedCard) {
        this.supportsAuthenticatedExtendedCard = supportsAuthenticatedExtendedCard;
    }
    
    public List<Map<String, Object>> getSignatures() {
        return signatures;
    }
    
    public void setSignatures(List<Map<String, Object>> signatures) {
        this.signatures = signatures;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AgentCard agentCard = (AgentCard) o;
        return super.equals(agentCard) && Objects.equals(url, agentCard.url)
            && Objects.equals(preferredTransport,
                agentCard.preferredTransport)
            && Objects.equals(additionalInterfaces, agentCard.additionalInterfaces)
            && Objects.equals(supportedInterfaces, agentCard.supportedInterfaces)
            && Objects.equals(provider, agentCard.provider) && Objects.equals(documentationUrl,
                agentCard.documentationUrl)
            && Objects.equals(securitySchemes, agentCard.securitySchemes)
            && Objects.equals(security, agentCard.security) && Objects.equals(securityRequirements,
                agentCard.securityRequirements)
            && Objects.equals(defaultInputModes,
                agentCard.defaultInputModes)
            && Objects.equals(defaultOutputModes, agentCard.defaultOutputModes)
            && Objects.equals(supportsAuthenticatedExtendedCard,
                agentCard.supportsAuthenticatedExtendedCard)
            && Objects.equals(signatures, agentCard.signatures);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, preferredTransport, additionalInterfaces,
            supportedInterfaces,
            provider, documentationUrl, securitySchemes, security, securityRequirements,
            defaultInputModes,
            defaultOutputModes, supportsAuthenticatedExtendedCard, signatures);
    }
}
