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
 * 角色删除时的领域缓存失效事件。
 * <p>
 * 继承 {@link BaseRoleEvent}，携带角色名称，
 * 通知 {@link RealmCacheManager} 清除与该角色相关的缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.ROLE_REMOVED_EVENT)
public class RoleRemovedEvent extends BaseRoleEvent {

    /** 角色名称。 */
    @ProtoField(3)
    final String roleName;

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    RoleRemovedEvent(String id, String containerId, String roleName) {
        super(id, containerId);
        this.roleName = Objects.requireNonNull(roleName);
    }

    /** 创建角色删除失效事件。 */
    public static RoleRemovedEvent create(String roleId, String roleName, String containerId) {
        return new RoleRemovedEvent(roleId, containerId, roleName);
    }

    /** 清除与该角色相关的缓存条目。 */
    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        realmCache.roleRemoval(getId(), roleName, containerId, invalidations);
    }

    /** 比较角色名称是否一致。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RoleRemovedEvent that = (RoleRemovedEvent) o;
        return roleName.equals(that.roleName);
    }

    /** 返回基于角色名称的哈希值。 */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + roleName.hashCode();
        return result;
    }
}
