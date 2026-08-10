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

import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.copilot.model.ConversationHistory;

import java.util.List;
import java.util.Map;

/**
 * Skill 优化表单：提交待优化 Skill、目标与上下文，驱动 Copilot 改进已有 Agent Skill。
 * Skill optimization form.
 *
 * @author nacos
 */
public class SkillOptimizationForm {
    
    /**
     * 原始 Skill（必填），待优化的 Agent Skill 对象。
     */
    private Skill skill;
    
    /**
     * 优化目标（可选），用户描述的改进方向或需求。
     */
    private String optimizationGoal;
    
    /**
     * 选中的 MCP 工具列表（可选），供优化时关联外部能力。
     */
    private List<Map<String, Object>> selectedMcpTools;
    
    /**
     * 会话历史（可选），包含用户输入、工具调用与模型回复。
     */
    private ConversationHistory conversationHistory;
    
    /**
     * 待优化目标文件名（可选）；指定则仅优化该文件内容，否则优化整个 Skill。
     */
    private String targetFileName;
    
    /**
     * 校验表单数据：Skill 非空且名称有效。
     */
    public void validate() {
        if (skill == null) {
            throw new IllegalArgumentException("Skill is required");
        }
        if (StringUtils.isBlank(skill.getName())) {
            throw new IllegalArgumentException("Skill name is required");
        }
    }
    
    public Skill getSkill() {
        return skill;
    }
    
    public void setSkill(Skill skill) {
        this.skill = skill;
    }
    
    public String getOptimizationGoal() {
        return optimizationGoal;
    }
    
    public void setOptimizationGoal(String optimizationGoal) {
        this.optimizationGoal = optimizationGoal;
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
    
    public String getTargetFileName() {
        return targetFileName;
    }
    
    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }
}
