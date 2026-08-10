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

/**
 * 插件配置项类型枚举。
 *
 * <p>定义 {@link ConfigItemDefinition} 支持的数据类型，供控制台渲染对应输入控件。</p>
 *
 * @author WangzJi
 * @since 3.2.0
 */
public enum ConfigItemType {
    
    /** 字符串类型配置。 */
    STRING,
    
    /** 数值类型配置。 */
    NUMBER,
    
    /** 布尔类型配置。 */
    BOOLEAN,
    
    /** 枚举类型配置（需配合 enumValues）。 */
    ENUM
}
