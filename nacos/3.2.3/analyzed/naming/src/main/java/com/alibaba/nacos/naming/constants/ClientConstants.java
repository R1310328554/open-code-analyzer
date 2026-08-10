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

package com.alibaba.nacos.naming.constants;

import java.util.concurrent.TimeUnit;

/**
 * 命名模块客户端相关常量。
 *
 * <p>定义连接类型、元数据键、实例端口模式及客户端过期时间等配置项。</p>
 *
 * @author xiweng.yy
 */
public class ClientConstants {
    
    /** 客户端连接类型属性键。 */
    public static final String CONNECTION_TYPE = "connectionType";
    
    /** 连接元数据属性键。 */
    public static final String CONNECTION_METADATA = "connectionMetadata";
    
    /** 默认客户端工厂标识。 */
    public static final String DEFAULT_FACTORY = "default";
    
    /** 临时实例 IP:Port 模式标识。 */
    public static final String EPHEMERAL_IP_PORT = "ephemeralIpPort";
    
    /** 持久实例 IP:Port 模式标识。 */
    public static final String PERSISTENT_IP_PORT = "persistentIpPort";
    
    /** 客户端数据版本号键。 */
    public static final String REVISION = "revision";
    
    /** 持久实例后缀标识（非临时）。 */
    public static final String PERSISTENT_SUFFIX = "false";
    
    /** 客户端过期时间配置键（毫秒）。 */
    public static final String CLIENT_EXPIRED_TIME_CONFIG_KEY = "nacos.naming.client.expired.time";
    
    /** 默认客户端过期时间：3 分钟。 */
    public static final long DEFAULT_CLIENT_EXPIRED_TIME = TimeUnit.MINUTES.toMillis(3);
    
}
