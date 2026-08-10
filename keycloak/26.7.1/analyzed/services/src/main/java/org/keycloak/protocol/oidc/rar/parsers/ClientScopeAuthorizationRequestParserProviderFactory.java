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
package org.keycloak.protocol.oidc.rar.parsers;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oidc.rar.AuthorizationRequestParserProvider;
import org.keycloak.protocol.oidc.rar.AuthorizationRequestParserProviderFactory;

/**
 * 客户端范围授权请求解析器工厂。
 * <p>注册 {@code client-scope} 解析器，将 OAuth scope 转换为 RAR 授权请求上下文。</p>
 *
 * @author <a href="mailto:dgozalob@redhat.com">Daniel Gozalo</a>
 */
public class ClientScopeAuthorizationRequestParserProviderFactory implements AuthorizationRequestParserProviderFactory {

    /** 解析器提供方标识 */
    public static final String CLIENT_SCOPE_PARSER_ID = "client-scope";

    /** @param session Keycloak 会话 @return 客户端范围解析器实例 */
    @Override
    public AuthorizationRequestParserProvider create(KeycloakSession session) {
        return new ClientScopeAuthorizationRequestParser(session);
    }

    /** 初始化（无操作） @param config 配置作用域 */
    @Override
    public void init(Config.Scope config) {

    }

    /** 工厂初始化后回调 @param factory 会话工厂 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** 关闭资源（无操作） */
    @Override
    public void close() {

    }

    /** @return 解析器标识 {@link #CLIENT_SCOPE_PARSER_ID} */
    @Override
    public String getId() {
        return CLIENT_SCOPE_PARSER_ID;
    }
}
