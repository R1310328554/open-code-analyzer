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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.console.controller.v3.ai;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.copilot.config.CopilotAgentManager;
import com.alibaba.nacos.copilot.config.CopilotConfigStorage;
import com.alibaba.nacos.copilot.config.CopilotProperties;
import com.alibaba.nacos.copilot.constant.CopilotConstants;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制台 Copilot 配置 REST 控制器，管理 LLM API Key、模型与 Studio 连接参数。
 * 仅暴露/接受 apiKey、model、studioUrl、studioProject 四个字段的简化视图。
 *
 * Console Copilot configuration controller.
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(CopilotConstants.COPILOT_CONSOLE_PATH + "/config")
public class ConsoleCopilotConfigController {
    
    /** Copilot 配置持久化存储。 */
    private final CopilotConfigStorage configStorage;
    
    /** Copilot Agent 管理器，配置变更后需刷新。 */
    private final CopilotAgentManager agentManager;
    
    @Autowired
    public ConsoleCopilotConfigController(CopilotConfigStorage configStorage,
        CopilotAgentManager agentManager) {
        this.configStorage = configStorage;
        this.agentManager = agentManager;
    }
    
    /**
      * 获取当前 Copilot 配置（仅返回 apiKey、model、studioUrl、studioProject）。
     * Get current Copilot configuration. Only returns apiKey, model, studioUrl and studioProject fields.
     *
     * @return Simplified CopilotProperties with only apiKey, model, studioUrl and studioProject
     */
    @Since("3.2.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<CopilotProperties> getConfig() throws NacosException {
        CopilotProperties config = configStorage.getConfig();
        if (config == null) {
            // 未配置时返回默认空配置
            config = new CopilotProperties();
        }
        
        // 构造仅含 apiKey、model、studioUrl、studioProject 的简化视图
        CopilotProperties simplifiedConfig = new CopilotProperties();
        simplifiedConfig.setApiKey(config.getApiKey());
        simplifiedConfig.setModel(config.getModel());
        simplifiedConfig.setStudioUrl(config.getStudioUrl());
        simplifiedConfig.setStudioProject(config.getStudioProject());
        
        return Result.success(simplifiedConfig);
    }
    
    /**
      * 创建或更新 Copilot 配置（仅接受四个简化字段，其余使用默认值）。
     * Create or update Copilot configuration. Only accepts apiKey, model, studioUrl and studioProject fields, other
     * fields use defaults.
     *
     * @param config Simplified CopilotProperties with only apiKey, model, studioUrl and studioProject
     * @return success result
     */
    @Since("3.2.0")
    @PostMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<Boolean> saveConfig(@RequestBody CopilotProperties config) throws NacosException {
        if (config == null) {
            throw new NacosException(NacosException.INVALID_PARAM, "Configuration cannot be null");
        }
        
        // 读取已有配置以保留其他字段，或创建带默认值的新配置
        CopilotProperties existingConfig = configStorage.getConfig();
        CopilotProperties fullConfig;
        
        if (existingConfig != null) {
            // 在已有配置上仅更新 apiKey、model、studioUrl、studioProject
            fullConfig = existingConfig;
        } else {
            // 创建带默认值的新配置
            fullConfig = new CopilotProperties();
        }
        
        // 仅更新 apiKey、model、studioUrl、studioProject 四个字段
        if (config.getApiKey() != null) {
            fullConfig.setApiKey(config.getApiKey());
        }
        if (config.getModel() != null) {
            fullConfig.setModel(config.getModel());
        }
        if (config.getStudioUrl() != null) {
            fullConfig.setStudioUrl(config.getStudioUrl());
        }
        if (config.getStudioProject() != null) {
            fullConfig.setStudioProject(config.getStudioProject());
        }
        
        boolean success = configStorage.saveConfig(fullConfig);
        
        if (success) {
            // 配置保存成功后刷新 Copilot Agent 运行时
            agentManager.refreshConfig();
        }
        
        return Result.success(success);
    }
}
