/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authentication.requiredactions;

import org.keycloak.Config;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.truststore.TruststoreProvider;

import com.webauthn4j.anchor.KeyStoreTrustAnchorRepository;
import com.webauthn4j.verifier.attestation.trustworthiness.certpath.CertPathTrustworthinessVerifier;
import com.webauthn4j.verifier.attestation.trustworthiness.certpath.DefaultCertPathTrustworthinessVerifier;
import com.webauthn4j.verifier.attestation.trustworthiness.certpath.NullCertPathTrustworthinessVerifier;

/**
 * {@link WebAuthnRegister} 工厂：根据信任库创建带证书路径校验的 WebAuthn 注册必需操作。
 * <p>需启用 {@link Profile.Feature#WEB_AUTHN} 特性。</p>
 */
public class WebAuthnRegisterFactory implements RequiredActionFactory, EnvironmentDependentProviderFactory {

    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "webauthn-register";

    /** 按信任库可用性创建 {@link WebAuthnRegister} 实例。 */
    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        WebAuthnRegister webAuthnRegister = null;
        TruststoreProvider truststoreProvider = session.getProvider(TruststoreProvider.class);
        if (truststoreProvider == null || truststoreProvider.getTruststore() == null) {
            webAuthnRegister = createProvider(session, new NullCertPathTrustworthinessVerifier());
        } else {
            KeyStoreTrustAnchorRepository keyStoreTrustAnchorRepository = new KeyStoreTrustAnchorRepository(truststoreProvider.getTruststore());
            DefaultCertPathTrustworthinessVerifier trustVerifier = new DefaultCertPathTrustworthinessVerifier(keyStoreTrustAnchorRepository);
            webAuthnRegister = createProvider(session, trustVerifier);
        }
        return webAuthnRegister;
    }

    /** @return 绑定 session 与信任校验器的 {@link WebAuthnRegister} */
    protected WebAuthnRegister createProvider(KeycloakSession session, CertPathTrustworthinessVerifier trustVerifier) {
         return new WebAuthnRegister(session, trustVerifier);
    }

    @Override
    public void init(Scope config) {
        // NOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOP
    }

    @Override
    public void close() {
        // NOP
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayText() {
        return "Webauthn Register";
    }

    /** @return 是否启用 WEB_AUTHN 特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.WEB_AUTHN);
    }
}
