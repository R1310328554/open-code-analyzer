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
 * 用户同意书更新时的用户缓存失效事件。
 * <p>
 * 实现 {@link UserCacheInvalidationEvent}，以用户 ID 为键，
 * 刷新与该用户 OAuth 同意书变更相关的缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.USER_CONSENTS_UPDATED_EVENT)
public class UserConsentsUpdatedEvent extends InvalidationEvent implements UserCacheInvalidationEvent {

    /** 以用户 ID 构造同意书更新失效事件。 */
    private UserConsentsUpdatedEvent(String id) {
        super(id);
    }

    /** Protobuf 反序列化工厂方法，同时作为对外创建入口。 */
    @ProtoFactory
    public static UserConsentsUpdatedEvent create(String id) {
        return new UserConsentsUpdatedEvent(id);
    }

    /** 返回包含用户 ID 的调试字符串。 */
    @Override
    public String toString() {
        return String.format("UserConsentsUpdatedEvent [ userId=%s ]", getId());
    }

    /** 刷新与该用户同意书变更相关的缓存条目。 */
    @Override
    public void addInvalidations(UserCacheManager userCache, Set<String> invalidations) {
        userCache.consentInvalidation(getId(), invalidations);
    }

}
