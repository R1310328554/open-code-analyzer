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
package com.alibaba.csp.sentinel.adapter.sofa.rpc.fallback;

import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * SOFARPC 适配器全局降级处理器注册表。
 *
 * @author cdfive
 */
public final class SofaRpcFallbackRegistry {

    private static volatile SofaRpcFallback providerFallback = new DefaultSofaRpcFallback();
    private static volatile SofaRpcFallback consumerFallback = new DefaultSofaRpcFallback();

    /**
     * 获取 Provider 侧降级处理器。
     *
     * @return Provider 降级处理器
     */
    public static SofaRpcFallback getProviderFallback() {
        return providerFallback;
    }

    /**
     * 设置 Provider 侧降级处理器。
     *
     * @param providerFallback Provider 降级处理器
     */
    public static void setProviderFallback(SofaRpcFallback providerFallback) {
        AssertUtil.notNull(providerFallback, "providerFallback cannot be null");
        SofaRpcFallbackRegistry.providerFallback = providerFallback;
    }

    /**
     * 获取 Consumer 侧降级处理器。
     *
     * @return Consumer 降级处理器
     */
    public static SofaRpcFallback getConsumerFallback() {
        return consumerFallback;
    }

    /**
     * 设置 Consumer 侧降级处理器。
     *
     * @param consumerFallback Consumer 降级处理器
     */
    public static void setConsumerFallback(SofaRpcFallback consumerFallback) {
        AssertUtil.notNull(consumerFallback, "consumerFallback cannot be null");
        SofaRpcFallbackRegistry.consumerFallback = consumerFallback;
    }

    private SofaRpcFallbackRegistry() {}
}

