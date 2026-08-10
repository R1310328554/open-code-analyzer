/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.representations.dpop;

import org.keycloak.representations.JsonWebToken;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DPoP（Demonstrating Proof-of-Possession）证明 JWT，继承 {@link JsonWebToken}。
 * <p>
 * 携带 HTTP 方法（htm）、URI（htu）及关联访问令牌哈希（ath），用于绑定令牌与客户端密钥对。
 *
 * @author <a href="mailto:dmitryt@backbase.com">Dmitry Telegin</a>
 */
public class DPoP extends JsonWebToken {

    /** 访问令牌哈希声明键（ath）。 */
    private static final String ATH = "ath";
    /** HTTP 方法声明键（htm）。 */
    private static final String HTM = "htm";
    /** HTTP URI 声明键（htu）。 */
    private static final String HTU = "htu";

    /** 关联访问令牌的哈希值（ath 声明）。 */
    @JsonProperty(ATH)
    private String accessTokenHash;

    /** 请求 HTTP 方法（htm 声明，如 GET、POST）。 */
    @JsonProperty(HTM)
    private String httpMethod;

    /** 请求 HTTP URI（htu 声明）。 */
    @JsonProperty(HTU)
    private String httpUri;

    /** 签名 JWK 的 thumbprint（运行时填充，非 JWT 声明）。 */
    private String thumbprint;

    public String getAccessTokenHash() {
        return accessTokenHash;
    }
    public void setAccessTokenHash(String accessTokenHash) {
        this.accessTokenHash = accessTokenHash;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getHttpUri() {
        return httpUri;
    }

    public void setHttpUri(String httpUri) {
        this.httpUri = httpUri;
    }

    public String getThumbprint() {
        return thumbprint;
    }

    public void setThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
    }

}
