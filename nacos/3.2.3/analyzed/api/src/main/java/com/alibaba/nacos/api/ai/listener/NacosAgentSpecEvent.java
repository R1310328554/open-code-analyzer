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

import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpec;

/**
 * Nacos AI 模块 Agent 规格（AgentSpec）变更事件。
 *
 * <p>包含规格名称与 {@link AgentSpec} 对象，用于监听 Agent 定义文件的发布与更新。</p>
 *
 * @author nacos
 */
public class NacosAgentSpecEvent implements NacosAiEvent {
    
    private final String agentSpecName;
    
    private final AgentSpec agentSpec;
    
    /**
     * 构造 AgentSpec 变更事件。
     *
     * @param agentSpecName Agent 规格名称
     * @param agentSpec Agent 规格内容
     */
    public NacosAgentSpecEvent(String agentSpecName, AgentSpec agentSpec) {
        this.agentSpecName = agentSpecName;
        this.agentSpec = agentSpec;
    }
    
    /** 返回 Agent 规格名称。 */
    public String getAgentSpecName() {
        return agentSpecName;
    }
    
    /** 返回 Agent 规格对象。 */
    public AgentSpec getAgentSpec() {
        return agentSpec;
    }
}
