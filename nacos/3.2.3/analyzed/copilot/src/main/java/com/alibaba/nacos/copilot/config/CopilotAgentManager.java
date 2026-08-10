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

package com.alibaba.nacos.copilot.config;

import com.alibaba.nacos.common.utils.StringUtils;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.studio.StudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Copilot Agent 管理器：基于 Nacos 配置或默认属性动态构建 AgentScope {@link ReActAgent}，并可选初始化 Studio 调试环境。
 * Copilot agent manager that manages AgentScope agents with dynamic configuration.
 *
 * @author nacos
 */
@Component
public class CopilotAgentManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CopilotAgentManager.class);
    
    /** Nacos 配置存储，优先读取远端 copilot-config.json */
    private final CopilotConfigStorage configStorage;
    /** Spring 绑定的默认 Copilot 配置属性 */
    private final CopilotProperties defaultProperties;
    /** Spring 环境，用于读取 COPILOT_API_KEY 等环境变量 */
    private final Environment environment;
    
    /** 当前生效的 Copilot 配置快照（volatile 保证可见性） */
    private volatile CopilotProperties currentConfig;
    /** 读写锁，保护配置刷新与读取的并发安全 */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    @Autowired
    public CopilotAgentManager(CopilotConfigStorage configStorage,
        CopilotProperties defaultProperties,
        Environment environment) {
        this.configStorage = configStorage;
        this.defaultProperties = defaultProperties;
        this.environment = environment;
    }
    
    /**
     * 若配置了 studioUrl 则初始化 AgentScope Studio 调试会话。
     * 调用时不应持有任何锁，避免阻塞配置读写。
     */
    private void initStudio() {
        CopilotProperties config = currentConfig;
        if (config == null) {
            return;
        }
        
        String studioUrl = config.getStudioUrl();
        if (StringUtils.isBlank(studioUrl)) {
            LOGGER.debug("Studio URL is not configured, skipping Studio initialization");
            return;
        }
        
        try {
            String studioProject = config.getStudioProject();
            if (StringUtils.isBlank(studioProject)) {
                studioProject = "NacosCopilot";
            }
            LOGGER.info("Initializing AgentScope Studio with URL: {}, Project: {}", studioUrl,
                studioProject);
            StudioManager.init()
                .studioUrl(studioUrl)
                .project(studioProject)
                .runName("nacos_copilot_" + System.currentTimeMillis())
                .initialize()
                .block();
            LOGGER.info("AgentScope Studio initialized successfully");
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize AgentScope Studio: {}", e.getMessage(), e);
        }
    }
    
    /** 启动时加载配置并尝试初始化 Studio。 */
    @PostConstruct
    public void init() {
        refreshConfig();
        initStudio();
    }
    
    /**
     * 获取当前生效的 Copilot 配置（读锁保护）。
     *
     * @return 当前 {@link CopilotProperties}
     */
    public CopilotProperties getConfig() {
        lock.readLock().lock();
        try {
            return currentConfig;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 从 Nacos 配置或默认值刷新当前配置，并在锁外重初始化 Studio。
     */
    public void refreshConfig() {
        lock.writeLock().lock();
        try {
            CopilotProperties config = getEffectiveConfig();
            currentConfig = config;
            LOGGER.info("Copilot configuration refreshed");
        } finally {
            lock.writeLock().unlock();
        }
        // Re-initialize Studio if URL changed (outside lock to avoid blocking)
        initStudio();
    }
    
    /**
     * 基于当前配置创建 DashScope 流式 ReAct Agent。
     *
     * @param systemPrompt 可选系统提示词
     * @return {@link ReActAgent} 实例；未启用或未配置 API Key 时返回 null
     */
    public ReActAgent createAgent(String systemPrompt) {
        CopilotProperties config = getConfig();
        
        if (config == null || !config.isEnabled()) {
            LOGGER.warn("Copilot is disabled or not configured");
            return null;
        }
        
        String apiKey = getApiKey(config);
        if (StringUtils.isBlank(apiKey)) {
            LOGGER.warn("Copilot API Key is not configured");
            return null;
        }
        
        // 构建 DashScope 流式对话模型
        DashScopeChatModel model = DashScopeChatModel.builder()
            .apiKey(apiKey)
            .modelName(config.getModel())
            .stream(true)
            .enableThinking(true)
            .build();
        
        // 组装 ReAct Agent 并注入系统提示词
        ReActAgent.Builder agentBuilder = ReActAgent.builder()
            .name("CopilotAgent")
            .model(model);
        
        if (StringUtils.isNotBlank(systemPrompt)) {
            agentBuilder.sysPrompt(systemPrompt);
        }
        
        return agentBuilder.build();
    }
    
    /**
     * 判断 Copilot 是否已启用且 API Key 已配置。
     *
     * @return 启用且可用时返回 true
     */
    public boolean isEnabled() {
        CopilotProperties config = getConfig();
        if (config == null || !config.isEnabled()) {
            return false;
        }
        
        String apiKey = getApiKey(config);
        return StringUtils.isNotBlank(apiKey);
    }
    
    /**
     * 解析生效配置：优先 Nacos Config，否则回退默认属性。
     *
     * @return 生效的 {@link CopilotProperties}
     */
    private CopilotProperties getEffectiveConfig() {
        // 优先从 Nacos 配置中心读取
        if (configStorage != null && configStorage.isAvailable()) {
            CopilotProperties config = configStorage.getConfig();
            if (config != null) {
                LOGGER.debug("Using Copilot config from Nacos Config");
                return config;
            }
        }
        
        // 回退至 Spring 默认配置
        LOGGER.debug("Using default Copilot config");
        return defaultProperties;
    }
    
    /**
     * 解析 API Key：环境变量 COPILOT_API_KEY 优先于配置项。
     *
     * @param config Copilot 配置
     * @return API Key，未配置时返回 null
     */
    private String getApiKey(CopilotProperties config) {
        // 优先读取环境变量
        String apiKey = environment.getProperty("COPILOT_API_KEY");
        if (StringUtils.isNotBlank(apiKey)) {
            return apiKey;
        }
        
        // 其次读取配置属性
        if (config != null) {
            return config.getApiKey();
        }
        
        return null;
    }
}
