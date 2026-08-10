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

package com.alibaba.nacos.copilot.form;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.copilot.model.ConversationHistory;

import java.util.List;
import java.util.Map;

/**
 * Skill 生成表单：提交用户背景信息、可选 MCP 工具与会话历史，驱动 Copilot 生成 Agent Skill。
 * Skill generation form.
 *
 * @author nacos
 */
public class SkillGenerationForm {
    
    /**
     * 用户提供的背景信息（必填），描述 Skill 的应用场景与能力需求。
     */
    private String backgroundInfo;
    
    /**
     * 选中的 MCP 工具列表（可选），供 Skill 生成时关联外部能力。
     */
    private List<Map<String, Object>> selectedMcpTools;
    
    /**
     * 会话历史（可选），包含用户输入、工具调用与模型回复。
     */
    private ConversationHistory conversationHistory;
    
    /**
     * 校验表单数据：背景信息不能为空。
     */
    public void validate() {
        if (StringUtils.isBlank(backgroundInfo)) {
            throw new IllegalArgumentException("Background information is required");
        }
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
    
    public ConversationHistory getConversationHistory() {
        return conversationHistory;
    }
    
    public void setConversationHistory(ConversationHistory conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
}
