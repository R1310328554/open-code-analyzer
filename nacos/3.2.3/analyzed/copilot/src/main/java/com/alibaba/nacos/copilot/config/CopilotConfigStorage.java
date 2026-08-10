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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.api.config.model.ConfigDetailInfo;
import com.alibaba.nacos.maintainer.client.config.ConfigMaintainerFactory;
import com.alibaba.nacos.maintainer.client.config.ConfigMaintainerService;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * Copilot 配置持久化：通过 {@link ConfigMaintainerService} 读写 copilot-config.json，实现 Copilot 参数的动态热更新。
 * Copilot configuration storage using Nacos Config.
 *
 * @author nacos
 */
@Component
public class CopilotConfigStorage {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CopilotConfigStorage.class);
    
    /** Nacos 配置 dataId */
    private static final String CONFIG_DATA_ID = "copilot-config.json";
    /** Nacos 配置 group */
    private static final String CONFIG_GROUP = "nacos-copilot";
    /** 默认命名空间（未显式配置时使用） */
    private static final String DEFAULT_NAMESPACE = "public";
    
    /** Copilot 配置所在 Nacos 命名空间 */
    @Value("${nacos.copilot.config.namespace:public}")
    private String configNamespace;
    
    /** Nacos 服务端地址，为空时使用系统属性或本地默认值 */
    @Value("${nacos.copilot.config.serverAddr:}")
    private String serverAddr;
    
    /** 配置运维客户端，负责 get/publish 操作 */
    private ConfigMaintainerService configMaintainerService;
    
    /**
     * 初始化配置运维客户端，按 serverAddr 或默认地址连接 Nacos。
     */
    @PostConstruct
    public void init() {
        try {
            if (StringUtils.isNotBlank(serverAddr)) {
                Properties properties = new Properties();
                properties.setProperty("serverAddr", serverAddr);
                configMaintainerService =
                    ConfigMaintainerFactory.createConfigMaintainerService(properties);
                LOGGER.info("Copilot config storage initialized with serverAddr: {}", serverAddr);
            } else {
                // 未配置 serverAddr 时使用环境默认地址
                String defaultServerAddr =
                    System.getProperty("nacos.server.addr", "127.0.0.1:8848");
                Properties properties = new Properties();
                properties.setProperty("serverAddr", defaultServerAddr);
                configMaintainerService =
                    ConfigMaintainerFactory.createConfigMaintainerService(properties);
                LOGGER.info("Copilot config storage initialized with default serverAddr: {}",
                    defaultServerAddr);
            }
        } catch (Exception e) {
            LOGGER.warn(
                "Failed to initialize Copilot config storage, will use default configuration", e);
        }
    }
    
    /**
     * 从 Nacos 配置中心读取并反序列化 Copilot 配置。
     *
     * @return {@link CopilotProperties}；未配置或解析失败时返回 null
     */
    public CopilotProperties getConfig() {
        if (configMaintainerService == null) {
            return null;
        }
        
        try {
            ConfigDetailInfo configInfo = configMaintainerService.getConfig(
                CONFIG_DATA_ID, CONFIG_GROUP, configNamespace);
            
            if (configInfo != null && StringUtils.isNotBlank(configInfo.getContent())) {
                return JacksonUtils.toObj(configInfo.getContent(), CopilotProperties.class);
            }
        } catch (NacosException e) {
            LOGGER.debug("Copilot config not found in Nacos Config, using default configuration",
                e);
        } catch (Exception e) {
            LOGGER.warn("Failed to parse Copilot config from Nacos Config", e);
        }
        
        return null;
    }
    
    /**
     * 将 Copilot 配置序列化后发布至 Nacos 配置中心。
     *
     * @param config 待保存的配置
     * @return 发布成功返回 true
     */
    public boolean saveConfig(CopilotProperties config) {
        if (configMaintainerService == null) {
            LOGGER.warn("Config maintainer service is not initialized, cannot save config");
            return false;
        }
        
        try {
            String content = JacksonUtils.toJson(config);
            boolean result = configMaintainerService.publishConfig(
                CONFIG_DATA_ID,
                CONFIG_GROUP,
                configNamespace,
                content,
                "nacos-copilot",
                "system",
                null,
                "Copilot configuration",
                "json");
            
            if (result) {
                LOGGER.info("Copilot config saved successfully to Nacos Config");
            } else {
                LOGGER.warn("Failed to save Copilot config to Nacos Config");
            }
            
            return result;
        } catch (NacosException e) {
            LOGGER.error("Failed to save Copilot config to Nacos Config", e);
            return false;
        }
    }
    
    /**
     * 判断配置运维客户端是否已成功初始化。
     *
     * @return 可用时返回 true
     */
    public boolean isAvailable() {
        return configMaintainerService != null;
    }
}
