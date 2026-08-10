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

package com.alibaba.nacos.client.naming.utils;

/**
 * 命名客户端通用常量与 URL 片段。
 *
 * <p>Web 上下文与 HTTP API 路径由 {@link InitUtils#initWebRootContext} 在启动时根据配置覆盖。</p>
 *
 * @author xuanyin.zy
 */
public class UtilAndComs {
    
    /** Web 应用上下文路径（默认 /nacos）。 */
    public static String webContext = "/nacos";
    
    /** 命名 HTTP API 基础路径。 */
    public static String nacosUrlBase = webContext + "/v1/ns";
    
    /** 实例相关 API 路径前缀。 */
    public static String nacosUrlInstance = nacosUrlBase + "/instance";
    
    /** 服务相关 API 路径前缀。 */
    public static String nacosUrlService = nacosUrlBase + "/service";
    
    /** 环境列表配置键。 */
    public static final String ENV_LIST_KEY = "envList";
    
    /** 表示全部 IP 的特殊占位常量。 */
    public static final String ALL_IPS = "000--00-ALL_IPS--00--000";
    
    /** VIP 容灾开关配置键。 */
    public static final String FAILOVER_SWITCH = "00-00---000-VIPSRV_FAILOVER_SWITCH-000---00-00";
    
    /** 默认 public 命名空间 ID。 */
    public static final String DEFAULT_NAMESPACE_ID = "public";
    
    /** 域名请求最大重试次数。 */
    public static final int REQUEST_DOMAIN_RETRY_COUNT = 3;
    
    /** 命名日志文件名配置键（已废弃）。 */
    @Deprecated
    public static final String NACOS_NAMING_LOG_NAME = "com.alibaba.nacos.naming.log.filename";
    
    /** 命名日志级别配置键（已废弃）。 */
    @Deprecated
    public static final String NACOS_NAMING_LOG_LEVEL = "com.alibaba.nacos.naming.log.level";
    
    /** 环境配置集合键。 */
    public static final String ENV_CONFIGS = "00-00---000-ENV_CONFIGS-000---00-00";
    
    /** VIP 客户端属性文件名。 */
    public static final String VIP_CLIENT_FILE = "vipclient.properties";
    
    /** 全部主机列表配置键。 */
    public static final String ALL_HOSTS = "00-00---000-ALL_HOSTS-000---00-00";
    
}
