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

import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.keys.PublicKeyLoader;
import org.keycloak.keys.PublicKeyStorageProvider;
import org.keycloak.keys.PublicKeyStorageUtils;
import org.keycloak.models.KeycloakSession;

/**
 * LinkedIn OpenID Connect 社交身份提供者。
 * <p>专用于 <b>Sign In with LinkedIn using OpenID Connect</b> 产品应用。</p>
 *
 * @author rmartinc
 */
public class LinkedInOIDCIdentityProvider extends OIDCIdentityProvider implements SocialIdentityProvider<OIDCIdentityProviderConfig> {

    /** 默认 OIDC scope 集合。 */
    public static final String DEFAULT_SCOPE = "openid profile email";

    /** 构造 LinkedIn OIDC IdP 实例。 */
    public LinkedInOIDCIdentityProvider(KeycloakSession session, OIDCIdentityProviderConfig config) {
        super(session, config);
    }

    /** 返回默认 OIDC scope。 */
    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    /**
     * 获取 IdP 签名验证公钥。
     * <p>LinkedIn JWKS 未声明 {@code use} 字段，需使用 {@link LinkedInPublicKeyLoader} 加载。</p>
     */
    @Override
    protected KeyWrapper getIdentityProviderKeyWrapper(JWSInput jws) {
        // 兼容 JWKS 中缺少 use=sig 声明的 LinkedIn 公钥
        PublicKeyLoader loader = new LinkedInPublicKeyLoader(session, getConfig());
        PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
        String modelKey = PublicKeyStorageUtils.getIdpModelCacheKey(session.getContext().getRealm().getId(), getConfig().getInternalId());
        return keyStorage.getPublicKey(modelKey, jws.getHeader().getKeyId(), jws.getHeader().getRawAlgorithm(), loader);
    }
}
