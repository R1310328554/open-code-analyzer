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

/**
 * 命名 HTTP/API 请求与响应字段名常量。
 *
 * <p>统一服务、实例、集群、命名空间等 JSON 字段键，避免硬编码字符串。</p>
 *
 * @author lixiaoshuang
 */
public class FieldsConstants {
    
    /** 通用名称字段。 */
    public static final String NAME = "name";
    
    /** 服务保护阈值字段。 */
    public static final String PROTECT_THRESHOLD = "protectThreshold";
    
    /** 服务分组名字段。 */
    public static final String GROUP_NAME = "groupName";
    
    /** 服务选择器字段。 */
    public static final String SELECTOR = "selector";
    
    /** 元数据字段。 */
    public static final String METADATA = "metadata";
    
    /** 服务对象字段。 */
    public static final String SERVICE = "service";
    
    /** 集群列表字段。 */
    public static final String CLUSTERS = "clusters";
    
    /** 服务列表字段。 */
    public static final String SERVICE_LIST = "serviceList";
    
    /** 计数结果字段。 */
    public static final String COUNT = "count";
    
    /** 命名空间 ID 字段。 */
    public static final String NAME_SPACE_ID = "namespaceId";
    
    /** 命名空间字段。 */
    public static final String NAME_SPACE = "namespace";
    
    /** 健康检查器配置字段。 */
    public static final String HEALTH_CHECKER = "healthChecker";
    
    /** 实例 IP 字段。 */
    public static final String IP = "ip";
    
    /** 实例端口字段。 */
    public static final String PORT = "port";
    
    /** 集群名字段。 */
    public static final String CLUSTER_NAME = "clusterName";
    
    /** 服务名字段。 */
    public static final String SERVICE_NAME = "serviceName";
    
    /** 是否临时实例字段。 */
    public static final String EPHEMERAL = "ephemeral";
    
    /** 状态列表字段。 */
    public static final String STATUSES = "statuses";
    
    /** 客户端 IP 字段。 */
    public static final String CLIENT_IP = "clientIP";
    
    /** 服务端状态字段。 */
    public static final String SERVICE_STATUS = "serverStatus";
    
    /** 编码方式字段。 */
    public static final String ENCODING = "encoding";
    
    /** 不修正 IP 字段标识。 */
    public static final String NOFIX = "nofix";
    
}
