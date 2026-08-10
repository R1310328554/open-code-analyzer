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

import java.util.Objects;
import java.util.Set;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.cache.infinispan.UserCacheManager;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 用户联邦身份链接被移除时的缓存失效事件。
 * <p>
 * 当用户与外部身份提供者（IdP）的关联链接被删除时发布，通知 {@link UserCacheManager}
 * 失效该用户及其联邦身份相关的缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.USER_FEDERATION_LINK_REMOVED_EVENT)
public class UserFederationLinkRemovedEvent extends InvalidationEvent implements UserCacheInvalidationEvent {

    /** 所属领域 ID。 */
    final String realmId;
    /** 被移除链接对应的身份提供者 ID（别名）。 */
    final String identityProviderId;
    /** 该链接在外部 IdP 侧的用户标识。 */
    final String socialUserId;

    /** 构造联邦身份链接移除事件。 */
    private UserFederationLinkRemovedEvent(String id, String realmId, String identityProviderId, String socialUserId) {
        super(id);
        this.realmId = Objects.requireNonNull(realmId);
        // identityProviderId 可能为 null
        this.identityProviderId = identityProviderId;
        this.socialUserId = socialUserId;
    }

    /** 根据用户 ID、领域及被移除的联邦身份模型创建事件。 */
    public static UserFederationLinkRemovedEvent create(String userId, String realmId, FederatedIdentityModel socialLink) {
        String identityProviderId = socialLink == null ? null : socialLink.getIdentityProvider();
        String socialUserId = socialLink == null ? null : socialLink.getUserId();
        return new UserFederationLinkRemovedEvent(userId, realmId, identityProviderId, socialUserId);
    }

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    static UserFederationLinkRemovedEvent protoFactory(String id, String realmId, String identityProviderId, String socialUserId) {
        return new UserFederationLinkRemovedEvent(id, realmId, identityProviderId, socialUserId);
    }

    /** 返回所属领域 ID。 */
    @ProtoField(2)
    public String getRealmId() {
        return realmId;
    }

    /** 返回被移除链接的身份提供者 ID。 */
    @ProtoField(3)
    public String getIdentityProviderId() {
        return identityProviderId;
    }

    /** 返回外部 IdP 侧的用户标识。 */
    @ProtoField(4)
    public String getSocialUserId() {
        return socialUserId;
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return String.format("UserFederationLinkRemovedEvent [ userId=%s, identityProviderId=%s, socialUserId=%s ]", getId(), identityProviderId, socialUserId);
    }

    /** 将联邦身份链接移除引发的失效键加入集合。 */
    @Override
    public void addInvalidations(UserCacheManager userCache, Set<String> invalidations) {
        userCache.federatedIdentityLinkRemovedInvalidation(getId(), realmId, identityProviderId, socialUserId, invalidations);
    }

    /** 比较事件字段是否相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserFederationLinkRemovedEvent that = (UserFederationLinkRemovedEvent) o;
        return Objects.equals(realmId, that.realmId) && Objects.equals(identityProviderId, that.identityProviderId) && Objects.equals(socialUserId, that.socialUserId);
    }

    /** 返回基于事件字段的哈希值。 */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), realmId, identityProviderId, socialUserId);
    }

}
