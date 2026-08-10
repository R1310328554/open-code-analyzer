/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.config.constants;

/**
 * 配置变更插件服务相关常量定义。
 *
 * <p>包含插件配置前缀、请求参数键名等，供插件实现与框架交互时统一引用。</p>
 *
 * @author liyunfei
 */
public class ConfigChangeConstants {
    
    /** Nacos 核心配置插件属性前缀。 */
    public static final String NACOS_CORE_CONFIG_PLUGIN_PREFIX = "nacos.core.config.plugin.";
    
    /** 插件自定义属性在请求参数中的键名。 */
    public static final String PLUGIN_PROPERTIES = "pluginProperties";
    
    /**
     * 原始配置方法调用参数在请求中的键名。
     */
    public static final String ORIGINAL_ARGS = "originalArgs";
    
}
