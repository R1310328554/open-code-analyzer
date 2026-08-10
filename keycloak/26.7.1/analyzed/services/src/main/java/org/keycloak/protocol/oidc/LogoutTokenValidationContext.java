/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.protocol.oidc;

import java.util.List;

import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.representations.LogoutToken;

/**
 * Logout Token 校验上下文，携带令牌、状态码及通过校验的 IdP 列表。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LogoutTokenValidationContext {

    /** 已解析的 logout token（可为 null）。 */
    private final LogoutToken logoutToken;
    /** 校验状态码。 */
    private final LogoutTokenValidationCode status;
    /** 校验通过的 OIDC 身份提供者列表。 */
    private final List<OIDCIdentityProvider> validIdentityProviders;

    LogoutTokenValidationContext(LogoutTokenValidationCode status) {
        this(status, null, null);
    }

    LogoutTokenValidationContext(LogoutTokenValidationCode status, LogoutToken logoutToken, List<OIDCIdentityProvider> validIdentityProviders) {
        this.logoutToken = logoutToken;
        this.status = status;
        this.validIdentityProviders = validIdentityProviders;
    }

    /** @return logout token */
    public LogoutToken getLogoutToken() {
        return logoutToken;
    }

    /** @return 校验状态码 */
    public LogoutTokenValidationCode getStatus() {
        return status;
    }

    /** @return 有效的 OIDC IdP 列表 */
    public List<OIDCIdentityProvider> getValidIdentityProviders() {
        return validIdentityProviders;
    }
}
