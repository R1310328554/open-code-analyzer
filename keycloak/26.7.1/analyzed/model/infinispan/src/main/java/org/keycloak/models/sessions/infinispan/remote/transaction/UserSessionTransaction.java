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

package org.keycloak.models.sessions.infinispan.remote.transaction;

import java.util.function.Consumer;

import org.keycloak.models.KeycloakTransaction;
import org.keycloak.models.sessions.infinispan.transaction.DatabaseUpdate;
import org.keycloak.models.sessions.infinispan.transaction.NonBlockingTransaction;

import org.infinispan.commons.util.concurrent.AggregateCompletionStage;

/**
 * 聚合在线/离线用户会话与客户端会话四套变更日志事务的 {@link KeycloakTransaction}。
 * <p>
 * 提交时异步并发执行四套事务的 {@link #asyncCommit}，缩短响应时间。
 */
public class UserSessionTransaction implements NonBlockingTransaction {

    /** 在线用户会话事务。 */
    private final UserSessionChangeLogTransaction userSessions;
    /** 在线客户端会话事务。 */
    private final ClientSessionChangeLogTransaction clientSessions;
    /** 离线用户会话事务。 */
    private final UserSessionChangeLogTransaction offlineUserSessions;
    /** 离线客户端会话事务。 */
    private final ClientSessionChangeLogTransaction offlineClientSessions;

    public UserSessionTransaction(UserSessionChangeLogTransaction userSessions, UserSessionChangeLogTransaction offlineUserSessions, ClientSessionChangeLogTransaction clientSessions, ClientSessionChangeLogTransaction offlineClientSessions) {
        this.userSessions = userSessions;
        this.offlineUserSessions = offlineUserSessions;
        this.clientSessions = clientSessions;
        this.offlineClientSessions = offlineClientSessions;
    }

    @Override
    public void asyncCommit(AggregateCompletionStage<Void> stage, Consumer<DatabaseUpdate> databaseUpdates) {
        userSessions.asyncCommit(stage, databaseUpdates);
        clientSessions.asyncCommit(stage, databaseUpdates);
        offlineUserSessions.asyncCommit(stage, databaseUpdates);
        offlineClientSessions.asyncCommit(stage, databaseUpdates);
    }

    @Override
    public void asyncRollback(AggregateCompletionStage<Void> stage) {
        userSessions.asyncRollback(stage);
        clientSessions.asyncRollback(stage);
        offlineUserSessions.asyncRollback(stage);
        offlineClientSessions.asyncRollback(stage);
    }

    public ClientSessionChangeLogTransaction getClientSessions(boolean offline) {
        return offline ? offlineClientSessions : clientSessions;
    }

    public UserSessionChangeLogTransaction getUserSessions(boolean offline) {
        return offline ? offlineUserSessions : userSessions;
    }

    /** 删除 realm 下在线与离线全部用户及客户端会话。 */
    public void removeAllSessionsByRealmId(String realmId) {
        clientSessions.getConditionalRemover().removeByRealmId(realmId);
        userSessions.getConditionalRemover().removeByRealmId(realmId);
        offlineClientSessions.getConditionalRemover().removeByRealmId(realmId);
        offlineUserSessions.getConditionalRemover().removeByRealmId(realmId);
    }

    /** 仅删除 realm 下在线用户及客户端会话。 */
    public void removeOnlineSessionsByRealmId(String realmId) {
        clientSessions.getConditionalRemover().removeByRealmId(realmId);
        userSessions.getConditionalRemover().removeByRealmId(realmId);
    }

    /** 删除指定用户在 realm 下的在线用户及客户端会话。 */
    public void removeAllSessionByUserId(String realmId, String userId) {
        userSessions.getConditionalRemover().removeByUserId(realmId, userId);
        clientSessions.getConditionalRemover().removeByUserId(realmId, userId);
    }

    /** 按 ID 删除用户会话及其下全部客户端会话。 */
    public void removeUserSessionById(String userSessionId, boolean offline) {
        getUserSessions(offline).remove(userSessionId);
        getClientSessions(offline).removeByUserSessionId(userSessionId);
    }
}
