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
 * 角色新增时的领域缓存失效事件。
 * <p>
 * 继承 {@link BaseRoleEvent}，携带角色名称，
 * 通知 {@link RealmCacheManager} 刷新与新增角色相关的缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.ROLE_ADDED_EVENT)
public class RoleAddedEvent extends BaseRoleEvent {

    /** 角色名称。 */
    @ProtoField(3)
    final String roleName;

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    RoleAddedEvent(String id, String containerId, String roleName) {
        super(id, containerId);
        this.roleName = roleName;
    }

    /** 创建角色新增失效事件。 */
    public static RoleAddedEvent create(String roleId, String containerId, String roleName) {
        return new RoleAddedEvent(roleId, containerId, roleName);
    }

    /** 将新增角色引发的失效键加入集合。 */
    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        realmCache.roleAdded(containerId, roleName, invalidations);
    }
}
