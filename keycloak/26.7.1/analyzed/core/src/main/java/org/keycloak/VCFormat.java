/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak;


/**
 * 可验证凭证（VC）支持的格式常量，对应 OpenID 可验证凭证签发规范中的多种编码方式。
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public interface VCFormat {
    /**
     * LD 凭证格式（Linked Data Credentials）。
     * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-vc-secured-using-data-integ}
     */
    String LDP_VC = "ldp_vc";

    /**
     * JWT 编码的可验证凭证格式。
     * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-w3c-verifiable-credentials}
     */
    String JWT_VC = "jwt_vc_json";

    /**
     * SD-JWT 可验证凭证格式（选择性披露 JWT）。
     * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-ietf-sd-jwt-vc}
     */
    String SD_JWT_VC = "dc+sd-jwt";

    /** 当前 Keycloak 支持的 VC 格式列表（JWT 与 SD-JWT）。 */
    String[] SUPPORTED_FORMATS = new String[]{JWT_VC, SD_JWT_VC};

    /**
     * 根据 OAuth scope 后缀推断应使用的 VC 格式；默认 SD-JWT。
     *
     * @param scope 授权 scope 字符串
     * @return 对应的 VC 格式标识
     */
    static String getFromScope(String scope) {
        String format = SD_JWT_VC; // 默认格式
        if (scope.toLowerCase().endsWith("_jwt")) format = JWT_VC;
        return format;
    }

    /**
     * 由 VC 格式值反推 scope 后缀（{@code _jwt} 或 {@code _sd}）。
     *
     * @param value VC 格式常量
     * @return scope 后缀，无匹配时为空字符串
     */
    static String getScopeSuffix(String value) {
        String suffix = "";
        if (JWT_VC.equals(value)) suffix = "_jwt";
        else if (SD_JWT_VC.equals(value)) suffix = "_sd";
        return suffix;
    }
}
