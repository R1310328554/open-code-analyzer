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
import org.keycloak.models.cache.infinispan.UserCacheManager;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 用户基本信息更新时的缓存失效事件。
 * <p>
 * 当用户名或邮箱等属性变更时发布，通知 {@link UserCacheManager}
 * 失效该用户 ID 及按用户名/邮箱查询的缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.USER_UPDATED_EVENT)
public class UserUpdatedEvent extends InvalidationEvent implements UserCacheInvalidationEvent {

    /** 更新后的用户名。 */
    @ProtoField(2)
    final String username;
    /** 更新后的邮箱。 */
    @ProtoField(3)
    final String email;
    /** 所属领域 ID。 */
    @ProtoField(4)
    final String realmId;

    /** 构造用户更新事件。 */
    private UserUpdatedEvent(String id, String username, String email, String realmId) {
        super(id);
        this.username = Objects.requireNonNull(username);
        this.email = email;
        this.realmId = Objects.requireNonNull(realmId);
    }

    /** 创建指定用户属性变更的更新事件。 */
    public static UserUpdatedEvent create(String id, String username, String email, String realmId) {
        return new UserUpdatedEvent(id, username, email, realmId);
    }

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    static UserUpdatedEvent protoFactory(String id, String username, String email, String realmId) {
        return new UserUpdatedEvent(id, username, email, realmId);
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return String.format("UserUpdatedEvent [ userId=%s, username=%s, email=%s ]", getId(), username, email);
    }

    /** 将用户更新引发的失效键加入集合。 */
    @Override
    public void addInvalidations(UserCacheManager userCache, Set<String> invalidations) {
        userCache.userUpdatedInvalidations(getId(), username, email, realmId, invalidations);
    }

    /** 比较事件字段是否相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserUpdatedEvent that = (UserUpdatedEvent) o;
        return Objects.equals(username, that.username) && Objects.equals(email, that.email) && Objects.equals(realmId, that.realmId);
    }

    /** 返回基于事件字段的哈希值。 */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username, email, realmId);
    }

}
