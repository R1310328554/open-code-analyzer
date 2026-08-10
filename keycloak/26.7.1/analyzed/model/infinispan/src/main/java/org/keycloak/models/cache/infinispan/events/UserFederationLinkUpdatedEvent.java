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
 * 用户联邦身份链接被更新时的缓存失效事件。
 * <p>
 * 当用户与外部身份提供者的关联链接发生变更时发布，通知 {@link UserCacheManager}
 * 失效该用户的联邦身份链接缓存。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.USER_FEDERATION_LINK_UPDATED_EVENT)
public class UserFederationLinkUpdatedEvent extends InvalidationEvent implements UserCacheInvalidationEvent {

    /** 构造联邦身份链接更新事件。 */
    private UserFederationLinkUpdatedEvent(String id) {
        super(id);
    }

    /** 创建指定用户 ID 的联邦身份链接更新事件。 */
    @ProtoFactory
    public static UserFederationLinkUpdatedEvent create(String id) {
        return new UserFederationLinkUpdatedEvent(id);
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return String.format("UserFederationLinkUpdatedEvent [ userId=%s ]", getId());
    }

    /** 将联邦身份链接更新引发的失效键加入集合。 */
    @Override
    public void addInvalidations(UserCacheManager userCache, Set<String> invalidations) {
        userCache.federatedIdentityLinkUpdatedInvalidation(getId(), invalidations);
    }

}
