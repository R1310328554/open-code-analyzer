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
 * 配置缓存 MD5 后处理器 SPI：在写入 {@link ConfigCache} 前对内容做额外处理
 * （如自定义摘要算法），由 {@link ConfigCachePostProcessorDelegate} 按类型加载。
 * The interface Config cache md5 post processor.
 *
 * @author Sunrisea
 */
public interface ConfigCachePostProcessor {
    
    /**
     * 返回后处理器名称，与 {@code nacos.config.cache.type} 匹配。
     * Gets post processor name.
     *
     * @return the post processor name
     */
    public String getName();
    
    /**
     * 对配置内容执行后处理并更新 {@link ConfigCache}（如重算 MD5）。
     * Post process.
     *
     * @param configCache the config cache
     * @param content     the content
     */
    public void postProcess(ConfigCache configCache, String content);
}
