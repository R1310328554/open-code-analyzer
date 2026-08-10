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
import java.util.Objects;

/**
 * A2A Agent 能力描述，声明 Agent 支持的交互特性。
 *
 * <p>涵盖流式响应、推送通知、状态迁移历史及扩展 Agent Card 等能力标志，
 * 并可通过 {@link #extensions} 挂载自定义扩展。</p>
 *
 * @author KiteSoar
 */
public class AgentCapabilities {
    
    private Boolean streaming;
    
    private Boolean pushNotifications;
    
    private Boolean stateTransitionHistory;
    
    /** A2A 1.0.0 扩展 Agent Card 能力标志。
     *
     * @since 3.2.1
     */
    private Boolean extendedAgentCard;
    
    private List<AgentExtension> extensions;
    
    /** 是否支持流式响应。 */
    public Boolean getStreaming() {
        return streaming;
    }
    
    /** 设置是否支持流式响应。 */
    public void setStreaming(Boolean streaming) {
        this.streaming = streaming;
    }
    
    /** 是否支持推送通知。 */
    public Boolean getPushNotifications() {
        return pushNotifications;
    }
    
    /** 设置是否支持推送通知。 */
    public void setPushNotifications(Boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }
    
    /** 是否支持状态迁移历史。 */
    public Boolean getStateTransitionHistory() {
        return stateTransitionHistory;
    }
    
    /** 设置是否支持状态迁移历史。 */
    public void setStateTransitionHistory(Boolean stateTransitionHistory) {
        this.stateTransitionHistory = stateTransitionHistory;
    }
    
    /** 是否支持扩展 Agent Card（A2A 1.0.0）。 */
    public Boolean getExtendedAgentCard() {
        return extendedAgentCard;
    }
    
    /** 设置是否支持扩展 Agent Card。 */
    public void setExtendedAgentCard(Boolean extendedAgentCard) {
        this.extendedAgentCard = extendedAgentCard;
    }
    
    /** 返回 Agent 扩展能力列表。 */
    public List<AgentExtension> getExtensions() {
        return extensions;
    }
    
    /** 设置 Agent 扩展能力列表。 */
    public void setExtensions(List<AgentExtension> extensions) {
        this.extensions = extensions;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentCapabilities that = (AgentCapabilities) o;
        return Objects.equals(streaming, that.streaming)
            && Objects.equals(pushNotifications, that.pushNotifications)
            && Objects.equals(stateTransitionHistory, that.stateTransitionHistory)
            && Objects.equals(
                extendedAgentCard, that.extendedAgentCard)
            && Objects.equals(extensions, that.extensions);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(streaming, pushNotifications, stateTransitionHistory, extendedAgentCard,
            extensions);
    }
}
