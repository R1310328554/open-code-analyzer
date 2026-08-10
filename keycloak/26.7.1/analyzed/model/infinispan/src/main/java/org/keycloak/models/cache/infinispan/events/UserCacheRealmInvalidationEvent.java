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

import java.util.Set;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.UserCacheManager;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 按领域批量失效用户缓存的事件。
 * <p>
 * 实现 {@link UserCacheInvalidationEvent}，以领域 ID 为键，
 * 清除该领域下全部用户的缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.USER_CACHE_REALM_INVALIDATION_EVENT)
public class UserCacheRealmInvalidationEvent  extends InvalidationEvent implements UserCacheInvalidationEvent {

    /** 以领域 ID 构造批量失效事件。 */
    private UserCacheRealmInvalidationEvent(String id) {
        super(id);
    }

    /** Protobuf 反序列化工厂方法，同时作为对外创建入口。 */
    @ProtoFactory
    public static UserCacheRealmInvalidationEvent create(String id) {
        return new UserCacheRealmInvalidationEvent(id);
    }

    /** 返回包含领域 ID 的调试字符串。 */
    @Override
    public String toString() {
        return String.format("UserCacheRealmInvalidationEvent [ realmId=%s ]", getId());
    }

    /** 失效该领域下全部用户的缓存条目。 */
    @Override
    public void addInvalidations(UserCacheManager userCache, Set<String> invalidations) {
        userCache.invalidateRealmUsers(getId(), invalidations);
    }

}
