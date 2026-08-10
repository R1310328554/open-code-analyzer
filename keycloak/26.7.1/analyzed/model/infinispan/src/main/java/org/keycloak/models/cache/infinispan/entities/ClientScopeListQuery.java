/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.cache.infinispan.entities;

import java.util.Set;

import org.keycloak.models.RealmModel;

/**
 * 客户端作用域列表查询结果的 Infinispan 缓存实体。
 * <p>
 * 实现 {@link ClientScopeQuery}，缓存某 realm（及可选客户端）下的作用域 ID 集合。
 */
public class ClientScopeListQuery extends AbstractRevisioned implements ClientScopeQuery {
    /** 查询命中的客户端作用域 ID 集合。 */
    private final Set<String> clientScopes;
    /** 所属 realm 的唯一标识。 */
    private final String realm;
    /** 可选的关联客户端 UUID。 */
    private String clientUuid;

    /** 以作用域 ID 集合构造列表查询缓存条目。 */
    public ClientScopeListQuery(long revisioned, String id, RealmModel realm, Set<String> clientScopes) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.clientScopes = clientScopes;
    }

    /** 以指定客户端 UUID 与作用域 ID 集合构造列表查询缓存条目。 */
    public ClientScopeListQuery(long revisioned, String id, RealmModel realm, String clientUuid, Set<String> clientScopes) {
        this(revisioned, id, realm, clientScopes);
        this.clientUuid = clientUuid;
    }

    /** 返回查询命中的客户端作用域 ID 集合。 */
    @Override
    public Set<String> getClientScopes() {
        return clientScopes;
    }

    /** 返回所属 realm 的唯一标识。 */
    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回关联客户端 UUID。 */
    @Override
    public String getClientId() {
        return clientUuid;
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return "ClientScopeListQuery{" +
                "id='" + getId() + "'" +
                ", realm='" + realm + '\'' +
                ", clientUuid='" + clientUuid + '\'' +
                '}';
    }
}
