/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.config;

import com.alibaba.nacos.api.plugin.PluginProvider;
import com.alibaba.nacos.api.plugin.PluginType;
import com.alibaba.nacos.plugin.config.spi.ConfigChangePluginService;

import java.util.Map;

/**
 * 配置变更插件提供者实现。
 *
 * <p>实现 {@link PluginProvider} 接口，向 Nacos 插件框架暴露 {@link PluginType#CONFIG_CHANGE}
 * 类型下的全部 {@link ConfigChangePluginService} 实例。</p>
 *
 * @author WangzJi
 * @since 3.2.0
 */
public class ConfigChangePluginProvider implements PluginProvider<ConfigChangePluginService> {
    
    /**
     * 返回配置变更插件类型标识。
     *
     * @return {@link PluginType#CONFIG_CHANGE}
     */
    @Override
    public PluginType getPluginType() {
        return PluginType.CONFIG_CHANGE;
    }
    
    /**
     * 获取全部已加载的配置变更插件服务。
     *
     * @return 服务类型到插件服务的映射
     */
    @Override
    public Map<String, ConfigChangePluginService> getAllPlugins() {
        return ConfigChangePluginManager.getInstance().getAllPlugins();
    }
}
