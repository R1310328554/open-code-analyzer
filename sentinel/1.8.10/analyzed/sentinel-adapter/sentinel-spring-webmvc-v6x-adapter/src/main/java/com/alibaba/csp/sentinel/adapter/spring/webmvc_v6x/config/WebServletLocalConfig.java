/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Web Servlet 适配器配置中心（已移植至 Spring Web 适配器）。
 *
 * @since 1.8.8
 */
public final class WebServletLocalConfig {

    public static final String BLOCK_PAGE_URL_CONF_KEY = "csp.sentinel.web.servlet.block.page";
    public static final String BLOCK_PAGE_HTTP_STATUS_CONF_KEY = "csp.sentinel.web.servlet.block.status";
    public static final String BLOCK_PAGE_ALLOW_ORIGINS_CONF_KEY = "csp.sentinel.web.servlet.block.cors-allow-origins";

    private static final int HTTP_STATUS_TOO_MANY_REQUESTS = 429;

    /**
     * 获取被 Sentinel 流控阻断时的跳转页面 URL。
     *
     * @return the block page URL, maybe null if not configured.
     */
    public static String getBlockPage() {
        return SentinelConfig.getConfig(BLOCK_PAGE_URL_CONF_KEY);
    }

    public static void setBlockPage(String blockPage) {
        SentinelConfig.setConfig(BLOCK_PAGE_URL_CONF_KEY, blockPage);
    }

    /**
     * <p>获取使用默认阻断页时的 HTTP 状态码。</p>
     * <p>可通过 {@code -Dcsp.sentinel.web.servlet.block.status}
     * 属性设置状态码；当属性为空或无效时，Sentinel 默认使用 429（Too Many Requests）。</p>
     *
     * @return the HTTP status of the default block page
     */
    public static int getBlockPageHttpStatus() {
        String value = SentinelConfig.getConfig(BLOCK_PAGE_HTTP_STATUS_CONF_KEY);
        if (StringUtil.isEmpty(value)) {
            return HTTP_STATUS_TOO_MANY_REQUESTS;
        }
        try {
            int s = Integer.parseInt(value);
            if (s <= 0) {
                throw new IllegalArgumentException("Invalid status code: " + s);
            }
            return s;
        } catch (Exception e) {
            RecordLog.warn("[WebServletConfig] Invalid block HTTP status (" + value + "), using default 429");
            setBlockPageHttpStatus(HTTP_STATUS_TOO_MANY_REQUESTS);
        }
        return HTTP_STATUS_TOO_MANY_REQUESTS;
    }

    /**
     * 设置默认阻断页的 HTTP 状态码。
     *
     * @param httpStatus the HTTP status of the default block page
     */
    public static void setBlockPageHttpStatus(int httpStatus) {
        if (httpStatus <= 0) {
            throw new IllegalArgumentException("Invalid HTTP status code: " + httpStatus);
        }
        SentinelConfig.setConfig(BLOCK_PAGE_HTTP_STATUS_CONF_KEY, String.valueOf(httpStatus));
    }

    private WebServletLocalConfig() {}
}
