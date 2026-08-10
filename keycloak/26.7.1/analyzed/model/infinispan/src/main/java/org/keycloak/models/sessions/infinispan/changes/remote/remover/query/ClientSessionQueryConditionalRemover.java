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

package org.keycloak.models.sessions.infinispan.changes.remote.remover.query;

import java.util.Map;
import java.util.Objects;

import org.keycloak.models.sessions.infinispan.changes.remote.remover.ConditionalRemover;
import org.keycloak.models.sessions.infinispan.entities.ClientSessionKey;
import org.keycloak.models.sessions.infinispan.entities.RemoteAuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.query.ClientSessionQueries;

/**
 * 按状态过滤删除 {@link RemoteAuthenticatedClientSessionEntity} 的条件删除器。
 * <p>
 * 通过 Infinispan Ickle 查询执行删除，无需索引。
 */
public class ClientSessionQueryConditionalRemover extends MultipleConditionQueryRemover<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> {

    public ClientSessionQueryConditionalRemover() {
        super();
    }

    @Override
    String getEntity() {
        return ClientSessionQueries.CLIENT_SESSION;
    }

    /** 按用户会话 ID 删除关联的客户端会话。 */
    public void removeByUserSessionId(String userSessionId) {
        add(new RemoveByUserSession(nextParameter(), userSessionId));
    }

    /** 按 realmId 删除该 realm 下全部客户端会话。 */
    public void removeByRealmId(String realmId) {
        add(new RemoveByRealm(nextParameter(), realmId));
    }

    /** 按 realmId + userId 删除指定用户的客户端会话。 */
    public void removeByUserId(String realmId, String userId) {
        add(new RemoveByUser(nextParameter(), realmId, nextParameter(), userId));
    }

    /** 按 userSessionId 匹配的删除条件。 */
    private record RemoveByUserSession(String userSessionParameter,
                                       String userSessionId) implements RemoveCondition<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> {

        @Override
        public String getConditionalClause() {
            return "(userSessionId = :%s)".formatted(userSessionParameter);
        }

        @Override
        public void addParameters(Map<String, Object> parameters) {
            parameters.put(userSessionParameter, userSessionId);
        }

        @Override
        public boolean willRemove(ClientSessionKey key, RemoteAuthenticatedClientSessionEntity value) {
            return Objects.equals(value.getUserSessionId(), userSessionId);
        }
    }

    /** 按 realmId 匹配的删除条件。 */
    private record RemoveByRealm(String realmParameter,
                                 String realmId) implements RemoveCondition<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> {

        @Override
        public String getConditionalClause() {
            return "(realmId = :%s)".formatted(realmParameter);
        }

        @Override
        public void addParameters(Map<String, Object> parameters) {
            parameters.put(realmParameter, realmId);
        }

        @Override
        public boolean willRemove(ClientSessionKey key, RemoteAuthenticatedClientSessionEntity value) {
            return Objects.equals(value.getRealmId(), realmId);
        }
    }

    /** 按 userId 与 realmId 联合匹配的删除条件。 */
    private record RemoveByUser(String realmParameter, String realmId, String userParameter,
                                String userId) implements RemoveCondition<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> {

        @Override
        public String getConditionalClause() {
            return "(userId = :%s && realmId = :%s)".formatted(userParameter, realmParameter);
        }

        @Override
        public void addParameters(Map<String, Object> parameters) {
            parameters.put(realmParameter, realmId);
            parameters.put(userParameter, userId);
        }

        @Override
        public boolean willRemove(ClientSessionKey key, RemoteAuthenticatedClientSessionEntity value) {
            return Objects.equals(value.getUserId(), userId) && Objects.equals(value.getRealmId(), realmId);
        }
    }
}
