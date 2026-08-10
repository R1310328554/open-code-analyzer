/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oidc.rar;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.keycloak.models.ClientModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;
import org.keycloak.rar.AuthorizationRequestContext;

/**
 * 授权请求 scope 解析提供者：将 scope 参数字符串解析为 {@link AuthorizationRequestContext}。
 * <p>用于 RAR 等场景下对 scope 的扩展解析。</p>
 *
 * @author <a href="mailto:dgozalob@redhat.com">Daniel Gozalo</a>
 */
public interface AuthorizationRequestParserProvider extends Provider {

    /**
     * 解析客户端 scope 参数。
     * @param client 客户端模型
     * @param scopeParam scope 查询参数字符串，可为 {@code null}
     * @return 解析后的授权请求上下文
     */
    AuthorizationRequestContext parseScopes(@Nonnull ClientModel client, @Nullable String scopeParam);

    /** 带用户上下文的 scope 解析，默认委托 {@link #parseScopes(ClientModel, String)}。 */
    default AuthorizationRequestContext parseScopes(@Nullable UserModel user, @Nonnull ClientModel client, @Nullable String scopeParam) {
        return parseScopes(client, scopeParam);
    }

}
