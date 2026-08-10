/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models;

import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.rar.AuthorizationRequestContext;

/**
 * 请求级客户端会话上下文：聚合已认证客户端会话、作用域、角色与协议映射。
 * Request-scoped context object
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClientSessionContext {

    /** @return 已认证的客户端会话 */
    AuthenticatedClientSessionModel getClientSession();

    /** @return 已解析的客户端作用域 ID 集合 */
    Set<String> getClientScopeIds();

    /**
     * 以流形式返回客户端作用域。
     * Returns client scopes as a stream.
     * @return Stream of client scopes. Never returns {@code null}.
     */
    Stream<ClientScopeModel> getClientScopesStream();

    /**
     * @return 是否请求了 offline token
     */
    boolean isOfflineTokenRequested();

    /**
     * 返回全部角色（含复合角色）流。
     * Returns all roles including composite ones as a stream.
     * @return Stream of {@link RoleModel}. Never returns {@code null}.
     */
    Stream<RoleModel> getRolesStream();

    /**
     * Returns protocol mappers as a stream.
     * @return Stream of protocol mappers. Never returns {@code null}.
     */
    Stream<ProtocolMapperModel> getProtocolMappersStream();

    /** @return 作用域字符串表示 */
    String getScopeString();

    String getScopeString(boolean ignoreIncludeInTokenScope);

    void setAttribute(String name, Object value);

    <T> T getAttribute(String attribute, Class<T> clazz);

    /** @return RAR 授权请求上下文 */
    AuthorizationRequestContext getAuthorizationRequestContext();

}
