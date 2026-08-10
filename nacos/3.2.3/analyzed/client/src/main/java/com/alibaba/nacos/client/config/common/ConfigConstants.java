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

package com.alibaba.nacos.client.config.common;

/**
 * 配置客户端公共常量，定义请求/响应参数键名。
 *
 * <p>供 {@link ConfigRequest}、{@link ConfigResponse} 等过滤器上下文使用。</p>
 *
 * @author Nacos
 */
public class ConfigConstants {
    
    /** 租户（命名空间）参数键。 */
    public static final String TENANT = "tenant";
    
    /** 配置 dataId 参数键。 */
    public static final String DATA_ID = "dataId";
    
    /** 配置 group 参数键。 */
    public static final String GROUP = "group";
    
    /** 配置内容参数键。 */
    public static final String CONTENT = "content";
    
    /** 配置类型参数键。 */
    public static final String CONFIG_TYPE = "configType";
    
    /** 加密数据密钥参数键。 */
    public static final String ENCRYPTED_DATA_KEY = "encryptedDataKey";
    
    /** 发布配置类型参数键。 */
    public static final String TYPE = "type";
    
    /** 配置内容 MD5 摘要参数键。 */
    public static final String MD5 = "md5";
}
