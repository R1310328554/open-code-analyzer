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

package org.keycloak.services.clientpolicy.executor;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * {@link SamlSecureClientUrisExecutor} 的 Provider 工厂。
 * <p>强制 SAML 客户端所有 URL 为 HTTPS，并可禁止通配符有效重定向 URI（可通过 <em>allow-wildcard-redirects</em> 配置放宽）。</p>
 *
 * @author rmartinc
 */
public class SamlSecureClientUrisExecutorFactory implements ClientPolicyExecutorProviderFactory {

    /** 执行器 Provider 标识符 */
    public static final String PROVIDER_ID = "saml-secure-client-uris";

    @Override
    public ClientPolicyExecutorProvider create(KeycloakSession session) {
        return new SamlSecureClientUrisExecutor(session);
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

    /** @return 执行器说明（英文原文保留） */
    @Override
    public String getHelpText() {
        return "Executor that enforces all URLs defined in the SAML client are https (TLS enabled). "
                + "It also enforces that wildcard redirect URIs are not used (configurable).";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("allow-wildcard-redirects")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .label("Allow wildcard valid redirect URIs")
                .helpText("Whether wildcard valid redirect URIs are allowed to be configured in the client.")
                .add()
                .build();
    }
}
