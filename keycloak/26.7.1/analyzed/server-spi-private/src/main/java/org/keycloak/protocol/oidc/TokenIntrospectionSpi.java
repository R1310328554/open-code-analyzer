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
package org.keycloak.protocol.oidc;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * OAuth2 令牌自省 SPI：为 Token Introspection 端点注册额外的令牌类型支持。
 * <p>内部 SPI，名称 {@code oauth2-token-introspection}。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class TokenIntrospectionSpi implements Spi {
    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code oauth2-token-introspection} */
    @Override
    public String getName() {
        return "oauth2-token-introspection";
    }

    /** @return 提供者接口 {@link TokenIntrospectionProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return TokenIntrospectionProvider.class;
    }

    /** @return 工厂接口 {@link TokenIntrospectionProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return TokenIntrospectionProviderFactory.class;
    }
}
