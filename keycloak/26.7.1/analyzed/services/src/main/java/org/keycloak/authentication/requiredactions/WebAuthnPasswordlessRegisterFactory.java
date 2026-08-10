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

import org.keycloak.models.KeycloakSession;

import com.webauthn4j.verifier.attestation.trustworthiness.certpath.CertPathTrustworthinessVerifier;

/**
 * {@link WebAuthnPasswordlessRegister} 工厂：注册无密码 WebAuthn 凭证必需操作。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class WebAuthnPasswordlessRegisterFactory extends WebAuthnRegisterFactory {

    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "webauthn-register-passwordless";

    /** @return {@link WebAuthnPasswordlessRegister} 实例 */
    @Override
    protected WebAuthnRegister createProvider(KeycloakSession session, CertPathTrustworthinessVerifier trustVerifier) {
        return new WebAuthnPasswordlessRegister(session, trustVerifier);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayText() {
        return "Webauthn Register Passwordless";
    }

}
