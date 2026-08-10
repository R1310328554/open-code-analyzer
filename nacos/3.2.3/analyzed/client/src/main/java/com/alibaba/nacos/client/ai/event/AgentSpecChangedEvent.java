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

import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpec;
import com.alibaba.nacos.common.notify.Event;

/**
 * Nacos AI 模块 AgentSpec 变更内部事件（客户端）。
 *
 * <p>由 {@link NacosAgentSpecCacheHolder} 在 MD5 变更时发布，
 * 供 {@link AiChangeNotifier} 转换为 {@link NacosAgentSpecEvent}。</p>
 *
 * @author nacos
 */
public class AgentSpecChangedEvent extends Event {
    
    private static final long serialVersionUID = 7893214560182347651L;
    
    /** AgentSpec 名称。 */
    private final String agentSpecName;
    
    /** 变更后的 AgentSpec 对象。 */
    private final AgentSpec agentSpec;
    
    /** 构造 AgentSpec 变更事件。 */
    public AgentSpecChangedEvent(String agentSpecName, AgentSpec agentSpec) {
        this.agentSpecName = agentSpecName;
        this.agentSpec = agentSpec;
    }
    
    public String getAgentSpecName() {
        return agentSpecName;
    }
    
    public AgentSpec getAgentSpec() {
        return agentSpec;
    }
}
