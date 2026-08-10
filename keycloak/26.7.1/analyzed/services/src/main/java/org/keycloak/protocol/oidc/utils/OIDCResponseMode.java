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

package org.keycloak.protocol.oidc.utils;

/**
 * OIDC 授权响应模式枚举：query、fragment、form_post 及对应 JARM JWT 变体。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum OIDCResponseMode {

    /** 参数置于 query string */
    QUERY("query"),
    /** JARM 简写，按 response_type 选择默认 JWT 模式 */
    JWT("jwt"),
    /** 参数置于 URI fragment */
    FRAGMENT("fragment"),
    /** 通过 HTML 表单 POST 回传参数 */
    FORM_POST("form_post"),
    /** JWT 响应置于 query 参数 response */
    QUERY_JWT("query.jwt"),
    /** JWT 响应置于 fragment */
    FRAGMENT_JWT("fragment.jwt"),
    /** JWT 响应通过 form_post 提交 */
    FORM_POST_JWT("form_post.jwt");

    private String value;

    OIDCResponseMode(String v) {
        value = v;
    }

    /**
     * 解析 response_mode 参数；null 时按 response_type 选择默认模式，值为 jwt 时选择默认 JARM 模式。
     * @param responseMode 请求中的 response_mode
     * @param responseType 已解析的 response_type
     */
        if (responseMode == null) {
            return getDefaultResponseMode(responseType);
        } else if(responseMode.equals("jwt")) {
            return getDefaultJarmResponseMode(responseType);
        } else {
            return fromValue(responseMode);
        }
    }

    /** response_type 无效时的宽松解析，无法识别时回退 QUERY/QUERY_JWT */
    public static OIDCResponseMode parseWhenInvalidResponseType(String responseMode) {
        if (responseMode == null) {
            return OIDCResponseMode.QUERY;
        } else if(responseMode.equals("jwt")) {
            return OIDCResponseMode.QUERY_JWT;
        } else {
            for (OIDCResponseMode c : OIDCResponseMode.values()) {
                if (c.value.equals(responseMode)) {
                    return c;
                }
            }
            return OIDCResponseMode.QUERY;
        }
    }

    /** @return response_mode 字符串值 */
    public String value() {
        return value;
    }

    private static OIDCResponseMode fromValue(String v) {
        for (OIDCResponseMode c : OIDCResponseMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    private static OIDCResponseMode getDefaultResponseMode(OIDCResponseType responseType) {
        if (responseType.isImplicitOrHybridFlow()) {
            return OIDCResponseMode.FRAGMENT;
        } else {
            return OIDCResponseMode.QUERY;
        }
    }

    private static OIDCResponseMode getDefaultJarmResponseMode(OIDCResponseType responseType) {
        if (responseType.isImplicitOrHybridFlow()) {
            return OIDCResponseMode.FRAGMENT_JWT;
        } else {
            return OIDCResponseMode.QUERY_JWT;
        }
    }
}
