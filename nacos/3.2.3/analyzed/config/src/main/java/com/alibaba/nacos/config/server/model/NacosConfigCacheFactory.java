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
 * Nacos 默认 {@link ConfigCacheFactory} 实现：创建 {@link ConfigCache} 与 {@link ConfigCacheGray}。
 * 通过 SPI 或委托注册为名称 {@code nacos} 的缓存工厂。
 * The type Nacos config cache factory.
 *
 * @author Sunrisea
 */
public class NacosConfigCacheFactory implements ConfigCacheFactory {
    
    /** 创建主版本内存配置缓存实例 */
    @Override
    public ConfigCache createConfigCache() {
        return new ConfigCache();
    }
    
    /** 创建灰度版本内存配置缓存实例 */
    @Override
    public ConfigCacheGray createConfigCacheGray() {
        return new ConfigCacheGray();
    }
    
    /** 返回工厂标识 {@code nacos} */
    @Override
    public String getName() {
        return "nacos";
    }
}
