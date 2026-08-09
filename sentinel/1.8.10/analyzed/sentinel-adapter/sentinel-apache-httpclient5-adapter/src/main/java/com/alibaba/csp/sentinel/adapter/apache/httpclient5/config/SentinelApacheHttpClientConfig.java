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
package com.alibaba.csp.sentinel.adapter.apache.httpclient5.config;

import com.alibaba.csp.sentinel.adapter.apache.httpclient5.extractor.ApacheHttpClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.apache.httpclient5.extractor.DefaultApacheHttpClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.apache.httpclient5.fallback.ApacheHttpClientFallback;
import com.alibaba.csp.sentinel.adapter.apache.httpclient5.fallback.DefaultApacheHttpClientFallback;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * Apache HttpClient 5.x Sentinel 适配器配置：资源名前缀、提取器与降级处理器。
 *
 * @author uuuyuqi
 */
public class SentinelApacheHttpClientConfig {

    /** Sentinel 资源名前缀，默认 {@code httpclient:}。 */
    private String prefix = "httpclient:";
    private ApacheHttpClientResourceExtractor extractor = new DefaultApacheHttpClientResourceExtractor();
    private ApacheHttpClientFallback fallback = new DefaultApacheHttpClientFallback();

    /** 获取资源名前缀。 */
    public String getPrefix() {
        return prefix;
    }

    /** 设置资源名前缀。 */
    public void setPrefix(String prefix) {
        AssertUtil.notNull(prefix, "prefix cannot be null");
        this.prefix = prefix;
    }

    /** 获取资源名提取器。 */
    public ApacheHttpClientResourceExtractor getExtractor() {
        return extractor;
    }

    /** 设置资源名提取器。 */
    public void setExtractor(ApacheHttpClientResourceExtractor extractor) {
        AssertUtil.notNull(extractor, "extractor cannot be null");
        this.extractor = extractor;
    }

    /** 获取降级处理器。 */
    public ApacheHttpClientFallback getFallback() {
        return fallback;
    }

    /** 设置降级处理器。 */
    public void setFallback(ApacheHttpClientFallback fallback) {
        AssertUtil.notNull(fallback, "fallback cannot be null");
        this.fallback = fallback;
    }
}
