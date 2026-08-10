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
package org.keycloak.social.linkedin;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.crypto.PublicKeysWrapper;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.keys.PublicKeyLoader;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.JWKSHttpUtils;
import org.keycloak.util.JWKSUtils;

/**
 * LinkedIn 专用公钥加载器。
 * <p>LinkedIn OpenID Connect 的
 * <a href="https://www.linkedin.com/oauth/openid/jwks">JWKS 端点</a>
 * 未在密钥中声明必需的 {@code use} 字段，本加载器假定请求用途即为签名密钥。</p>
 *
 * @author rmartinc
 */
public class LinkedInPublicKeyLoader implements PublicKeyLoader {

    private final KeycloakSession session;
    private final OIDCIdentityProviderConfig config;

    /** 构造 LinkedIn 公钥加载器。 */
    public LinkedInPublicKeyLoader(KeycloakSession session, OIDCIdentityProviderConfig config) {
        this.session = session;
        this.config = config;
    }

    /**
     * 从 IdP 配置的 JWKS URL 拉取密钥并按签名用途解析。
     * <p>第二个参数 {@code true} 表示在缺少 use 声明时仍按 SIG 用途匹配。</p>
     */
    @Override
    public PublicKeysWrapper loadKeys() throws Exception {
        String jwksUrl = config.getJwksUrl();
        JSONWebKeySet jwks = JWKSHttpUtils.sendJwksRequest(session, jwksUrl);
        return JWKSUtils.getKeyWrappersForUse(jwks, JWK.Use.SIG, true);
    }
}
