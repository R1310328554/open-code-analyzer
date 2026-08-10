/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.policy.evaluation;

import java.util.List;
import java.util.Map;

/**
 * 领域查询视图：供策略在评估时查询用户组成员、角色与属性，无需直接访问 {@link org.keycloak.models.RealmModel}。
 *
 * This interface provides methods to query information from a realm.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface Realm {

    /**
     * 检查用户是否为给定组成员（默认包含子组继承）。
     *
     * <p>Checks whether or not a user with the given <code>id</code> is a member of the given <code>group</code>.
     *
     * <p>This method will also consider memberships where the user is a member of any child group of the given <code>group</code>.
     * For instance, if user is member of <code>/Group A/Group B</code> and this method is checking if user is a member of <code>/Group A</code>
     * the result will be <code>true</code> given that the user is a member of a child group of <code>/Group A</code>.
     *
     * @param id the user id. It can be the id, username or email
     * @param group the group path. For instance, /Group A/Group B.
     * @return true if user is a member of the given group. Otherwise returns false.
     */
    default boolean isUserInGroup(String id, String group) {
        return isUserInGroup(id, group, true);
    }

    /**
     * 检查用户是否为给定组成员，可选是否考虑子组。
     *
     * Checks whether or not a user with the given <code>id</code> is a member of the given <code>group</code>.
     *
     * @param id the user id. It can be the id, username or email
     * @param group the group path. For instance, /Group A/Group B.
     * @param checkParent if true, this method returns true even though the user is not directly associated with the given group but a member of any child of the group.
     * @return true if user is a member of the given group. Otherwise returns false.
     */
    boolean isUserInGroup(String id, String group, boolean checkParent);

    /**
     * 检查用户是否拥有指定领域角色。
     *
     * Checks whether or not a user with the given <code>id</code> is granted with the given realm <code>role</code>.
     *
     * @param id the user id. It can be the id, username or email
     * @param role the role name
     * @return true if the user is granted with the role. Otherwise, false.
     */
    boolean isUserInRealmRole(String id, String role);

    /**
     * 检查用户是否拥有指定客户端角色。
     *
     * Checks whether or not a user with the given <code>id</code> is granted with the given client <code>role</code>.
     *
     * @param id the user id. It can be the id, username or email
     * @param clientId the client id
     * @param role the role name
     * @return true if the user is granted with the role. Otherwise, false.
     */
    boolean isUserInClientRole(String id, String clientId, String role);

    /**
     * 检查指定组是否拥有领域角色。
     *
     * Checks whether or not a <code>group</code> is granted with the given realm <code>role</code>.
     *
     * @param group the group path. For instance, /Group A/Group B.
     * @param role the role name
     * @return true if the group is granted with the role. Otherwise, false.
     */
    boolean isGroupInRole(String group, String role);

    /**
     * 返回用户拥有的全部领域角色名称。
     *
     * Returns all realm roles granted for a user with the given <code>id</code>.
     *
     * @param id the user id. It can be the id, username or email
     * @return the roles granted to the user
     */
    List<String> getUserRealmRoles(String id);

    /**
     * 返回用户在指定客户端下的全部角色名称。
     *
     * Returns all client roles granted for a user with the given <code>id</code>.
     *
     * @param id the user id. It can be the id, username or email
     * @param clientId the client id
     * @return the roles granted to the user
     */
    List<String> getUserClientRoles(String id, String clientId);

    /**
     * 返回用户所属的全部组路径。
     *
     * Returns all groups which the user with the given <code>id</code> is a member.
     *
     * @param id the user id. It can be the id, username or email
     * @return the groups which the user is a member
     */
    List<String> getUserGroups(String id);

    /**
     * 返回用户的全部属性映射。
     *
     * Returns all attributes associated with the a user with the given <code>id</code>.
     *
     * @param id the user id. It can be the id, username or email
     * @return a map with the attributes associated with the user
     */
    Map<String, List<String>> getUserAttributes(String id);
}
