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
import org.keycloak.models.GroupModel;
import org.keycloak.models.cache.infinispan.RealmCacheManager;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 组删除时的领域缓存失效事件。
 * <p>
 * 实现 {@link RealmCacheInvalidationEvent}，失效组查询与组名称缓存；
 * 若存在父组，同时失效父组相关条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.GROUP_REMOVED_EVENT)
public class GroupRemovedEvent extends InvalidationEvent implements RealmCacheInvalidationEvent {

    /** 所属领域 ID。 */
    @ProtoField(2)
    final String realmId;
    /** 父组 ID；顶级组时为 null。 */
    @ProtoField(3)
    final String parentId;

    /** 以组 ID、领域 ID 与父组 ID 构造事件。 */
    public GroupRemovedEvent(String groupId, String realmId, String parentId) {
        super(groupId);
        this.realmId = Objects.requireNonNull(realmId);
        this.parentId = parentId;
    }

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    static GroupRemovedEvent protoFactory(String id, String realmId, String parentId) {
        return new GroupRemovedEvent(id, realmId, parentId);
    }

    /** 从组模型创建删除失效事件。 */
    public static GroupRemovedEvent create(GroupModel group, String realmId) {
        return new GroupRemovedEvent(group.getId(), realmId, group.getParentId());
    }

    /** 返回包含领域 ID、组 ID 与父组 ID 的调试字符串。 */
    @Override
    public String toString() {
        return String.format("GroupRemovedEvent [ realmId=%s, groupId=%s, parentId=%s ]", realmId, getId(), parentId);
    }

    /** 失效组查询、组名称及父组相关缓存条目。 */
    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        realmCache.groupQueriesInvalidations(realmId, invalidations);
        realmCache.groupNameInvalidations(getId(), invalidations);
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

        GroupRemovedEvent that = (GroupRemovedEvent) o;
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
