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
import org.keycloak.models.sessions.infinispan.entities.RemoteUserSessionEntity;
import org.keycloak.models.sessions.infinispan.query.UserSessionQueries;

/**
 * 按状态过滤删除 {@link RemoteUserSessionEntity} 的条件删除器。
 * <p>
 * 通过 Infinispan Ickle 查询执行删除，无需索引。
 */
public class UserSessionQueryConditionalRemover extends MultipleConditionQueryRemover<String, RemoteUserSessionEntity> {

    public UserSessionQueryConditionalRemover() {
        super();
    }

    @Override
    String getEntity() {
        return UserSessionQueries.USER_SESSION;
    }

    /** 按 realmId 删除该 realm 下全部用户会话。 */
    public void removeByRealmId(String realmId) {
        add(new RemoveByRealm(nextParameter(), realmId));
    }

    /** 按 realmId + userId 删除指定用户的会话。 */
    public void removeByUserId(String realmId, String userId) {
        add(new RemoveUser(nextParameter(), userId, nextParameter(), realmId));
    }

    /** 按 userId 与 realmId 联合匹配的删除条件。 */
    private record RemoveUser(String userParameter, String userId, String realmParameter,
                              String realmId) implements RemoveCondition<String, RemoteUserSessionEntity> {

        @Override
        public String getConditionalClause() {
            return "(userId = :%s && realmId = :%s)".formatted(userParameter, realmParameter);
        }

        @Override
        public void addParameters(Map<String, Object> parameters) {
            parameters.put(userParameter, userId);
            parameters.put(realmParameter, realmId);
        }

        @Override
        public boolean willRemove(String key, RemoteUserSessionEntity value) {
            return Objects.equals(value.getUserId(), userId) && Objects.equals(value.getRealmId(), realmId);
        }
    }

    /** 按 realmId 匹配的删除条件。 */
    private record RemoveByRealm(String parameter,
                                 String realmId) implements RemoveCondition<String, RemoteUserSessionEntity> {

        @Override
        public String getConditionalClause() {
            return "(realmId = :%s)".formatted(parameter);
        }

        @Override
        public void addParameters(Map<String, Object> parameters) {
            parameters.put(parameter, realmId);
        }

        @Override
        public boolean willRemove(String key, RemoteUserSessionEntity value) {
            return Objects.equals(realmId, value.getRealmId());
        }
    }
}
