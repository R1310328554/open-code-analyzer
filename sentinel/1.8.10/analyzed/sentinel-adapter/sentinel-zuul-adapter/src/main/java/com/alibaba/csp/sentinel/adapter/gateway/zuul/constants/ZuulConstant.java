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

package com.alibaba.csp.sentinel.adapter.gateway.zuul.constants;

import com.netflix.zuul.ZuulFilter;

/**
 * Zuul 适配器常量定义，包含 {@link com.netflix.zuul.context.RequestContext} 键名与过滤器类型。
 *
 * @author tiger
 */
public class ZuulConstant {

    /**
     * 负载均衡使用的 Zuul {@link com.netflix.zuul.context.RequestContext} 键（服务 ID）。
     */
    public static final String SERVICE_ID_KEY = "serviceId";
    /**
     * 代理路由使用的 Zuul {@link com.netflix.zuul.context.RequestContext} 键（路由 ID）。
     */
    public static final String PROXY_ID_KEY = "proxy";

    /**
     * {@link ZuulFilter#filterType()} 错误类型。
     */
    public static final String ERROR_TYPE = "error";

    /**
     * {@link ZuulFilter#filterType()} 后置类型。
     */
    public static final String POST_TYPE = "post";

    /**
     * {@link ZuulFilter#filterType()} 前置类型。
     */
    public static final String PRE_TYPE = "pre";

    /**
     * {@link ZuulFilter#filterType()} 路由类型。
     */
    public static final String ROUTE_TYPE = "route";

    /**
     * 发送响应过滤器的执行顺序。
     */
    public static final int SEND_RESPONSE_FILTER_ORDER = 1000;

    /**
     * 当 serviceId 为空时，Zuul 使用的 Sentinel 默认上下文名。
     */
    public static final String ZUUL_DEFAULT_CONTEXT = "zuul_default_context";

    /**
     * 在 Zuul 上下文中保存 Sentinel Entry 的键名。
     *
     * @since 1.6.0
     */
    public static final String ZUUL_CTX_SENTINEL_ENTRIES_KEY = "_sentinel_entries";

    private ZuulConstant(){}
}
