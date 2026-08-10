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

package org.keycloak.models.sessions.infinispan.stream;

import java.util.Map;
import java.util.function.Predicate;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.sessions.infinispan.AuthenticatedClientSessionAdapter;
import org.keycloak.models.sessions.infinispan.changes.SessionEntityWrapper;
import org.keycloak.models.sessions.infinispan.entities.UserSessionEntity;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 用户会话缓存流过滤谓词，可按 realm、用户、客户端及 broker 会话信息组合筛选。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@ProtoTypeId(Marshalling.USER_SESSION_PREDICATE)
public class UserSessionPredicate implements Predicate<Map.Entry<String, SessionEntityWrapper<UserSessionEntity>>> {

    /** 目标 realm ID。 */
    private final String realm;

    /** 可选的用户 ID 过滤条件。 */
    private String user;

    /** 可选的客户端 UUID 过滤条件。 */
    private String client;

    /** 可选的 broker 会话 ID 过滤条件。 */
    private String brokerSessionId;
    /** 可选的 broker 用户 ID 过滤条件。 */
    private String brokerUserId;

    private UserSessionPredicate(String realm) {
        this.realm = realm;
    }

    /**
     * 创建用户会话谓词。若使用 {@link #client(java.lang.String)}，请注意其 stale 会话警告。
     * @param realm realm 标识
     * @return 可链式配置的谓词实例
     */
    public static UserSessionPredicate create(String realm) {
        return new UserSessionPredicate(realm);
    }

    /** 限定匹配的用户 ID。 */
    public UserSessionPredicate user(String user) {
        this.user = user;
        return this;
    }

    /**
     * 增加客户端匹配条件。因性能原因，客户端会话从用户会话分离时仅删除客户端会话、
     * 不更新用户会话，此条件可能匹配到过时的会话。
     *
     * @see AuthenticatedClientSessionAdapter#detachFromUserSession()
     * @param clientUUID 客户端 UUID
     * @return 当前谓词实例，便于链式调用
     */
    public UserSessionPredicate client(String clientUUID) {
        this.client = clientUUID;
        return this;
    }

    /** 限定 broker 会话 ID。 */
    public UserSessionPredicate brokerSessionId(String id) {
        this.brokerSessionId = id;
        return this;
    }

    /** 限定 broker 用户 ID。 */
    public UserSessionPredicate brokerUserId(String id) {
        this.brokerUserId = id;
        return this;
    }

    /**
     * 返回用户 ID 过滤条件。
     * @return 用户 ID，未设置时为 null
     */
    @ProtoField(1)
    public String getUserId() {
        return user;
    }

    @ProtoField(2)
    public String getBrokerSessionId() {
        return brokerSessionId;
    }

    @ProtoField(3)
    public String getBrokerUserId() {
        return brokerUserId;
    }

    @ProtoField(4)
    String getRealm() {
        return realm;
    }

    @ProtoField(5)
    public String getClient() {
        return client;
    }

    /** ProtoStream 反序列化工厂，从各字段重建谓词。 */
    @ProtoFactory
    static UserSessionPredicate create(String userId, String brokerSessionId, String brokerUserId, String realm, String client) {
        return create(realm)
                .user(userId)
                .client(client)
                .brokerSessionId(brokerSessionId)
                .brokerUserId(brokerUserId);
    }

    /** 对缓存条目中的 {@link UserSessionEntity} 应用组合过滤条件。 */
    @Override
    public boolean test(Map.Entry<String, SessionEntityWrapper<UserSessionEntity>> entry) {
        UserSessionEntity entity = entry.getValue().getEntity();

        return realm.equals(entity.getRealmId()) &&
                (user == null || entity.getUser().equals(user)) &&
                (client == null || entity.getClientSessions().contains(client)) &&
                (brokerSessionId == null || brokerSessionId.equals(entity.getBrokerSessionId())) &&
                (brokerUserId == null || brokerUserId.equals(entity.getBrokerUserId()));

    }

    /** 转换为作用于 {@link UserSessionModel} 的等价谓词，供领域模型层查询使用。 */
    public Predicate<? super UserSessionModel> toModelPredicate() {

        return (Predicate<UserSessionModel>) entity ->
                entity != null && realm.equals(entity.getRealm().getId()) &&
                        (user == null || entity.getUser().getId().equals(user)) &&
                        (client == null || (entity.getAuthenticatedClientSessions() != null && entity.getAuthenticatedClientSessions().containsKey(client))) &&
                        (brokerSessionId == null || brokerSessionId.equals(entity.getBrokerSessionId())) &&
                        (brokerUserId == null || brokerUserId.equals(entity.getBrokerUserId()));
    }


}
