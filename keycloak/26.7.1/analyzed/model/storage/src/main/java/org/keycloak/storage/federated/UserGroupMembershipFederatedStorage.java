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

import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;

/**
 * 联邦用户组成员关系存储接口，管理外部用户与 Keycloak 组之间的成员关系。
 *
 * <p>当用户来自外部存储且组归属无法由该存储直接维护时，通过联邦存储持久化组成员信息。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserGroupMembershipFederatedStorage {

    /**
     * 获取联邦用户所属的全部组。
     *
     * @param realm a reference to the realm.
     * @param userId the user identifier.
     * @return a non-null {@code Stream} of groups.
     */
    Stream<GroupModel> getGroupsStream(RealmModel realm, String userId);

    /** 将联邦用户加入指定组。 */
    void joinGroup(RealmModel realm, String userId, GroupModel group);

    /** 将联邦用户移出指定组。 */
    void leaveGroup(RealmModel realm, String userId, GroupModel group);

    /**
     * 获取指定 {@code realm} 中属于 {@code group} 的联邦用户 ID。
     *
     * @param realm a reference to the realm.
     * @param group a reference to the group whose federated members are being searched.
     * @param firstResult first result to return. Ignored if negative or {@code null}.
     * @param max maximum number of results to return. Ignored if negative or {@code null}.
     * @return a non-null {@code Stream} of federated user ids that are members of the group in the realm.
     */
    Stream<String> getMembershipStream(RealmModel realm, GroupModel group, Integer firstResult, Integer max);

    /**
     * @deprecated This interface is no longer necessary; collection-based methods were removed from the parent interface
     * and therefore the parent interface can be used directly
     */
    @Deprecated
    interface Streams extends UserGroupMembershipFederatedStorage {
    }
}
