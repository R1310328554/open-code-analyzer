/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 客户端作用域删除时的领域缓存失效事件。
 * <p>
 * 继承 {@link BaseClientScopeEvent}，通知 {@link RealmCacheManager} 清除与已删除作用域相关的缓存条目。
 */
@ProtoTypeId(Marshalling.CLIENT_SCOPE_REMOVED_EVENT)
public class ClientScopeRemovedEvent extends BaseClientScopeEvent {

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    ClientScopeRemovedEvent(String id, String realmId) {
        super(id, realmId);
    }

    /** 创建客户端作用域删除失效事件。 */
    public static ClientScopeRemovedEvent create(String clientScopeId, String realmId) {
        return new ClientScopeRemovedEvent(clientScopeId, realmId);
    }

    /** 将删除作用域引发的失效键加入集合。 */
    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        realmCache.clientScopeRemoval(realmId, invalidations);
    }
}
