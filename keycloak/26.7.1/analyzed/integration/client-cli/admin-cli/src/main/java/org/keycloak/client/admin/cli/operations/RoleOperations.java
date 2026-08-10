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
package org.keycloak.client.admin.cli.operations;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.representations.idm.RoleRepresentation;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.keycloak.client.cli.util.HttpUtil.composeResourceUrl;
import static org.keycloak.client.cli.util.HttpUtil.doDeleteJSON;
import static org.keycloak.client.cli.util.HttpUtil.doGetJSON;
import static org.keycloak.client.cli.util.HttpUtil.doPostJSON;

/**
 * 角色相关 Admin REST 操作的静态工具类。
 * <p>
 * 封装领域/客户端角色的查询、复合角色管理及用户/组角色映射的 GET 调用。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class RoleOperations {

    /** Jackson 反序列化用的领域角色列表类型。 */
    public static class LIST_OF_ROLES extends ArrayList<RoleRepresentation>{};
    /** Jackson 反序列化用的 JSON 节点列表类型。 */
    public static class LIST_OF_NODES extends ArrayList<ObjectNode>{};

    /** 按角色名搜索并返回领域角色 ID。 */
    public static String getIdFromRoleName(String adminRoot, String realm, String auth, String rname) {
        return OperationUtils.getIdForType(adminRoot, realm, auth, "roles", "search", rname, "name");
    }

    /** 为复合角色添加子领域角色。 */
    public static void addRealmRoles(String rootUrl, String realm, String auth, String roleid, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "roles-by-id/" + roleid + "/composites");
        doPostJSON(resourceUrl, auth, roles);
    }

    /** 为复合角色添加子客户端角色（委托至领域复合端点）。 */
    public static void addClientRoles(String rootUrl, String realm, String auth, String roleid, List<?> roles) {
        addRealmRoles(rootUrl, realm, auth, roleid, roles);
    }

    /** 从复合角色移除子领域角色。 */
    public static void removeRealmRoles(String rootUrl, String realm, String auth, String roleid, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "roles-by-id/" + roleid + "/composites");
        doDeleteJSON(resourceUrl, auth, roles);
    }

    /** 从复合角色移除子客户端角色。 */
    public static void removeClientRoles(String rootUrl, String realm, String auth, String roleid, List<?> roles) {
        removeRealmRoles(rootUrl, realm, auth, roleid, roles);
    }

    /** 按角色 ID 查询领域角色名称。 */
    public static String getRoleNameFromId(String adminRoot, String realm, String auth, String rid) {
        return OperationUtils.getAttrForType(adminRoot, realm, auth, "roles", "id", rid, "id","name");
    }

    /** 按角色 ID 查询指定客户端下的角色名称。 */
    public static String getClientRoleNameFromId(String adminRoot, String realm, String auth, String cid, String rid) {
        return OperationUtils.getAttrForType(adminRoot, realm, auth, "clients/" + cid + "/roles", "id", rid, "id", "name");
    }

    /** 列出领域中全部角色（强类型表示）。 */
    public static List<RoleRepresentation> getRealmRoles(String rootUrl, String realm, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "roles");
        return doGetJSON(LIST_OF_ROLES.class, resourceUrl, auth);
    }

    /** 获取单个领域角色的 JSON 表示。 */
    public static ObjectNode getRealmRole(String rootUrl, String realm, String rolename, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "roles/" + rolename);
        return doGetJSON(ObjectNode.class, resourceUrl, auth);
    }

    /** 列出指定客户端下定义的全部角色。 */
    public static List<ObjectNode> getClientRoles(String rootUrl, String realm, String idOfClient, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "clients/" + idOfClient + "/roles");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取指定客户端下单个角色的 JSON 表示。 */
    public static ObjectNode getClientRole(String rootUrl, String realm, String idOfClient, String rolename, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "clients/" + idOfClient + "/roles/" + rolename);
        return doGetJSON(ObjectNode.class, resourceUrl, auth);
    }

    /** 列出领域中全部角色（JSON 节点形式，供 LocalSearch 使用）。 */
    public static List<ObjectNode> getRealmRolesAsNodes(String rootUrl, String realm, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "roles");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取用户已分配的领域角色映射。 */
    public static List<ObjectNode> getRealmRolesForUserAsNodes(String rootUrl, String realm, String userid, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/realm");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取用户领域角色的有效复合角色（含传递继承）。 */
    public static List<ObjectNode> getCompositeRealmRolesForUserAsNodes(String rootUrl, String realm, String userid, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/realm/composite");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取用户可分配但未持有的领域角色。 */
    public static List<ObjectNode> getAvailableRealmRolesForUserAsNodes(String rootUrl, String realm, String userid, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/realm/available");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取用户已分配的客户端角色映射。 */
    public static List<ObjectNode> getClientRolesForUserAsNodes(String rootUrl, String realm, String userid, String idOfClient, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/clients/" + idOfClient);
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取用户客户端角色的有效复合角色。 */
    public static List<ObjectNode> getCompositeClientRolesForUserAsNodes(String rootUrl, String realm, String userid, String idOfClient, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/clients/" + idOfClient + "/composite");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取用户可分配但未持有的客户端角色。 */
    public static List<ObjectNode> getAvailableClientRolesForUserAsNodes(String rootUrl, String realm, String userid, String idOfClient, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/clients/" + idOfClient + "/available");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取组已分配的领域角色映射。 */
    public static List<ObjectNode> getRealmRolesForGroupAsNodes(String rootUrl, String realm, String groupid, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/realm");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取组领域角色的有效复合角色。 */
    public static List<ObjectNode> getCompositeRealmRolesForGroupAsNodes(String rootUrl, String realm, String groupid, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/realm/composite");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取组可分配但未持有的领域角色。 */
    public static List<ObjectNode> getAvailableRealmRolesForGroupAsNodes(String rootUrl, String realm, String groupid, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/realm/available");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取组已分配的客户端角色映射。 */
    public static List<ObjectNode> getClientRolesForGroupAsNodes(String rootUrl, String realm, String groupid, String idOfClient, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/clients/" + idOfClient);
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取组客户端角色的有效复合角色。 */
    public static List<ObjectNode> getCompositeClientRolesForGroupAsNodes(String rootUrl, String realm, String groupid, String idOfClient, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/clients/" + idOfClient + "/composite");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }

    /** 获取组可分配但未持有的客户端角色。 */
    public static List<ObjectNode> getAvailableClientRolesForGroupAsNodes(String rootUrl, String realm, String groupid, String idOfClient, String auth) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/clients/" + idOfClient + "/available");
        return doGetJSON(LIST_OF_NODES.class, resourceUrl, auth);
    }
}
