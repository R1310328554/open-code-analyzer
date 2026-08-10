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
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 客户端更新时的领域缓存失效事件。
 * <p>
 * 继承 {@link BaseClientEvent}，携带客户端公开标识（clientId），
 * 通知 {@link RealmCacheManager} 刷新与更新客户端相关的缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.CLIENT_UPDATED_EVENT)
public class ClientUpdatedEvent extends BaseClientEvent {

    /** 客户端公开标识（clientId）。 */
    @ProtoField(3)
    final String clientId;

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    ClientUpdatedEvent(String id, String realmId, String clientId) {
        super(id, realmId);
        this.clientId = clientId;
    }

    /** 创建客户端更新失效事件。 */
    public static ClientUpdatedEvent create(String clientUuid, String clientId, String realmId) {
        return new ClientUpdatedEvent(clientUuid, realmId, clientId);
    }

    /** 返回包含领域 ID、客户端 UUID 与 clientId 的调试字符串。 */
    @Override
    public String toString() {
        return String.format("ClientUpdatedEvent [ realmId=%s, clientUuid=%s, clientId=%s ]", realmId, getId(), clientId);
    }

    /** 将更新客户端引发的失效键加入集合。 */
    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        realmCache.clientUpdated(realmId, getId(), clientId, invalidations);
    }
}
