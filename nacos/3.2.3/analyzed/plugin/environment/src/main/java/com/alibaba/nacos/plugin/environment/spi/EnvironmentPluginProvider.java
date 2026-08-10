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

package com.alibaba.nacos.plugin.environment.spi;

import com.alibaba.nacos.api.plugin.PluginProvider;
import com.alibaba.nacos.api.plugin.PluginType;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 环境插件提供者实现。
 *
 * <p>实现 {@link PluginProvider}，向 Nacos 插件框架暴露所有
 * {@link CustomEnvironmentPluginService} SPI 实现。</p>
 *
 * @author WangzJi
 * @since 3.2.0
 */
public class EnvironmentPluginProvider implements PluginProvider<CustomEnvironmentPluginService> {
    
    /**
     * 返回环境插件类型标识。
     *
     * @return 插件类型
     */
    @Override
    public PluginType getPluginType() {
        return PluginType.ENVIRONMENT;
    }
    
    /**
     * 加载并返回所有已注册的环境插件，以插件名索引。
     *
     * @return 插件名 → 服务实例映射
     */
    @Override
    public Map<String, CustomEnvironmentPluginService> getAllPlugins() {
        Collection<CustomEnvironmentPluginService> services = NacosServiceLoader.load(
            CustomEnvironmentPluginService.class);
        Map<String, CustomEnvironmentPluginService> result = new HashMap<>(services.size());
        for (CustomEnvironmentPluginService service : services) {
            String name = service.pluginName();
            if (StringUtils.isNotBlank(name)) {
                result.put(name, service);
            }
        }
        return result;
    }
}
