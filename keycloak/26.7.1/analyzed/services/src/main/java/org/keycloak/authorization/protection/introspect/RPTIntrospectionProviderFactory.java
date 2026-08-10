/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.protection.introspect;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oidc.TokenIntrospectionProvider;
import org.keycloak.protocol.oidc.TokenIntrospectionProviderFactory;

/**
 * {@link RPTIntrospectionProvider} 工厂，provider id 为 requesting_party_token。
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class RPTIntrospectionProviderFactory implements TokenIntrospectionProviderFactory {
    /** @return RPT 自省提供者实例 */
    @Override
    public TokenIntrospectionProvider create(KeycloakSession session) {
        return new RPTIntrospectionProvider(session);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    /** @return provider id requesting_party_token */
    @Override
    public String getId() {
        return "requesting_party_token";
    }
}
