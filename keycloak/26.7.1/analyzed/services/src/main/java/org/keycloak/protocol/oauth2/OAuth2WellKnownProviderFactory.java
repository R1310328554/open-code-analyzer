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
 */package org.keycloak.protocol.oauth2;

import org.keycloak.protocol.oidc.OIDCWellKnownProviderFactory;

/**
 * OAuth 2.0 授权服务器元数据（Well-Known）提供方工厂。
 * <p>继承 {@link OIDCWellKnownProviderFactory}，暴露 RFC 8414 定义的 {@code /.well-known/oauth-authorization-server} 端点。</p>
 * <p>{@see https://www.rfc-editor.org/rfc/rfc8414.html#section-3}</p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class OAuth2WellKnownProviderFactory extends OIDCWellKnownProviderFactory {
    /** Well-Known 提供方 SPI ID。 */
    public static final String PROVIDER_ID = "oauth-authorization-server";
    @Override
    /** @return 提供方 ID {@value #PROVIDER_ID} */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** 可通过服务器元数据（Server Metadata）发现此提供方。 */
    public boolean isAvailableViaServerMetadata() {
        return true;
    }
}
