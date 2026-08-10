/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.visibility.spi;

import com.alibaba.nacos.api.plugin.PluginProvider;
import com.alibaba.nacos.api.plugin.PluginType;

import java.util.Map;

/**
 * 可见性插件提供者实现。
 *
 * <p>实现 {@link PluginProvider}，向 Nacos 插件框架暴露所有
 * {@link VisibilityService} SPI 实现。</p>
 *
 * @author xiweng.yy
 */
public class VisibilityPluginProvider implements PluginProvider<VisibilityService> {
    
    /**
     * 返回可见性插件类型标识。
     *
     * @return 插件类型
     */
    @Override
    public PluginType getPluginType() {
        return PluginType.VISIBILITY;
    }
    
    /**
     * 返回所有已注册的可见性服务插件。
     *
     * @return 插件名 → 服务实例映射
     */
    @Override
    public Map<String, VisibilityService> getAllPlugins() {
        return VisibilityPluginManager.getInstance().getAllPlugins();
    }
}
