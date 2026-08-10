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

package com.alibaba.nacos.api.plugin;

import java.util.List;
import java.util.Map;

/**
 * 插件配置规格接口。
 *
 * <p>允许插件声明可配置属性列表，并在运行时应用/读取键值对配置。</p>
 *
 * @author WangzJi
 * @since 3.2.0
 */
public interface PluginConfigSpec {
    
    /**
     * 获取配置项定义列表。
     *
     * @return 配置项定义集合
     */
    List<ConfigItemDefinition> getConfigDefinitions();
    
    /**
     * 将配置应用到插件实例。
     *
     * @param config 配置键值对
     */
    void applyConfig(Map<String, String> config);
    
    /**
     * 获取当前生效的配置。
     *
     * @return 当前配置键值对
     */
    Map<String, String> getCurrentConfig();
}
