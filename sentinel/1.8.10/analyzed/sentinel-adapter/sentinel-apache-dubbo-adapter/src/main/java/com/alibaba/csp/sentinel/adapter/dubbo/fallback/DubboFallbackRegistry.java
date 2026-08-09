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
package com.alibaba.csp.sentinel.adapter.dubbo.fallback;

import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboAdapterGlobalConfig;

/**
 * <p>Dubbo 全局降级注册表（已废弃）。</p>
 *
 * @author Eric Zhao
 * @deprecated use {@link DubboAdapterGlobalConfig} instead since 1.8.0.
 */
@Deprecated
public final class DubboFallbackRegistry {

    /** 获取 Consumer 降级处理器（委托 {@link DubboAdapterGlobalConfig}）。 */
    public static DubboFallback getConsumerFallback() {
        return DubboAdapterGlobalConfig.getConsumerFallback();
    }

    /** 设置 Consumer 降级处理器。 */
    public static void setConsumerFallback(DubboFallback consumerFallback) {
        DubboAdapterGlobalConfig.setConsumerFallback(consumerFallback);
    }

    /** 获取 Provider 降级处理器。 */
    public static DubboFallback getProviderFallback() {
        return DubboAdapterGlobalConfig.getProviderFallback();
    }

    /** 设置 Provider 降级处理器。 */
    public static void setProviderFallback(DubboFallback providerFallback) {
        DubboAdapterGlobalConfig.setProviderFallback(providerFallback);
    }

    private DubboFallbackRegistry() {}
}
