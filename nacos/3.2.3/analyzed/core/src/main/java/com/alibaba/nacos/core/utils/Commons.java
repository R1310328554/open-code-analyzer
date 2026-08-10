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

package com.alibaba.nacos.core.utils;

/**
 * Core 模块 HTTP 上下文路径与 API 版本常量。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class Commons {
    
    /** Nacos 服务端根上下文路径。 */
    public static final String NACOS_SERVER_CONTEXT = "/nacos";
    
    /** Open API v1 版本前缀。 */
    public static final String NACOS_SERVER_VERSION = "/v1";
    
    /** Open API v2 版本前缀。 */
    public static final String NACOS_SERVER_VERSION_V2 = "/v2";
    
    /** Open API v3（Admin）版本前缀。 */
    public static final String NACOS_SERVER_VERSION_V3 = "/v3";
    
    /** 默认 Core 模块 v1 上下文：{@code /v1/core}。 */
    public static final String DEFAULT_NACOS_CORE_CONTEXT = NACOS_SERVER_VERSION + "/core";
    
    /** Core 模块 v1 上下文（与默认相同）。 */
    public static final String NACOS_CORE_CONTEXT = DEFAULT_NACOS_CORE_CONTEXT;
    
    /** Core 模块 v2 上下文：{@code /v2/core}。 */
    public static final String NACOS_CORE_CONTEXT_V2 = NACOS_SERVER_VERSION_V2 + "/core";
    
    /** Admin Core 模块 v3 上下文：{@code /v3/admin/core}。 */
    public static final String NACOS_ADMIN_CORE_CONTEXT_V3 =
        NACOS_SERVER_VERSION_V3 + "/admin/core";
    
}
