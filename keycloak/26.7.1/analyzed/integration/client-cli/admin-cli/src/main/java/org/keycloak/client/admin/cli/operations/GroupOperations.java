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

import java.util.List;

import static org.keycloak.client.cli.util.HttpUtil.composeResourceUrl;
import static org.keycloak.client.cli.util.HttpUtil.doDeleteJSON;
import static org.keycloak.client.cli.util.HttpUtil.doPostJSON;

/**
 * 用户组相关 Admin REST 操作的静态工具类。
 * <p>
 * 提供按名称/路径解析组 ID，以及为用户组添加/移除领域与客户端角色的方法。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GroupOperations {

    /** 按组名精确搜索并返回组 ID。 */
    public static String getIdFromName(String rootUrl, String realm, String auth, String groupname) {
        return OperationUtils.getIdForType(rootUrl, realm, auth, "groups", "search", groupname, "name", () -> new String[] { "exact", "true" });
    }

    /** 按组路径属性查找并返回组 ID。 */
    public static String getIdFromPath(String rootUrl, String realm, String auth, String path) {
        return OperationUtils.getIdForType(rootUrl, realm, auth, "groups", "path", path, "path");
    }

    /** 为组添加领域角色映射。 */
    public static void addRealmRoles(String rootUrl, String realm, String auth, String groupid, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/realm");
        doPostJSON(resourceUrl, auth, roles);
    }

    /** 为组添加指定客户端的角色映射。 */
    public static void addClientRoles(String rootUrl, String realm, String auth, String groupid, String idOfClient, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/clients/" + idOfClient);
        doPostJSON(resourceUrl, auth, roles);
    }

    /** 从组移除领域角色映射。 */
    public static void removeRealmRoles(String rootUrl, String realm, String auth, String groupid, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/realm");
        doDeleteJSON(resourceUrl, auth, roles);
    }

    /** 从组移除指定客户端的角色映射。 */
    public static void removeClientRoles(String rootUrl, String realm, String auth, String groupid, String idOfClient, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/clients/" + idOfClient);
        doDeleteJSON(resourceUrl, auth, roles);
    }
}
