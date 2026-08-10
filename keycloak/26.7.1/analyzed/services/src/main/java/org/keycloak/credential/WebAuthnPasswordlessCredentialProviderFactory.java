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

package org.keycloak.credential;

import org.keycloak.models.KeycloakSession;

/**
 * WebAuthn 无密码凭证 {@link WebAuthnPasswordlessCredentialProvider} 的 SPI 工厂。
 * <p>复用 {@link WebAuthnCredentialProviderFactory} 的转换器与元数据服务。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class WebAuthnPasswordlessCredentialProviderFactory extends WebAuthnCredentialProviderFactory {

    /** SPI 工厂标识：{@code keycloak-webauthn-passwordless}。 */
    public static final String PROVIDER_ID = "keycloak-webauthn-passwordless";

    @Override
    /** @param session 当前会话 @return 无密码 WebAuthn 凭证提供者 */
    public CredentialProvider create(KeycloakSession session) {
        return new WebAuthnPasswordlessCredentialProvider(session, getMetadataService(), createOrGetObjectConverter());
    }

    @Override
    /** @return {@link #PROVIDER_ID} */
    public String getId() {
        return PROVIDER_ID;
    }

}
