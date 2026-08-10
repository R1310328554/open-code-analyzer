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

package com.alibaba.nacos.config.server.model;

/**
 * Nacos 默认配置缓存后处理器：实现 {@link ConfigCachePostProcessor} SPI，
 * 在配置写入内存缓存后执行 MD5 等后处理逻辑（当前实现为空操作，保留扩展点）。
 * The type Nacos config cache md 5 post processor.
 *
 * @author Sunrisea
 */
public class NacosConfigCachePostProcessor implements ConfigCachePostProcessor {
    
    /** {@inheritDoc} 返回处理器标识 "nacos"，供 {@link ConfigCachePostProcessorDelegate} 路由。 */
    @Override
    public String getName() {
        return "nacos";
    }
    
    /**
     * 配置缓存写入后的后处理钩子。
     *
     * @param configCache 已更新的内存缓存条目
     * @param content     配置正文内容
     */
    @Override
    public void postProcess(ConfigCache configCache, String content) {
    }
}
