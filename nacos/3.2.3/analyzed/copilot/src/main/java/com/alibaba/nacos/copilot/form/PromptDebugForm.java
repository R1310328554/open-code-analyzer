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
 * Prompt 调试表单：携带系统 Prompt 与用户输入，供 Copilot 在线验证 Prompt 效果。
 * Prompt debug form.
 *
 * @author nacos
 */
public class PromptDebugForm {
    
    /**
     * 系统 Prompt 内容（必填），定义 AI 角色与行为准则的模板。
     */
    private String prompt;
    
    /**
     * 用户输入内容（必填），用于配合 Prompt 进行联调测试。
     */
    private String userInput;
    
    /**
     * 校验 Prompt 与用户输入均非空。
     */
    public void validate() {
        if (StringUtils.isBlank(prompt)) {
            throw new IllegalArgumentException("Prompt is required");
        }
        if (StringUtils.isBlank(userInput)) {
            throw new IllegalArgumentException("User input is required");
        }
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public String getUserInput() {
        return userInput;
    }
    
    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }
}
