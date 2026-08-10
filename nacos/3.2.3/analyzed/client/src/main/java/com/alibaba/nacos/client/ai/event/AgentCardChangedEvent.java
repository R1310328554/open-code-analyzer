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

package com.alibaba.nacos.client.ai.event;

import com.alibaba.nacos.api.ai.model.a2a.AgentCardDetailInfo;
import com.alibaba.nacos.client.ai.utils.CacheKeyUtils;
import com.alibaba.nacos.common.notify.Event;

/**
 * Nacos AI 模块 Agent Card 变更内部事件（客户端）。
 *
 * <p>由缓存层 {@link NacosAgentCardCacheHolder} 发布，
 * {@link AiChangeNotifier} 消费后转换为 {@link NacosAgentCardEvent} 分发给监听器。</p>
 *
 * @author xiweng.yy
 */
public class AgentCardChangedEvent extends Event {
    
    private static final long serialVersionUID = 2010793364377243018L;
    
    /** Agent 名称。 */
    private final String agentName;
    
    /** 版本键，最新版时使用 {@link CacheKeyUtils#LATEST_VERSION}。 */
    private final String version;
    
    /** 变更后的 Agent Card 详情。 */
    private final AgentCardDetailInfo agentCard;
    
    /** 由 Agent Card 详情构造变更事件并解析名称与版本键。 */
    public AgentCardChangedEvent(AgentCardDetailInfo agentCard) {
        this.agentCard = agentCard;
        this.agentName = agentCard.getName();
        this.version = buildVersion(agentCard);
    }
    
    /** 根据是否最新版本构建缓存键用的版本字符串。 */
    private String buildVersion(AgentCardDetailInfo agentCard) {
        if (null == agentCard.isLatestVersion() || agentCard.isLatestVersion()) {
            return CacheKeyUtils.LATEST_VERSION;
        }
        return agentCard.getVersion();
    }
    
    public String getAgentName() {
        return agentName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public AgentCardDetailInfo getAgentCard() {
        return agentCard;
    }
}
