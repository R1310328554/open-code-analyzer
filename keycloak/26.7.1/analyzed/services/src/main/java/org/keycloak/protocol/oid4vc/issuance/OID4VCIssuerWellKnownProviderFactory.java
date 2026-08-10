/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.issuance;

import org.keycloak.Config;
import org.keycloak.OID4VCConstants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oid4vc.OID4VCEnvironmentProviderFactory;
import org.keycloak.wellknown.WellKnownProvider;
import org.keycloak.wellknown.WellKnownProviderFactory;

/**
 * OID4VCI 凭证签发者元数据的 {@link WellKnownProviderFactory} 工厂。
 * <p>端点 {@code /.well-known/openid-credential-issuer}，返回 Credential Issuer Metadata。</p>
 * <p>{@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-11.2.2}</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public class OID4VCIssuerWellKnownProviderFactory implements WellKnownProviderFactory, OID4VCEnvironmentProviderFactory {

    public static final String PROVIDER_ID = OID4VCConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER;

    @Override
    /** @param session Keycloak 会话 @return OID4VCI 签发者元数据提供方 */
    public WellKnownProvider create(KeycloakSession session) {
        return new OID4VCIssuerWellKnownProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
        // no-op
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** 可通过 Server Metadata 发现。 */
    public boolean isAvailableViaServerMetadata() {
        return true;
    }
}
