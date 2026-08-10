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
package org.keycloak.protocol.oid4vc.issuance;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oid4vc.OID4VCEnvironmentProviderFactory;
import org.keycloak.protocol.oidc.rar.AuthorizationDetailsProcessorFactory;
import org.keycloak.util.AuthorizationDetailsParser;

import static org.keycloak.OID4VCConstants.OPENID_CREDENTIAL;

/**
 * 创建 OID4VCI 专用 {@link AuthorizationDetailsProcessor} 的工厂。
 * <p>初始化时注册 {@link OID4VCAuthorizationDetailsParser}；仅在 OID4VCI 特性可用时启用。</p>
 *
 * @author <a href="mailto:Forkim.Akwichek@adorsys.com">Forkim Akwichek</a>
 */
public class OID4VCAuthorizationDetailsProcessorFactory implements AuthorizationDetailsProcessorFactory, OID4VCEnvironmentProviderFactory {

    /** 提供方 ID，与 {@code openid_credential} 授权细节类型一致。 */
    public static final String PROVIDER_ID = OPENID_CREDENTIAL;

    @Override
    /** @param session Keycloak 会话 @return OID4VCI 授权细节处理器 */
    public OID4VCAuthorizationDetailsProcessor create(KeycloakSession session) {
        return new OID4VCAuthorizationDetailsProcessor(session);
    }

    @Override
    public void init(Config.Scope config) {
        AuthorizationDetailsParser.registerParser(OPENID_CREDENTIAL, new OID4VCAuthorizationDetailsParser());
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** 较高优先级，优先匹配 OID4VCI 授权细节类型。 */
    public int order() {
        // Higher order means higher priority - OID4VCI should be checked first
        return 100;
    }
}
