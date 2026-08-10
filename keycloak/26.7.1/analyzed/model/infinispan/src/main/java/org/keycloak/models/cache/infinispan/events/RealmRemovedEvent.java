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
import org.keycloak.models.cache.infinispan.RealmCacheManager;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 领域删除时的缓存失效事件。
 * <p>
 * 继承 {@link BaseRealmEvent}，通知 {@link RealmCacheManager}
 * 清除该领域及其关联实体的全部缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.REALM_REMOVED_EVENT)
public class RealmRemovedEvent extends BaseRealmEvent {

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    RealmRemovedEvent(String id, String realmName) {
        super(id, realmName);
    }

    /** 创建领域删除失效事件。 */
    public static RealmRemovedEvent create(String realmId, String realmName) {
        return new RealmRemovedEvent(realmId, realmName);
    }

    /** 失效该领域及其关联实体的全部缓存条目。 */
    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        realmCache.realmRemoval(getId(), realmName, invalidations);
    }
}
