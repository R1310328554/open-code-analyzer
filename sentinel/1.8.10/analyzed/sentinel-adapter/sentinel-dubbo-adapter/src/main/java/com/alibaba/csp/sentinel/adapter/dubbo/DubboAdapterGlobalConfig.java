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
package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DefaultDubboFallback;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;
import com.alibaba.csp.sentinel.adapter.dubbo.origin.DefaultDubboOriginParser;
import com.alibaba.csp.sentinel.adapter.dubbo.origin.DubboOriginParser;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * <p>Dubbo 旧版适配器的全局配置与回调注册中心。</p>
 * <p>管理资源名前缀、消费者/提供者降级处理器及调用来源解析器。</p>
 *
 * @author lianglin
 * @author Eric Zhao
 * @since 1.7.0
 */
public final class DubboAdapterGlobalConfig {

    private static final String TRUE_STR = "true";

    /** 是否在资源名中使用前缀的配置键。 */
    public static final String DUBBO_RES_NAME_WITH_PREFIX_KEY = "csp.sentinel.dubbo.resource.use.prefix";
    /** Provider 端资源名前缀配置键。 */
    public static final String DUBBO_PROVIDER_RES_NAME_PREFIX_KEY = "csp.sentinel.dubbo.resource.provider.prefix";
    /** Consumer 端资源名前缀配置键。 */
    public static final String DUBBO_CONSUMER_RES_NAME_PREFIX_KEY = "csp.sentinel.dubbo.resource.consumer.prefix";

    private static final String DEFAULT_DUBBO_PROVIDER_PREFIX = "dubbo:provider:";
    private static final String DEFAULT_DUBBO_CONSUMER_PREFIX = "dubbo:consumer:";

    private static volatile DubboFallback consumerFallback = new DefaultDubboFallback();
    private static volatile DubboFallback providerFallback = new DefaultDubboFallback();
    private static volatile DubboOriginParser originParser = new DefaultDubboOriginParser();

    /** 是否启用资源名前缀。 */
    public static boolean isUsePrefix() {
        return TRUE_STR.equalsIgnoreCase(SentinelConfig.getConfig(DUBBO_RES_NAME_WITH_PREFIX_KEY));
    }

    /** 获取 Provider 端资源名前缀，未启用时返回 null。 */
    public static String getDubboProviderPrefix() {
        if (isUsePrefix()) {
            String config = SentinelConfig.getConfig(DUBBO_PROVIDER_RES_NAME_PREFIX_KEY);
            return StringUtil.isNotBlank(config) ? config : DEFAULT_DUBBO_PROVIDER_PREFIX;
        }
        return null;
    }

    /** 获取 Consumer 端资源名前缀，未启用时返回 null。 */
    public static String getDubboConsumerPrefix() {
        if (isUsePrefix()) {
            String config = SentinelConfig.getConfig(DUBBO_CONSUMER_RES_NAME_PREFIX_KEY);
            return StringUtil.isNotBlank(config) ? config : DEFAULT_DUBBO_CONSUMER_PREFIX;
        }
        return null;
    }

    /** 获取消费者端降级处理器。 */
    public static DubboFallback getConsumerFallback() {
        return consumerFallback;
    }

    /** 设置消费者端降级处理器。 */
    public static void setConsumerFallback(DubboFallback consumerFallback) {
        AssertUtil.notNull(consumerFallback, "consumerFallback cannot be null");
        DubboAdapterGlobalConfig.consumerFallback = consumerFallback;
    }

    /** 获取提供者端降级处理器。 */
    public static DubboFallback getProviderFallback() {
        return providerFallback;
    }

    /** 设置提供者端降级处理器。 */
    public static void setProviderFallback(DubboFallback providerFallback) {
        AssertUtil.notNull(providerFallback, "providerFallback cannot be null");
        DubboAdapterGlobalConfig.providerFallback = providerFallback;
    }

    /**
     * 获取 Dubbo 适配器的调用来源解析器。
     *
     * @return the origin parser
     * @since 1.8.0
     */
    public static DubboOriginParser getOriginParser() {
        return originParser;
    }

    /**
     * 设置 Dubbo 适配器的调用来源解析器。
     *
     * @param originParser the origin parser
     * @since 1.8.0
     */
    public static void setOriginParser(DubboOriginParser originParser) {
        AssertUtil.notNull(originParser, "originParser cannot be null");
        DubboAdapterGlobalConfig.originParser = originParser;
    }

    private DubboAdapterGlobalConfig() {}
}
