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
import org.keycloak.models.cache.infinispan.RealmCacheManager;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 组新增时的领域缓存失效事件。
 * <p>
 * 实现 {@link RealmCacheInvalidationEvent}，失效组查询缓存；
 * 若存在父组，同时失效父组相关条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.GROUP_ADDED_EVENT)
public class GroupAddedEvent extends InvalidationEvent implements RealmCacheInvalidationEvent {

    /** 所属领域 ID。 */
    @ProtoField(2)
    final String realmId;
    /** 父组 ID；顶级组时为 null。 */
    @ProtoField(3)
    final String parentId; //parentId may be null

    /** 以组 ID、领域 ID 与父组 ID 构造事件。 */
    private GroupAddedEvent(String groupId, String realmId, String parentId) {
        super(groupId);
        this.realmId = Objects.requireNonNull(realmId);
        this.parentId = parentId;
    }

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    static GroupAddedEvent protoFactory(String id, String realmId, String parentId) {
        return new GroupAddedEvent(id, realmId, parentId);
    }

    /** 创建组新增失效事件。 */
    public static GroupAddedEvent create(String groupId, String parentId, String realmId) {
        return new GroupAddedEvent(groupId, realmId, parentId);
    }

    /** 返回包含领域 ID 与组 ID 的调试字符串。 */
    @Override
    public String toString() {
        return String.format("GroupAddedEvent [ realmId=%s, groupId=%s ]", realmId, getId());
    }

    /** 失效组查询缓存，并在有父组时失效父组条目。 */
    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        realmCache.groupQueriesInvalidations(realmId, invalidations);
        if (parentId != null) {
            invalidations.add(parentId);
        }
    }

    /** 比较领域 ID 与父组 ID 是否一致。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GroupAddedEvent that = (GroupAddedEvent) o;
        return realmId.equals(that.realmId) && Objects.equals(parentId, that.parentId);
    }

    /** 返回基于领域 ID 与父组 ID 的哈希值。 */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + realmId.hashCode();
        result = 31 * result + Objects.hashCode(parentId);
        return result;
    }
}
