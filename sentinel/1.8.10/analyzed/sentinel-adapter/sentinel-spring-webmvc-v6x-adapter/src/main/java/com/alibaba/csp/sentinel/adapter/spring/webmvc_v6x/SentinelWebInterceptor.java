/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config.SentinelWebMvcConfig;

import com.alibaba.csp.sentinel.adapter.web.common.UrlCleaner;
import com.alibaba.csp.sentinel.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.HandlerMapping;

/**
 * 与 Sentinel 集成的 Spring Web MVC 拦截器。
 * <p>
 * 资源名将记录为 {@code ${uri}} 形式。
 *
 * @since 1.8.8
 */
public class SentinelWebInterceptor extends AbstractSentinelInterceptor {

    private final SentinelWebMvcConfig config;

    public SentinelWebInterceptor() {
        this(new SentinelWebMvcConfig());
    }

    public SentinelWebInterceptor(SentinelWebMvcConfig config) {
        super(config);
        if (config == null) {
            // 默认使用内置默认配置。
            this.config = new SentinelWebMvcConfig();
        } else {
            this.config = config;
        }
    }

    @Override
    protected String getResourceName(HttpServletRequest request) {
        // 从请求属性中解析 Spring Web URL 匹配模式。
        Object resourceNameObject = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (resourceNameObject == null || !(resourceNameObject instanceof String)) {
            return null;
        }
        String resourceName = (String) resourceNameObject;
        UrlCleaner urlCleaner = config.getUrlCleaner();
        if (urlCleaner != null) {
            resourceName = urlCleaner.clean(resourceName);
        }
        if (config.isContextPathSpecify() && request.getContextPath() != null) {
            resourceName = request.getContextPath() + resourceName;
        }
        if (StringUtil.isNotEmpty(resourceName) && config.isHttpMethodSpecify()) {
            resourceName = request.getMethod().toUpperCase() + ":" + resourceName;
        }
        return resourceName;
    }

    @Override
    protected String getContextName(HttpServletRequest request) {
        if (config.isWebContextUnify()) {
            return super.getContextName(request);
        }

        return getResourceName(request);
    }
}
