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
package org.keycloak.storage.client;

import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.RealmModel;

/**
 * 客户端查找抽象：按内部 ID 与 clientId 精确/模糊查询客户端，登录流程必需。
 * Abstraction interface for lookoup of clients by id and clientId.  These methods required for participating in login flows.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientLookupProvider {

    /**
     * 按内部 ID 精确查找客户端。
     * Exact search for a client by its internal ID.
     * @param realm Realm to limit the search.
     * @param id Internal ID
     * @return Model of the client, or {@code null} if no client is found.
     */
    ClientModel getClientById(RealmModel realm, String id);

    /**
     * 按公开 clientId（OIDC 的 client_id / SAML 的 entityID）精确查找客户端。
     * Exact search for a client by its public client identifier.
     * @param realm Realm to limit the search for clients.
     * @param clientId String that identifies the client to the external parties.
     *   Maps to {@code client_id} in OIDC or {@code entityID} in SAML.
     * @return Model of the client, or {@code null} if no client is found.
     */
    ClientModel getClientByClientId(RealmModel realm, String clientId);

    /**
     * 在公开 clientId 中不区分大小写地模糊搜索客户端。
     * Case-insensitive search for clients that contain the given string in their public client identifier.
     * @param realm Realm to limit the search for clients.
     * @param clientId Searched substring of the public client
     *   identifier ({@code client_id} in OIDC or {@code entityID} in SAML.)
     * @param firstResult First result to return. Ignored if negative or {@code null}.
     * @param maxResults Maximum number of results to return. Ignored if negative or {@code null}.
     * @return Stream of ClientModel or an empty stream if no client is found. Never returns {@code null}.
     */
    Stream<ClientModel> searchClientsByClientIdStream(RealmModel realm, String clientId, Integer firstResult, Integer maxResults);

    /** 按属性名值对搜索客户端。
     * @param realm 限定 realm
     * @param attributes 属性过滤条件
     * @param firstResult 分页起始（负值或 null 忽略）
     * @param maxResults 最大返回数（负值或 null 忽略） */
    Stream<ClientModel> searchClientsByAttributes(RealmModel realm, Map<String, String> attributes, Integer firstResult, Integer maxResults);

    /** 按认证流绑定覆盖项搜索客户端。
     * @param realm 限定 realm
     * @param overrides 需匹配的流绑定覆盖
     * @param firstResult 分页起始
     * @param maxResults 最大返回数 */
    default Stream<ClientModel> searchClientsByAuthenticationFlowBindingOverrides(RealmModel realm, Map<String, String> overrides, Integer firstResult, Integer maxResults) {
		Stream<ClientModel> clients = searchClientsByAttributes(realm, Map.of(), null, null)
				.filter(client -> overrides.entrySet().stream().allMatch(override -> override.getValue().equals(client.getAuthenticationFlowBindingOverrides().get(override.getKey()))));
		if (firstResult != null && firstResult >= 0) {
			clients = clients.skip(firstResult);
		}
		if (maxResults != null && maxResults >= 0 ) {
			clients = clients.limit(maxResults);
		}
		return clients;
    }

    /**
     * 返回客户端关联的全部默认范围（{@code defaultScope=true}）或可选范围（{@code false}）。
     * Return all default scopes (if {@code defaultScope} is {@code true}) or all optional scopes (if {@code defaultScope} is {@code false}) linked with the client
     *
     * @param realm Realm
     * @param client Client
     * @param defaultScopes if true default scopes, if false optional scopes, are returned
     * @return map where key is the name of the clientScope, value is particular clientScope. Returns empty map if no scopes linked (never returns null).
     */
    Map<String, ClientScopeModel> getClientScopes(RealmModel realm, ClientModel client, boolean defaultScopes);
}
