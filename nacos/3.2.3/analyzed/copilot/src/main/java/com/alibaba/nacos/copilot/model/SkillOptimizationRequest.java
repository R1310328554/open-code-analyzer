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

import com.alibaba.nacos.api.ai.model.skills.Skill;

import java.io.Serializable;
import java.util.Map;

/**
 * Skill 优化请求：提交待优化 Skill、目标与会话历史，由 Copilot 改进 Agent Skill。
 * Skill optimization request.
 *
 * @author nacos
 */
public class SkillOptimizationRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 原始 Skill（必填，前端应先获取完整 Skill）。
     */
    private Skill skill;
    
    /**
     * 优化目标（可选），用户输入的改进方向或需求。
     */
    private String optimizationGoal;
    
    /**
     * 会话历史（可选）；系统分析其是否适合 Skill 优化及应做哪些改进。
     */
    private ConversationHistory conversationHistory;
    
    /**
     * 待优化目标文件名（可选）；指定则仅优化该文件，否则优化整个 Skill。
     */
    private String targetFileName;
    
    /**
     * 附加参数，扩展优化行为的键值对。
     */
    private Map<String, Object> params;
    
    public SkillOptimizationRequest() {
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
    
    public String getTargetFileName() {
        return targetFileName;
    }
    
    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }
}
