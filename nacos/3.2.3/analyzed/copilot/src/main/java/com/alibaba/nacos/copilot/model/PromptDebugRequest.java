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
 * Prompt 调试请求：携带系统 Prompt 与用户输入，供 Copilot 在线验证效果。
 * Prompt debug request.
 *
 * @author nacos
 */
public class PromptDebugRequest {
    
    /**
     * 系统 Prompt 内容，定义 AI 角色与行为准则。
     */
    private String prompt;
    
    /**
     * 用户输入内容，用于配合 Prompt 进行联调测试。
     */
    private String userInput;
    
    public PromptDebugRequest() {
    }
    
    public PromptDebugRequest(String prompt, String userInput) {
        this.prompt = prompt;
        this.userInput = userInput;
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
