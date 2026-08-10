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

package com.alibaba.nacos.plugin.trace;

import com.alibaba.nacos.api.plugin.PluginProvider;
import com.alibaba.nacos.api.plugin.PluginType;
import com.alibaba.nacos.plugin.trace.spi.NacosTraceSubscriber;

import java.util.Map;

/**
 * 链路追踪插件提供者实现。
 *
 * <p>实现 {@link PluginProvider}，向 Nacos 插件框架暴露
 * {@link NacosTracePluginManager} 中已加载的全部追踪订阅者。</p>
 *
 * @author WangzJi
 * @since 3.2.0
 */
public class TracePluginProvider implements PluginProvider<NacosTraceSubscriber> {
    
    /**
     * 返回追踪插件类型标识。
     *
     * @return 插件类型
     */
    @Override
    public PluginType getPluginType() {
        return PluginType.TRACE;
    }
    
    /**
     * 返回所有已注册的追踪订阅者。
     *
     * @return 插件名 → 订阅者实例映射
     */
    @Override
    public Map<String, NacosTraceSubscriber> getAllPlugins() {
        return NacosTracePluginManager.getInstance().getAllPlugins();
    }
}
