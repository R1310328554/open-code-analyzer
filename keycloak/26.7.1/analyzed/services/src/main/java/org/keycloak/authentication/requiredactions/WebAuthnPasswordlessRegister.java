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

package org.keycloak.authentication.requiredactions;

import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.credential.WebAuthnPasswordlessCredentialProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.WebAuthnPolicy;
import org.keycloak.models.credential.WebAuthnCredentialModel;

import com.webauthn4j.verifier.attestation.trustworthiness.certpath.CertPathTrustworthinessVerifier;

/**
 * WebAuthn 无密码凭证注册必需操作：使用 realm 无密码 WebAuthn 策略注册凭证。
 * <p>临时类，未来认证 SPI 改进后可能移除；继承 {@link WebAuthnRegister}。</p>
 */
public class WebAuthnPasswordlessRegister extends WebAuthnRegister {

    /** @param session Keycloak 会话 @param certPathtrustVerifier 证书路径信任校验器 */
    public WebAuthnPasswordlessRegister(KeycloakSession session, CertPathTrustworthinessVerifier certPathtrustVerifier) {
        super(session, certPathtrustVerifier);
    }

    /** @return realm 无密码 WebAuthn 策略 */
    @Override
    protected WebAuthnPolicy getWebAuthnPolicy(RequiredActionContext context) {
        return context.getRealm().getWebAuthnPolicyPasswordless();
    }

    /** @return 无密码 WebAuthn 凭证类型 */
    @Override
    protected String getCredentialType() {
        return WebAuthnCredentialModel.TYPE_PASSWORDLESS;
    }

    /** @return 无密码 WebAuthn 凭证提供者 ID */
    @Override
    protected String getCredentialProviderId() {
        return WebAuthnPasswordlessCredentialProviderFactory.PROVIDER_ID;
    }


}
