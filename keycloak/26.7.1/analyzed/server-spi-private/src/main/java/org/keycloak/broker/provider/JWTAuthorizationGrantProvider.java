/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.broker.provider;
import java.util.List;

import org.keycloak.models.IdentityProviderModel;
import org.keycloak.protocol.oidc.JWTAuthorizationGrantValidationContext;

/**
 * JWT 授权授予型身份提供方：验证外部 JWT 断言并建立联邦身份。
 */
public interface JWTAuthorizationGrantProvider <C extends IdentityProviderModel> extends IdentityProvider<C> {

    /** 校验 JWT 授权授予断言并返回 {@link BrokeredIdentityContext}。 */
    BrokeredIdentityContext validateAuthorizationGrantAssertion(JWTAuthorizationGrantValidationContext assertion) throws IdentityBrokerException;

    /** 允许的时钟偏差（秒）。 */
    int getAllowedClockSkew();

    /** 是否允许重复使用同一 JWT 断言。 */
    boolean isAssertionReuseAllowed();

    /**
     * 返回允许的 audience 列表；断言 audience 命中其一即视为有效。
     *
     * @return list of allowed audience values. JWT assertion is considered valid if it's audience is one of the audiences returned from this method
     */
    List<String> getAllowedAudienceForJWTGrant();

    /** JWT 断言允许的最大过期时间（秒）。 */
    int getMaxAllowedExpiration();

    /** 断言签名算法标识。 */
    String getAssertionSignatureAlg();

    /** 是否限制由此流程签发的访问令牌过期时间。 */
    boolean isLimitAccessTokenExpiration();
}
