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

package com.alibaba.nacos.copilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Copilot 配置属性：绑定 {@code nacos.copilot.*} 前缀，涵盖启用开关、模型、API Key 与 Studio 调试参数。
 * Copilot configuration properties.
 *
 * @author nacos
 */
@Component
@ConfigurationProperties(prefix = "nacos.copilot")
public class CopilotProperties {
    
    /**
     * 是否启用 Copilot 功能。
     */
    private boolean enabled = true;
    
    /**
     * 默认命名空间 ID。
     */
    private String defaultNamespace = "public";
    
    /**
     * DashScope API Key（可由环境变量或配置文件提供）。
     */
    private String apiKey;
    
    /**
     * 大模型名称，默认 qwen-turbo。
     */
    private String model = "qwen-turbo";
    
    /**
     * AgentScope Studio 调试服务地址。
     */
    private String studioUrl;
    
    /**
     * AgentScope Studio 项目名称。
     */
    private String studioProject = "NacosCopilot";
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getDefaultNamespace() {
        return defaultNamespace;
    }
    
    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }
    
    /**
     * 获取 DashScope API Key。
     *
     * @return API Key
     */
    public String getApiKey() {
        return apiKey;
    }
    
    /**
     * 设置 DashScope API Key。
     *
     * @param apiKey API Key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * 获取大模型名称。
     *
     * @return 模型名称
     */
    public String getModel() {
        return model;
    }
    
    /**
     * 设置大模型名称。
     *
     * @param model 模型名称
     */
    public void setModel(String model) {
        this.model = model;
    }
    
    /**
     * 获取 AgentScope Studio 地址。
     *
     * @return Studio URL
     */
    public String getStudioUrl() {
        return studioUrl;
    }
    
    /**
     * 设置 AgentScope Studio 地址。
     *
     * @param studioUrl Studio URL
     */
    public void setStudioUrl(String studioUrl) {
        this.studioUrl = studioUrl;
    }
    
    /**
     * 获取 AgentScope Studio 项目名称。
     *
     * @return Studio 项目名
     */
    public String getStudioProject() {
        return studioProject;
    }
    
    /**
     * 设置 AgentScope Studio 项目名称。
     *
     * @param studioProject Studio 项目名
     */
    public void setStudioProject(String studioProject) {
        this.studioProject = studioProject;
    }
}
