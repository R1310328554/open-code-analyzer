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
 * 组属性更新时的领域缓存失效事件。
 * <p>
 * 实现 {@link RealmCacheInvalidationEvent}，仅失效该组的名称相关缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.GROUP_UPDATED_EVENT)
public class GroupUpdatedEvent extends InvalidationEvent implements RealmCacheInvalidationEvent {

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    GroupUpdatedEvent(String id) {
        super(id);
    }

    /** 创建组更新失效事件。 */
    public static GroupUpdatedEvent create(String groupId) {
        return new GroupUpdatedEvent(groupId);
    }

    /** 返回包含组 ID 的调试字符串。 */
    @Override
    public String toString() {
        return "GroupUpdatedEvent [ " + getId() + " ]";
    }

    /** 失效该组的名称相关缓存条目。 */
    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        realmCache.groupNameInvalidations(getId(), invalidations);
    }

}
