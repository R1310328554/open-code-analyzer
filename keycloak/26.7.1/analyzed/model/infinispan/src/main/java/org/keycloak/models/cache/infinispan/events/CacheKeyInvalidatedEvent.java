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

package org.keycloak.models.cache.infinispan.events;

import java.util.Set;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.RealmCacheManager;
import org.keycloak.models.cache.infinispan.UserCacheManager;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 按缓存键直接失效的集群事件。
 * <p>
 * 当某缓存条目需精确失效时广播此事件，同时作用于 {@link RealmCacheManager} 与 {@link UserCacheManager}。
 */
@ProtoTypeId(Marshalling.CACHE_KEY_INVALIDATION_EVENT)
public class CacheKeyInvalidatedEvent extends InvalidationEvent implements RealmCacheInvalidationEvent, UserCacheInvalidationEvent {

    /** 以缓存键 ID 构造失效事件。 */
    @ProtoFactory
    public CacheKeyInvalidatedEvent(String id) {
        super(id);
    }

    /** 将指定缓存键加入领域缓存失效集合。 */
    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        realmCache.invalidateCacheKey(getId(), invalidations);
    }

    /** 将指定缓存键加入用户缓存失效集合。 */
    @Override
    public void addInvalidations(UserCacheManager userCache, Set<String> invalidations) {
        userCache.invalidateCacheKey(getId(), invalidations);
    }
}
