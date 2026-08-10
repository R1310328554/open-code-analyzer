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

/**
 * Prompt 优化请求：提交原始 Prompt 及优化目标，驱动 Copilot 生成改进版本。
 * Prompt optimization request.
 *
 * @author nacos
 */
public class PromptOptimizationRequest {
    
    /**
     * 原始 Prompt 内容（必填）。
     */
    private String prompt;
    
    /**
     * 优化目标或需求描述，如「使回复更简洁」「增加示例」等。
     */
    private String optimizationGoal;
    
    public PromptOptimizationRequest() {
    }
    
    public PromptOptimizationRequest(String prompt, String optimizationGoal) {
        this.prompt = prompt;
        this.optimizationGoal = optimizationGoal;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public String getOptimizationGoal() {
        return optimizationGoal;
    }
    
    public void setOptimizationGoal(String optimizationGoal) {
        this.optimizationGoal = optimizationGoal;
    }
}
