/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.configuration;

import com.alibaba.nacos.common.event.ServerConfigChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.plugin.config.constants.ConfigChangeConstants;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置变更插件配置持有者：从环境变量读取
 * {@code nacos.core.config.plugin.*} 前缀属性，按插件类型分组；
 * 订阅 {@link ServerConfigChangeEvent} 热刷新。
 * config change plugin configs.
 *
 * @author liyunfei
 **/
@Configuration
public class ConfigChangeConfigs extends Subscriber<ServerConfigChangeEvent> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigChangeConfigs.class);
    
    /** 插件配置环境变量前缀 */
    private static final String PREFIX = ConfigChangeConstants.NACOS_CORE_CONFIG_PLUGIN_PREFIX;
    
    /** 插件类型 → 属性映射（volatile 保证刷新可见性） */
    private volatile Map<String, Properties> configPluginProperties = new HashMap<>();
    
    /** 注册配置变更订阅并首次加载插件属性 */
    public ConfigChangeConfigs() {
        NotifyCenter.registerSubscriber(this);
        refreshPluginProperties();
    }
    
    /** 从 {@link EnvUtil} 重新解析带前缀的插件配置 */
    private void refreshPluginProperties() {
        try {
            Map<String, Properties> newProperties = new HashMap<>(3);
            Properties properties =
                PropertiesUtil.getPropertiesWithPrefix(EnvUtil.getEnvironment(), PREFIX);
            if (properties != null) {
                for (String each : properties.stringPropertyNames()) {
                    int typeIndex = each.indexOf('.');
                    String type = each.substring(0, typeIndex);
                    String subKey = each.substring(typeIndex + 1);
                    newProperties.computeIfAbsent(type, key -> new Properties())
                        .setProperty(subKey, properties.getProperty(each));
                }
            }
            configPluginProperties = newProperties;
        } catch (Exception e) {
            LOGGER.warn("[ConfigChangeConfigs]Refresh config plugin properties failed ", e);
        }
    }
    
    /**
     * 按插件类型返回 Properties；不存在时返回空 Properties 并 WARN。
     *
     * @param configPluginType 插件 serviceType
     * @return 插件专属配置
     */
    public Properties getPluginProperties(String configPluginType) {
        Properties properties = configPluginProperties.get(configPluginType);
        if (properties == null) {
            LOGGER.warn(
                "[ConfigChangeConfigs]Can't find config plugin properties for type {}, will use empty properties",
                configPluginType);
            return new Properties();
        }
        return properties;
    }
    
    @Override
    /** 服务端配置变更时刷新插件属性缓存 */
    public void onEvent(ServerConfigChangeEvent event) {
        refreshPluginProperties();
    }
    
    @Override
    /** 订阅 {@link ServerConfigChangeEvent} 类型 */
    public Class<? extends Event> subscribeType() {
        return ServerConfigChangeEvent.class;
    }
}
