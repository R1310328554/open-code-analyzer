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

package org.keycloak.models.sessions.infinispan.entities;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.common.util.Time;
import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoReserved;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 已认证客户端会话的 Infinispan 嵌入式缓存实体。
 * <p>
 * 与用户会话关联，存储 OAuth/OIDC 客户端登录上下文及 notes。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.AUTHENTICATED_CLIENT_SESSION_ENTITY)
@ProtoReserved(
        value = {7},
        names = {"id"}
)
public class AuthenticatedClientSessionEntity extends SessionEntity {

    // 远程缓存最后时间戳元数据，用于判断是否需要写入远程 DC（已弃用）
    @Deprecated(since = "26.4", forRemoval = true)
    public static final String LAST_TIMESTAMP_REMOTE = "lstr";
    @Deprecated(since = "26.4", forRemoval = true)
    public static final String CLIENT_ID_NOTE = "clientId";

    private String authMethod;
    private String redirectUri;
    private volatile int timestamp;
    private String action;

    private Map<String, String> notes = new ConcurrentHashMap<>();

    // TODO [pruivo] [KC27] 这些字段构成客户端会话身份，计划改为 final
    private volatile String userSessionId;
    private volatile String clientId;
    private volatile String userId;

    public AuthenticatedClientSessionEntity() {
    }

    @ProtoField(2)
    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    @ProtoField(3)
    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @ProtoField(4)
    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getUserSessionStarted() {
        String started = getNotes().get(AuthenticatedClientSessionModel.USER_SESSION_STARTED_AT_NOTE);
        return started == null ? timestamp : Integer.parseInt(started);
    }

    public int getStarted() {
        String started = getNotes().get(AuthenticatedClientSessionModel.STARTED_AT_NOTE);
        return started == null ? timestamp : Integer.parseInt(started);
    }

    public boolean isUserSessionRememberMe() {
        return Boolean.parseBoolean(getNotes().get(AuthenticatedClientSessionModel.USER_SESSION_REMEMBER_ME_NOTE));
    }

    @ProtoField(9)
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        getNotes().put(CLIENT_ID_NOTE, clientId);
        this.clientId = clientId;
    }

    @ProtoField(5)
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @ProtoField(value = 6, mapImplementation = ConcurrentHashMap.class)
    public Map<String, String> getNotes() {
        return notes;
    }

    public void setNotes(Map<String, String> notes) {
        this.notes = notes;
    }

    @ProtoField(10)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        AuthenticatedClientSessionEntity that = (AuthenticatedClientSessionEntity) o;
        return Objects.equals(userSessionId, that.userSessionId) && Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(userSessionId);
        result = 31 * result + Objects.hashCode(clientId);
        return result;
    }

    // ProtoFactory：因部分字段为 final 而需要工厂构造
    @ProtoFactory
    AuthenticatedClientSessionEntity(String realmId, String authMethod, String redirectUri, int timestamp, String action, Map<String, String> notes, String userSessionId, String clientId, String userId) {
        super(realmId);
        this.authMethod = authMethod;
        this.redirectUri = redirectUri;
        this.timestamp = timestamp;
        this.action = action;
        this.notes = notes;
        this.userSessionId = userSessionId;
        this.clientId = clientId;
        this.userId = userId;
    }

    @ProtoField(8)
    public String getUserSessionId() {
        return userSessionId;
    }

    public void setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
    }

    public static AuthenticatedClientSessionEntity create(RealmModel realm, ClientModel client, UserSessionModel userSession) {
        var entity = new AuthenticatedClientSessionEntity();
        entity.setRealmId(realm.getId());
        entity.setClientId(client.getId());
        entity.setTimestamp(Time.currentTime());
        entity.getNotes().put(AuthenticatedClientSessionModel.STARTED_AT_NOTE, String.valueOf(entity.getTimestamp()));
        entity.getNotes().put(AuthenticatedClientSessionModel.USER_SESSION_STARTED_AT_NOTE, String.valueOf(userSession.getStarted()));
        if (userSession.isRememberMe()) {
            entity.getNotes().put(AuthenticatedClientSessionModel.USER_SESSION_REMEMBER_ME_NOTE, "true");
        }
        entity.setUserId(userSession.getUser().getId());
        return entity;
    }

    public static AuthenticatedClientSessionEntity createFromModel(AuthenticatedClientSessionModel model) {
        var entity = create(model.getRealm(), model.getClient(), model.getUserSession());
        entity.setNotes(model.getNotes() == null ? new ConcurrentHashMap<>() : model.getNotes());
        return entity;
    }
}
