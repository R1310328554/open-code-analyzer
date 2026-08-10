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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.keycloak.client.cli.util.Headers;
import org.keycloak.client.cli.util.HeadersBody;
import org.keycloak.client.cli.util.HeadersBodyStatus;
import org.keycloak.client.cli.util.HttpUtil;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.util.JsonSerialization;

import static org.keycloak.client.cli.util.HttpUtil.composeResourceUrl;
import static org.keycloak.client.cli.util.HttpUtil.doDeleteJSON;
import static org.keycloak.client.cli.util.HttpUtil.doPostJSON;

/**
 * 用户相关 Admin REST 操作的静态工具类。
 * <p>
 * 封装用户角色映射的增删及密码重置等常用调用。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class UserOperations {

    /** 为用户添加领域角色映射。 */
    public static void addRealmRoles(String rootUrl, String realm, String auth, String userid, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/realm");
        doPostJSON(resourceUrl, auth, roles);
    }

    /** 为用户添加指定客户端的角色映射。 */
    public static void addClientRoles(String rootUrl, String realm, String auth, String userid, String idOfClient, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/clients/" + idOfClient);
        doPostJSON(resourceUrl, auth, roles);
    }

    /** 从用户移除领域角色映射。 */
    public static void removeRealmRoles(String rootUrl, String realm, String auth, String userid, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/realm");
        doDeleteJSON(resourceUrl, auth, roles);
    }

    /** 从用户移除指定客户端的角色映射。 */
    public static void removeClientRoles(String rootUrl, String realm, String auth, String userid, String idOfClient, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/clients/" + idOfClient);
        doDeleteJSON(resourceUrl, auth, roles);
    }

    /**
     * 重置用户密码。
     *
     * @param rootUrl   Admin REST 根 URL
     * @param realm     目标领域
     * @param auth      Bearer 令牌
     * @param userid    用户 ID
     * @param password  新密码明文
     * @param temporary 是否为临时密码
     */
    public static void resetUserPassword(String rootUrl, String realm, String auth, String userid, String password, boolean temporary) {

        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/reset-password");

        Headers headers = new Headers();
        if (auth != null) {
            headers.add("Authorization", auth);
        }
        headers.add("Content-Type", "application/json");

        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setType("password");
        credentials.setTemporary(temporary);
        credentials.setValue(password);

        HeadersBodyStatus response;

        byte[] body;
        try {
            body = JsonSerialization.writeValueAsBytes(credentials);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

        try {
            response = HttpUtil.doRequest("put", resourceUrl, new HeadersBody(headers, new ByteArrayInputStream(body)));
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: PUT " + resourceUrl + "\n" + new String(body), e);
        }

        response.checkSuccess();
    }

    /** 按用户名精确搜索并返回用户 ID。 */
    public static String getIdFromUsername(String rootUrl, String realm, String auth, String username) {
        return OperationUtils.getIdForType(rootUrl, realm, auth, "users", "username", username, "username",
                () -> new String[] {"exact", "true"});
    }
}
