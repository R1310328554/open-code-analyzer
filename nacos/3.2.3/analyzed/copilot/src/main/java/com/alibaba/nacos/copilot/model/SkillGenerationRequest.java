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

package com.alibaba.nacos.copilot.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Skill 生成请求：提交背景信息、MCP 工具与会话历史，由 Copilot 生成 Agent Skill。
 * Skill generation request.
 *
 * @author nacos
 */
public class SkillGenerationRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户提供的背景信息（必填），描述 Skill 的应用场景与能力需求。
     */
    private String backgroundInfo;
    
    /**
     * 选中的 MCP 工具列表（可选），供 Skill 生成时关联外部能力。
     */
    private List<Map<String, Object>> selectedMcpTools;
    
    /**
     * 会话历史（可选）；系统将分析其是否适合 Skill 生成或优化。
     */
    private ConversationHistory conversationHistory;
    
    /**
     * 附加参数，扩展生成行为的键值对。
     */
    private Map<String, Object> params;
    
    public SkillGenerationRequest() {
    }
    
    public String getBackgroundInfo() {
        return backgroundInfo;
    }
    
    public void setBackgroundInfo(String backgroundInfo) {
        this.backgroundInfo = backgroundInfo;
    }
    
    public List<Map<String, Object>> getSelectedMcpTools() {
        return selectedMcpTools;
    }
    
    public void setSelectedMcpTools(List<Map<String, Object>> selectedMcpTools) {
        this.selectedMcpTools = selectedMcpTools;
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    public ConversationHistory getConversationHistory() {
        return conversationHistory;
    }
    
    public void setConversationHistory(ConversationHistory conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
}
