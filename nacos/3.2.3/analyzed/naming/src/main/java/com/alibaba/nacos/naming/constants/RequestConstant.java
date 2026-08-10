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

package com.alibaba.nacos.naming.constants;

/**
 * 命名 HTTP 请求参数字段常量。
 *
 * <p>定义健康检查、有效性及实例地址等通用请求键。</p>
 *
 * @author nacos
 */
public class RequestConstant {
    
    /** 实例健康状态请求键。 */
    public static final String HEALTHY_KEY = "healthy";
    
    /** 实例有效性请求键。 */
    public static final String VALID_KEY = "valid";
    
    /** 实例 IP 请求键。 */
    public static final String IP_KEY = "ip";
    
    /** 实例端口请求键。 */
    public static final String PORT_KEY = "port";
    
}
