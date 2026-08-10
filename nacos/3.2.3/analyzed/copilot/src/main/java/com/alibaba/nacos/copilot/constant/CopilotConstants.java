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

package com.alibaba.nacos.copilot.constant;

/**
 * Nacos Copilot 常量：定义 REST API 路径前缀与各能力端点相对路径。
 * Nacos Copilot Constants.
 *
 * @author nacos
 */
public class CopilotConstants {
    
    /** Copilot API 根路径 */
    public static final String COPILOT_PATH = "/copilot";
    
    /** 控制台 Copilot API 完整前缀路径 */
    public static final String COPILOT_CONSOLE_PATH = "/v3/console" + COPILOT_PATH;
    
    /** Skill 优化端点相对路径 */
    public static final String SKILL_OPTIMIZE_PATH = "/skill/optimize";
    
    /** Skill 生成端点相对路径 */
    public static final String SKILL_GENERATE_PATH = "/skill/generate";
    
    /** Prompt 优化端点相对路径 */
    public static final String PROMPT_OPTIMIZE_PATH = "/prompt/optimize";
    
    /** Prompt 调试端点相对路径 */
    public static final String PROMPT_DEBUG_PATH = "/prompt/debug";
    
    /** 对话端点相对路径 */
    public static final String CHAT_PATH = "/chat";
    
    /** 对话历史端点相对路径 */
    public static final String CHAT_HISTORY_PATH = "/chat/history";
    
    private CopilotConstants() {
        // 私有构造器，禁止实例化常量类
    }
}
