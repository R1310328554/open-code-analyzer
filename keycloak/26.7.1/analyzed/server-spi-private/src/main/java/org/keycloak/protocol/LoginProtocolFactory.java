/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol;

import java.util.Map;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;

import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;

/**
 * {@link LoginProtocol} 工厂：管理内置协议映射器、默认客户端作用域及客户端默认值。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface LoginProtocolFactory extends ProviderFactory<LoginProtocol> {
    /**
     * 可应用于客户端的内置 {@link ProtocolMapperModel} 映射。
     * List of built in protocol mappers that can be used to apply to clients.
     *
     * @return
     */
    Map<String, ProtocolMapperModel> getBuiltinMappers();


    /** 创建协议 REST 端点实例。 */
    Object createProtocolEndpoint(KeycloakSession session, EventBuilder event);


    /**
     * 新建 realm 时创建默认客户端作用域。
     * @param newRealm
     * @param addScopesToExistingClients If true, then existing realm clients will be updated (created realm default scopes will be added to them)
     */
    void createDefaultClientScopes(RealmModel newRealm, boolean addScopesToExistingClients);


    /**
     * 为新客户端设置协议相关的默认值。
     * @param rep
     * @param newClient
     */
    void setupClientDefaults(ClientRepresentation rep, ClientModel newClient);

    /**
     * 为特定登录协议的 {@link ClientScopeRepresentation} 填充默认值。
     * Add default values to {@link ClientScopeRepresentation}s that refer to the specific login-protocol
     */
    void addClientScopeDefaults(ClientScopeRepresentation clientScope);

    /**
     * 客户端作用域创建/更新时的协议特定校验钩子。
     * @param session Keycloak session
     * @param clientScope client scope to create or update
     * @throws WebApplicationException or some of it's subclass if validation fails
     */
    default void validateClientScope(KeycloakSession session, ClientScopeRepresentation clientScope) throws WebApplicationException {
    }

    /**
     * 校验 clientScope 是否适用于指定客户端（通常在协议请求中调用）。
     * Test if the clientScope is valid for particular client. Usually called during protocol requests
     */
    default boolean isValidClientScope(KeycloakSession session, ClientModel client, ClientScopeModel clientScope) {
        return true;
    }

    /**
     * 校验客户端作用域能否作为 Default 或 Optional 分配给客户端/realm。
     * <p>Validates whether a client scope can be assigned as Default or Optional to a client or realm.</p>
     * @param session      the Keycloak session
     * @param clientScope  the client scope to be assigned
     * @param defaultScope true if assigning as Default scope, false if Optional
     * @param realm        the realm where the assignment is happening
     * @throws BadRequestException if the assignment is not allowed
     */
    default void validateClientScopeAssignment(KeycloakSession session, ClientScopeModel clientScope, boolean defaultScope, RealmModel realm) {
        // 默认允许所有分配；协议实现可覆盖以施加限制
    }

    /**
     * 该协议是否可作为客户端登录协议使用。
     * Returns whether this protocol can be used as a client protocol.
     *
     * @return true if the protocol can be used for clients, false otherwise
     */
    default boolean allowAsClientProtocol() {
        return true;
    }

    /**
     * 撤销用户对指定客户端的授权同意时的回调。
     * @param session Keycloak session
     * @param client Client
     * @param user user
     */
    default void onConsentRevoked(KeycloakSession session, ClientModel client, UserModel user) {
    }
}
