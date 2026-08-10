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
 * 配置缓存工厂 SPI 接口：按实现名称创建 {@link ConfigCache} 与 {@link ConfigCacheGray} 实例，
 * 由 {@link ConfigCacheFactoryDelegate} 根据 {@code nacos.config.cache.type} 选择具体实现。
 * The interface Config cache factory.
 *
 * @author Sunrisea
 */
public interface ConfigCacheFactory {
    
    /**
     * 创建标准配置缓存实例。
     * Create config cache config cache.
     *
     * @return the config cache
     */
    public ConfigCache createConfigCache();
    
    /**
     * 创建灰度配置缓存实例。
     * Create config cache gray config cache gray.
     *
     * @return the config cache gray
     */
    public ConfigCacheGray createConfigCacheGray();
    
    /**
     * 返回工厂实现名称，用于与配置项 {@code nacos.config.cache.type} 匹配。
     * Gets config cache factroy name.
     *
     * @return the config cache factory name
     */
    public String getName();
}
