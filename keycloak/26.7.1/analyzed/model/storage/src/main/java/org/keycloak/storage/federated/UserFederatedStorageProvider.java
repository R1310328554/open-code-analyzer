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

package org.keycloak.storage.federated;

import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * 用户联邦存储 Provider：聚合属性、Broker 链接、同意书、组/角色映射、凭证等联邦数据的读写能力。
 * <p>
 * 外部用户存储（LDAP 等）通常仅提供用户名等基础字段；其余数据通过本 Provider 持久化到 Keycloak 本地存储。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserFederatedStorageProvider extends Provider,
        UserAttributeFederatedStorage,
        UserBrokerLinkFederatedStorage,
        UserConsentFederatedStorage,
        UserNotBeforeFederatedStorage,
        UserGroupMembershipFederatedStorage,
        UserRequiredActionsFederatedStorage,
        UserRoleMappingsFederatedStorage,
        UserFederatedUserCredentialStore,
        UserVerifiableCredentialFederatedStorage {

    /**
     * 获取 realm 中全部联邦用户的 ID 列表（分页）。
     *
     * @param realm a reference to the realm.
     * @param first first result to return. Ignored if negative or {@code null}.
     * @param max maximum number of results to return. Ignored if negative or {@code null}.
     * @return a non-null {@link Stream} of federated user ids.
     */
    Stream<String> getStoredUsersStream(RealmModel realm, Integer first, Integer max);

    /**
     * 统计 realm 中联邦用户总数。
     *
     * @param realm 所属 realm
     * @return 联邦用户数量
     */
    int getStoredUsersCount(RealmModel realm);

    /** realm 删除前的回调，清理该 realm 下的全部联邦存储数据。 */
    void preRemove(RealmModel realm);

    /** 组删除前的回调，清理相关联邦组映射。 */
    void preRemove(RealmModel realm, GroupModel group);

    /** 角色删除前的回调，清理相关联邦角色映射。 */
    void preRemove(RealmModel realm, RoleModel role);

    /** 客户端删除前的回调，清理相关联邦同意书等数据。 */
    void preRemove(RealmModel realm, ClientModel client);

    /** 协议映射器删除前的回调。 */
    void preRemove(ProtocolMapperModel protocolMapper);

    /** 客户端作用域删除前的回调。 */
    void preRemove(ClientScopeModel clientScope);

    /** 用户删除前的回调，清理该用户的全部联邦存储数据。 */
    void preRemove(RealmModel realm, UserModel user);

    /** 组件（如用户存储 Provider）删除前的回调。 */
    void preRemove(RealmModel realm, ComponentModel model);

    /**
     * @deprecated 父接口已移除基于集合的方法，可直接使用本接口，无需再继承此 Streams 子接口。
     */
    @Deprecated
    interface Streams extends UserFederatedStorageProvider,
            UserAttributeFederatedStorage.Streams,
            UserBrokerLinkFederatedStorage.Streams,
            UserConsentFederatedStorage.Streams,
            UserFederatedUserCredentialStore.Streams,
            UserGroupMembershipFederatedStorage.Streams,
            UserRequiredActionsFederatedStorage.Streams,
            UserRoleMappingsFederatedStorage.Streams {
    }
}
