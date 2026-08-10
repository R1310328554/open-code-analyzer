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
package org.keycloak.protocol.oid4vc.model;

import org.keycloak.jose.jwk.JSONWebKeySet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JWT VC 签发者元数据，对应端点 {@code /.well-known/jwt-vc-issuer}。
 * <p>声明签发者标识及用于验证凭证签名的 JWK 集合。</p>
 * {@see https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-03.html#name-jwt-vc-issuer-metadata}
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JWTVCIssuerMetadata {
    /** 签发者标识 URI。 */
    @JsonProperty("issuer")
    private String issuer;
    /** 验证凭证签名用的 JWK 集合。 */
    @JsonProperty("jwks")
    private JSONWebKeySet jwks;

    /** @return 签发者 URI */
    public String getIssuer() {
        return issuer;
    }

    /** @param issuer 签发者 URI */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /** @return JWK 集合 */
    public JSONWebKeySet getJwks() {
        return jwks;
    }

    /** @param jwks JWK 集合 */
    public void setJwks(JSONWebKeySet jwks) {
        this.jwks = jwks;
    }
}
