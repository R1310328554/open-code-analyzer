/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.broker.trust;

import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.util.Strings;

import static org.keycloak.broker.jwtauthorizationgrant.JWTAuthorizationGrantConfig.PUBLIC_KEY_SIGNATURE_VERIFIER;
import static org.keycloak.broker.jwtauthorizationgrant.JWTAuthorizationGrantConfig.PUBLIC_KEY_SIGNATURE_VERIFIER_KEY_ID;
import static org.keycloak.broker.oidc.OIDCIdentityProviderConfig.JWKS_URL;
import static org.keycloak.broker.oidc.OIDCIdentityProviderConfig.USE_JWKS_URL;
import static org.keycloak.common.util.UriUtils.checkUrl;

/**
 * 默认信任 IdP 配置：复用 OIDC JWKS URL 与 JWT 授权公钥字段，
 * 校验 realm SSL 要求并隐藏登录页入口。
 */
public class DefaultTrustIdentityProviderConfig extends IdentityProviderModel {

    /** 配置键：受信任 JWKS 端点 URL。 */
    public static final String TRUSTED_JWKS_URL = JWKS_URL;
    /** 配置键：内嵌 JWKS 或 PEM 公钥材料。 */
    public static final String TRUSTED_JWKS = PUBLIC_KEY_SIGNATURE_VERIFIER;
    /** 配置键：硬编码公钥对应的 key id。 */
    public static final String TRUSTED_JWKS_KEY_ID = PUBLIC_KEY_SIGNATURE_VERIFIER_KEY_ID;

    public DefaultTrustIdentityProviderConfig() {
    }

    public DefaultTrustIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /** 信任 IdP 不参与用户登录流程，始终隐藏。 */
    @Override
    public Boolean isHideOnLogin() {
        return true;
    }

    /** 校验 JWKS URL 或静态公钥至少配置一项，并检查 URL SSL 策略。 */
    @Override
    public void validate(RealmModel realm) {
        super.validate(realm);
        boolean hasTrustedJwksUrl = !Strings.isEmpty(getTrustedJwksUrl());
        boolean hasTrustedJwks = !Strings.isEmpty(getTrustedJwks());
        if (isUseJwksUrl()) {
            if (!hasTrustedJwksUrl) {
                throw new IllegalArgumentException("JWKS URL is required when 'Use JWKS URL' is enabled");
            }
        } else if (!hasTrustedJwks) {
            throw new IllegalArgumentException("Validating public key is required when 'Use JWKS URL' is disabled");
        }
        if (isUseJwksUrl()) {
            checkUrl(realm.getSslRequired(), getTrustedJwksUrl(), TRUSTED_JWKS_URL);
        }
    }

    /** @return 是否通过 HTTP JWKS URL 获取公钥 */
    public boolean isUseJwksUrl() {
        String useJwksUrl = getConfig().get(USE_JWKS_URL);
        return useJwksUrl == null ? !Strings.isEmpty(getTrustedJwksUrl())
                : Boolean.parseBoolean(useJwksUrl);
    }

    /** @return 受信任 JWKS URL */
    public String getTrustedJwksUrl() {
        return getConfig().get(TRUSTED_JWKS_URL);
    }

    /** 设置或清除 JWKS URL 配置。 */
    public void setTrustedJwksUrl(String trustedJwksUrl) {
        if (trustedJwksUrl == null) {
            getConfig().remove(TRUSTED_JWKS_URL);
        } else {
            getConfig().put(TRUSTED_JWKS_URL, trustedJwksUrl);
        }
    }

    /** @return 内嵌 JWKS/PEM 公钥字符串 */
    public String getTrustedJwks() {
        return getConfig().get(TRUSTED_JWKS);
    }

    /** 设置或清除静态公钥材料。 */
    public void setTrustedJwks(String trustedJwks) {
        if (trustedJwks == null) {
            getConfig().remove(TRUSTED_JWKS);
        } else {
            getConfig().put(TRUSTED_JWKS, trustedJwks);
        }
    }

    /** @return 硬编码公钥 kid，可为 null */
    public String getTrustedJwksKeyId() {
        return getConfig().get(TRUSTED_JWKS_KEY_ID);
    }
}
