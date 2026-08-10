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

package org.keycloak.models.cache.infinispan.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.cache.infinispan.UserCacheManager;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 用户完整失效事件，在用户新增或删除时使用。
 * <p>
 * 携带用户名、邮箱、领域 ID 及联邦身份快照，通知 {@link UserCacheManager}
 * 全面失效与该用户相关的全部缓存条目（含按用户名/邮箱查询及联邦身份链接）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.USER_FULL_INVALIDATION_EVENT)
public class UserFullInvalidationEvent extends InvalidationEvent implements UserCacheInvalidationEvent {

    /** 用户名。 */
    @ProtoField(2)
    final String username;
    /** 用户邮箱。 */
    @ProtoField(3)
    final String email;
    /** 所属领域 ID。 */
    @ProtoField(4)
    final String realmId;
    /** 领域是否启用了身份联合功能。 */
    @ProtoField(5)
    final boolean identityFederationEnabled;
    /** 联邦身份映射：IdP 别名 → 外部用户 ID。 */
    @ProtoField(value = 6, mapImplementation = HashMap.class)
    final Map<String, String> federatedIdentities;

    /** 构造用户完整失效事件。 */
    private UserFullInvalidationEvent(String id, String username, String email, String realmId, boolean identityFederationEnabled, Map<String, String> federatedIdentities) {
        super(id);
        this.username = Objects.requireNonNull(username);
        this.email = email;
        this.realmId = Objects.requireNonNull(realmId);
        this.federatedIdentities = federatedIdentities;
        this.identityFederationEnabled = identityFederationEnabled;
    }

    /** 根据用户信息与联邦身份流创建完整失效事件。 */
    public static UserFullInvalidationEvent create(String userId, String username, String email, String realmId, boolean identityFederationEnabled, Stream<FederatedIdentityModel> federatedIdentities) {
        Map<String, String> federatedIdentitiesMap = null;
        if (identityFederationEnabled) {
            federatedIdentitiesMap = federatedIdentities.collect(Collectors.toMap(FederatedIdentityModel::getIdentityProvider,
                    FederatedIdentityModel::getUserId));
        }
        return new UserFullInvalidationEvent(userId, username, email, realmId, identityFederationEnabled, federatedIdentitiesMap);
    }

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    static UserFullInvalidationEvent protoFactory(String id, String username, String email, String realmId, boolean identityFederationEnabled, Map<String, String> federatedIdentities) {
        return new UserFullInvalidationEvent(id, username, email, realmId, identityFederationEnabled, federatedIdentities);
    }

    /** 返回联邦身份映射。 */
    public Map<String, String> getFederatedIdentities() {
        return federatedIdentities;
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return String.format("UserFullInvalidationEvent [ userId=%s, username=%s, email=%s ]", getId(), username, email);
    }

    /** 将用户完整失效所需的全部缓存键加入集合。 */
    @Override
    public void addInvalidations(UserCacheManager userCache, Set<String> invalidations) {
        userCache.fullUserInvalidation(getId(), username, email, realmId, identityFederationEnabled, federatedIdentities, invalidations);
    }

    /** 比较事件字段是否相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserFullInvalidationEvent that = (UserFullInvalidationEvent) o;
        return identityFederationEnabled == that.identityFederationEnabled && Objects.equals(username, that.username) && Objects.equals(email, that.email) && Objects.equals(realmId, that.realmId) && Objects.equals(federatedIdentities, that.federatedIdentities);
    }

    /** 返回基于事件字段的哈希值。 */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username, email, realmId, identityFederationEnabled, federatedIdentities);
    }

}
