package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config;

import com.alibaba.csp.sentinel.adapter.web.common.UrlCleaner;

/**
 * Sentinel 前置 Web MVC 拦截器配置（在 HandlerMapping 之前执行）。
 *
 * @since 1.8.8
 */
public class SentinelPreWebMvcConfig extends BaseWebMvcConfig {

    public static final String DEFAULT_REQUEST_ATTRIBUTE_NAME = "$$sentinel_pre_spring_web_entry_attr";

    private UrlCleaner urlCleaner;

    /**
     * 是否在 URL 资源名中包含 HTTP 方法前缀（如 {@code POST:}）。
     */
    private boolean httpMethodSpecify;

    /**
     * 是否统一 Web 上下文（即使用默认上下文名），默认为 true。
     *
     * @since 1.7.2
     */
    private boolean webContextUnify = true;

    public SentinelPreWebMvcConfig() {
        super();
        setRequestAttributeName(DEFAULT_REQUEST_ATTRIBUTE_NAME);
    }

    public boolean isHttpMethodSpecify() {
        return httpMethodSpecify;
    }

    public SentinelPreWebMvcConfig setHttpMethodSpecify(boolean httpMethodSpecify) {
        this.httpMethodSpecify = httpMethodSpecify;
        return this;
    }

    public boolean isWebContextUnify() {
        return webContextUnify;
    }

    public SentinelPreWebMvcConfig setWebContextUnify(boolean webContextUnify) {
        this.webContextUnify = webContextUnify;
        return this;
    }

    public UrlCleaner getUrlCleaner() {
        return urlCleaner;
    }

    public SentinelPreWebMvcConfig setUrlCleaner(UrlCleaner urlCleaner) {
        this.urlCleaner = urlCleaner;
        return this;
    }
}
