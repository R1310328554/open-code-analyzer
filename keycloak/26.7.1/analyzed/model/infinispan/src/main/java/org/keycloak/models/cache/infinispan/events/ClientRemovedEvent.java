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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.cache.infinispan.RealmCacheManager;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 客户端删除时的领域缓存失效事件。
 * <p>
 * 继承 {@link BaseClientEvent}，携带客户端 ID 与其角色映射，
 * 依次失效客户端本身及依赖其角色的缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.CLIENT_REMOVED_EVENT)
public class ClientRemovedEvent extends BaseClientEvent {

    /** 客户端公开标识（clientId）。 */
    @ProtoField(3)
    final String clientId;
    /** 客户端角色映射：roleId → roleName。 */
    @ProtoField(4)
    final Map<String, String> clientRoles;

    /** Protobuf 反序列化工厂方法。 */
    @ProtoFactory
    ClientRemovedEvent(String id, String realmId, String clientId, Map<String, String> clientRoles) {
        super(id, realmId);
        this.clientId = Objects.requireNonNull(clientId);
        this.clientRoles = Objects.requireNonNull(clientRoles);
    }


    /** 从客户端模型创建删除失效事件，自动收集其角色映射。 */
    public static ClientRemovedEvent create(ClientModel client) {
        var clientRoles = client.getRolesStream().collect(Collectors.toMap(RoleModel::getId, RoleModel::getName));
        return new ClientRemovedEvent(client.getId(), client.getRealm().getId(), client.getClientId(), clientRoles);
    }


    /** 返回包含客户端 ID 与角色映射的调试字符串。 */
    @Override
    public String toString() {
        return String.format("ClientRemovedEvent [ realmId=%s, clientUuid=%s, clientId=%s, clientRoleIds=%s ]", realmId, getId(), clientId, clientRoles);
    }

    /** 失效客户端及其全部客户端角色的相关缓存条目。 */
    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        realmCache.clientRemoval(realmId, getId(), clientId, invalidations);

        // 单独遍历所有客户端角色，失效依赖它们的缓存记录
        for (Map.Entry<String, String> clientRole : clientRoles.entrySet()) {
            String roleId = clientRole.getKey();
            String roleName = clientRole.getValue();
            realmCache.roleRemoval(roleId, roleName, getId(), invalidations);
        }
    }

    /** 比较客户端 ID 与角色映射是否一致。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ClientRemovedEvent that = (ClientRemovedEvent) o;
        return clientId.equals(that.clientId) &&
                clientRoles.equals(that.clientRoles);
    }

    /** 返回基于客户端 ID 与角色映射的哈希值。 */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + clientId.hashCode();
        result = 31 * result + clientRoles.hashCode();
        return result;
    }
}
