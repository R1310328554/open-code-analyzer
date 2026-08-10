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
package org.keycloak.social.google;

import org.keycloak.broker.jwtauthorizationgrant.JWTAuthorizationGrantConfig;
import org.keycloak.broker.oidc.IssuerValidation;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderType;
import org.keycloak.models.RealmModel;

/**
 * Google IdP 扩展配置。
 * <p>支持 userIp、hostedDomain、offlineAccess 及 JWT Authorization Grant 相关选项。</p>
 *
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class GoogleIdentityProviderConfig extends OIDCIdentityProviderConfig implements JWTAuthorizationGrantConfig, IssuerValidation {

    /** 从 realm 身份提供者模型构造配置。 */
    public GoogleIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /** 创建空配置。 */
    public GoogleIdentityProviderConfig() {
        
    }

    /** 是否在 UserInfo 请求中附加 userIp 参数。 */
    public boolean isUserIp() {
        String userIp = getConfig().get("userIp");
        return userIp == null ? false : Boolean.valueOf(userIp);
    }

    /** 设置是否启用 userIp 参数。 */
    public void setUserIp(boolean ip) {
        getConfig().put("userIp", String.valueOf(ip));
    }

    /** 获取限制的 Google 托管域（hd），空字符串视为 null。 */
    public String getHostedDomain() {
        String hostedDomain = getConfig().get("hostedDomain");

        return hostedDomain == null || hostedDomain.isEmpty() ? null : hostedDomain;
    }

    /** 设置 Google 托管域限制。 */
    public void setHostedDomain(final String hostedDomain) {
        getConfig().put("hostedDomain", hostedDomain);
    }

    /** 是否在授权请求中请求 offline access（refresh token）。 */
    public boolean isOfflineAccess() {
        String offlineAccess = getConfig().get("offlineAccess");
        return offlineAccess == null ? false : Boolean.valueOf(offlineAccess);
    }
    
    /** 设置是否请求 offline access。 */
    public void setOfflineAccess(boolean offlineAccess) {
        getConfig().put("offlineAccess", String.valueOf(offlineAccess));
    }

    /** 返回 JWT Authorization Grant 允许的最大断言过期秒数（默认 3600）。 */
    @Override
    public int getJWTAuthorizationGrantMaxAllowedAssertionExpiration() {
        return Integer.parseInt(getConfig().getOrDefault(JWT_AUTHORIZATION_GRANT_MAX_ALLOWED_ASSERTION_EXPIRATION, "3600"));
    }

    /** 校验 issuer 必须为 Google，并在启用 JWT Grant 时校验 issuer 配置。 */
    @Override
    public void validate(RealmModel realm) {
        if (!GoogleIdentityProvider.ISSUER_URL.equals(getConfig().get(ISSUER))) {
           throw new IllegalArgumentException("The issuer url [" + getConfig().get(ISSUER) + "] is invalid");
        }
        if (isJWTAuthorizationGrantEnabled()) {
            validateIssuer(realm, IdentityProviderType.JWT_AUTHORIZATION_GRANT);
        }
    }
}
