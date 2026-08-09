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

package com.alibaba.csp.sentinel.adapter.gateway.zuul2.constants;

/**
 * Zuul2 适配器相关常量定义。
 *
 * @author wavesZh
 */
public class SentinelZuul2Constants {
    /**
     * 当 routeId 为空时使用的默认入口（上下文）名称。
     */
    public static final String ZUUL_DEFAULT_CONTEXT = "zuul2_default_context";
    /**
     * 在 Zuul 上下文中保存 Sentinel Entry 的键名。
     */
    public static final String ZUUL_CTX_SENTINEL_ENTRIES_KEY = "_sentinel_entries";

    public static final String ZUUL_CTX_SENTINEL_FALLBACK_ROUTE = "_sentinel_fallback_route";
    /**
     * 标记请求是否已被 Sentinel 拦截。
     */
    public static final String ZUUL_CTX_SENTINEL_BLOCKED_FLAG = "_sentinel_blocked_flag";

    private SentinelZuul2Constants() {}
}
