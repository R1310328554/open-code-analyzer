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

package com.alibaba.nacos.api.naming;

/**
 * 服务发现模块 HTTP/Open API 常用请求参数名常量。
 *
 * <p>客户端与控制台在调用命名服务 REST 接口时，使用这些键作为 query/form 字段名。</p>
 *
 * @author nkorange
 * @since 1.0.0
 */
public class CommonParams {
    
    /** 响应/请求中的业务码字段名。 */
    public static final String CODE = "code";
    
    /** 服务名字段名。 */
    public static final String SERVICE_NAME = "serviceName";
    
    /** 集群名字段名。 */
    public static final String CLUSTER_NAME = "clusterName";
    
    /** 命名空间 ID 字段名。 */
    public static final String NAMESPACE_ID = "namespaceId";
    
    /** 分组名字段名。 */
    public static final String GROUP_NAME = "groupName";
    
    /** 是否启用轻量心跳的参数字段名。 */
    public static final String LIGHT_BEAT_ENABLED = "lightBeatEnabled";
    
    /** 命名请求超时（毫秒）参数字段名。 */
    public static final String NAMING_REQUEST_TIMEOUT = "namingRequestTimeout";
}
