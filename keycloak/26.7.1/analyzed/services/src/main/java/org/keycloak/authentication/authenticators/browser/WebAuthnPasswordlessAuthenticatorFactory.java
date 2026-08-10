/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.authentication.authenticators.browser;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.credential.WebAuthnCredentialModel;

/**
 * 无密码 WebAuthn 认证器工厂：注册 {@link WebAuthnPasswordlessAuthenticator}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class WebAuthnPasswordlessAuthenticatorFactory extends WebAuthnAuthenticatorFactory {

    /** Provider ID：webauthn-authenticator-passwordless。 */
    public static final String PROVIDER_ID = "webauthn-authenticator-passwordless";

    @Override
    /** @return 参考类别（passwordless 凭证类型） */
    public String getReferenceCategory() {
        return WebAuthnCredentialModel.TYPE_PASSWORDLESS;
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "WebAuthn Passwordless Authenticator";
    }

    @Override
    /** @return 帮助说明：无密码 WebAuthn 认证 */
    public String getHelpText() {
        return "Authenticator for Passwordless WebAuthn authentication";
    }

    @Override
    /** @return 新建 {@link WebAuthnPasswordlessAuthenticator} 实例 */
    public Authenticator create(KeycloakSession session) {
        return new WebAuthnPasswordlessAuthenticator(session);
    }

    @Override
    /** @return Provider ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

}
