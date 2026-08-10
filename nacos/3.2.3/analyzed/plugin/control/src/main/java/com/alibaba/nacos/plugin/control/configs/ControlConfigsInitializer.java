/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control.configs;

/**
 * 管控插件配置初始化 SPI 接口。
 *
 * <p>实现类通过 SPI 注册，在 {@link ControlConfigs} 单例首次创建时注入各项配置参数。</p>
 *
 * @author shiyiyue
 */
public interface ControlConfigsInitializer {
    
    /**
     * 初始化管控配置参数。
     *
     * @param controlConfigs 待填充的配置容器
     */
    void initialize(ControlConfigs controlConfigs);
    
}
