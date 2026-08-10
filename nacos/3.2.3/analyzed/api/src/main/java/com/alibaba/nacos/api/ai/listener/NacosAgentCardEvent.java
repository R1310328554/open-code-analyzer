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

package com.alibaba.nacos.api.ai.listener;

import com.alibaba.nacos.api.ai.model.a2a.AgentCardDetailInfo;

/**
 * Nacos AI 模块 Agent Card 变更事件，携带完整 Agent 卡片详情。
 *
 * <p>由 {@link AgentCardDetailInfo} 构造，可通过 {@link #getAgentName()} 与
 * {@link #getAgentCard()} 读取 Agent 名称与卡片内容。</p>
 *
 * @author xiweng.yy
 */
public class NacosAgentCardEvent implements NacosAiEvent {
    
    private final String agentName;
    
    private final AgentCardDetailInfo agentCard;
    
    /**
     * 由 Agent 卡片详情构造事件。
     *
     * @param agentCard Agent 卡片详情，名称取自 {@link AgentCardDetailInfo#getName()}
     */
    public NacosAgentCardEvent(AgentCardDetailInfo agentCard) {
        this.agentName = agentCard.getName();
        this.agentCard = agentCard;
    }
    
    /** 返回 Agent 名称。 */
    public String getAgentName() {
        return agentName;
    }
    
    /** 返回 Agent 卡片详情对象。 */
    public AgentCardDetailInfo getAgentCard() {
        return agentCard;
    }
}
