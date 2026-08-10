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

package com.alibaba.nacos.api;

/**
 * 通过 JVM {@code -D} 系统属性读取 Nacos 配置项的键名常量。
 *
 * <p>优先级高于 {@link PropertyKeyConst} 中同名 Properties 键。</p>
 *
 * @author pbting
 */
public interface SystemPropertyKeyConst {
    
    String NAMING_SERVER_PORT = "nacos.naming.exposed.port";
    
    /**
     * 云环境（阿里云或其他云厂商）下是否启用命名空间解析，默认开启。
     */
    String IS_USE_CLOUD_NAMESPACE_PARSING = "nacos.use.cloud.namespace.parsing";
    
    /** 云环境下可通过 {@code -D} 指定进程级全局统一命名空间。 */
    String ANS_NAMESPACE = "ans.namespace";
    
    /** 是否启用 endpoint 解析规则，亦可通过 {@code -D} 指定。 */
    String IS_USE_ENDPOINT_PARSING_RULE = "nacos.use.endpoint.parsing.rule";
    
    /** 自 2.3.3 起，供 Java Agent 等无法读取环境 RAM 信息的场景使用。 */
    String IS_USE_RAM_INFO_PARSING = "nacos.use.ram.info.parsing";
}
