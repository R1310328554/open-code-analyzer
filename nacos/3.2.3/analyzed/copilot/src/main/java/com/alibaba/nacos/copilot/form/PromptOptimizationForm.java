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

/**
 * Prompt 优化表单：提交原始 Prompt 及可选优化目标，驱动 Copilot 生成改进版本。
 * Prompt optimization form.
 *
 * @author nacos
 */
public class PromptOptimizationForm {
    
    /**
     * 原始 Prompt 内容（必填）。
     */
    private String prompt;
    
    /**
     * 优化目标或需求描述（可选），如「使回复更简洁」「增加示例」「支持多语言」等。
     */
    private String optimizationGoal;
    
    /**
     * 校验原始 Prompt 非空。
     */
    public void validate() {
        if (StringUtils.isBlank(prompt)) {
            throw new IllegalArgumentException("Prompt is required");
        }
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
