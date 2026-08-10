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

package com.alibaba.nacos.cmdb.utils;

/**
 * Utils and constants.
 * <p>CMDB HTTP API 路径常量：基于 Nacos 服务端 v1 前缀拼接 CMDB 上下文根路径。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
public class UtilsAndCommons {
    
    /** Nacos Open API 版本前缀 */
    private static final String NACOS_SERVER_VERSION = "/v1";
    
    /** CMDB REST 根路径，例如 {@code /v1/cmdb} */
    public static final String NACOS_CMDB_CONTEXT = NACOS_SERVER_VERSION + "/cmdb";
    
}
