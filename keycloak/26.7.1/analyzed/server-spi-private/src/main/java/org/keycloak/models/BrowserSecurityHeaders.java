/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 浏览器安全 HTTP 响应头枚举：领域可配置键、标准头名与默认值。
 * <p>由 {@link RealmModel} 属性覆盖，{@link #realmDefaultHeaders} 提供启动默认值映射。</p>
 */
public enum BrowserSecurityHeaders {

    /** 防点击劫持：限制 iframe 嵌入来源。 */
    X_FRAME_OPTIONS("xFrameOptions", "X-Frame-Options", "SAMEORIGIN"),
    /** 内容安全策略（CSP），限制脚本与资源加载来源。 */
    CONTENT_SECURITY_POLICY("contentSecurityPolicy", "Content-Security-Policy", ContentSecurityPolicyBuilder.create().build()),
    /** CSP 仅报告模式，不阻断违规资源。 */
    CONTENT_SECURITY_POLICY_REPORT_ONLY("contentSecurityPolicyReportOnly", "Content-Security-Policy-Report-Only", ""),
    /** 禁止 MIME 类型嗅探。 */
    X_CONTENT_TYPE_OPTIONS("xContentTypeOptions", "X-Content-Type-Options", "nosniff"),
    /** 指示搜索引擎不索引/不跟踪页面。 */
    X_ROBOTS_TAG("xRobotsTag", "X-Robots-Tag", "none"),
    /** 强制 HTTPS（HSTS）。 */
    STRICT_TRANSPORT_SECURITY("strictTransportSecurity", "Strict-Transport-Security", "max-age=31536000; includeSubDomains"),
    /** Referer 头泄露策略。 */
    REFERRER_POLICY("referrerPolicy", "Referrer-Policy", "no-referrer");

    private final String key;
    private final String headerName;
    private final String defaultValue;

    BrowserSecurityHeaders(String key, String headerName, String defaultValue) {
        this.key = key;
        this.headerName = headerName;
        this.defaultValue = defaultValue;
    }

    /** 领域属性存储键。 */
    public String getKey() {
        return key;
    }

    /** HTTP 响应头名称。 */
    public String getHeaderName() {
        return headerName;
    }

    /** 未在领域中覆盖时的默认头值。 */
    public String getDefaultValue() {
        return defaultValue;
    }

    /** 领域默认安全头键值映射（已弃用）。 */
    @Deprecated // should be removed eventually
    public static final Map<String, String> realmDefaultHeaders;

    static {

        Map<String, String> dh = new HashMap<>();
        dh.put(X_FRAME_OPTIONS.getKey(), X_FRAME_OPTIONS.getDefaultValue());
        dh.put(CONTENT_SECURITY_POLICY.getKey(), CONTENT_SECURITY_POLICY.getDefaultValue());
        dh.put(CONTENT_SECURITY_POLICY_REPORT_ONLY.getKey(), CONTENT_SECURITY_POLICY_REPORT_ONLY.getDefaultValue());
        dh.put(X_CONTENT_TYPE_OPTIONS.getKey(), X_CONTENT_TYPE_OPTIONS.getDefaultValue());
        dh.put(X_ROBOTS_TAG.getKey(), X_ROBOTS_TAG.getDefaultValue());
        dh.put(STRICT_TRANSPORT_SECURITY.getKey(), STRICT_TRANSPORT_SECURITY.getDefaultValue());
        dh.put(REFERRER_POLICY.getKey(), REFERRER_POLICY.getDefaultValue());

        realmDefaultHeaders = Collections.unmodifiableMap(dh);
    }
}
