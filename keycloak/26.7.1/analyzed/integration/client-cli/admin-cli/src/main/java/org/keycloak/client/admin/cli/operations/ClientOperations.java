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

/**
 * 客户端相关 Admin REST 操作的静态工具类。
 * <p>
 * 封装按 {@code clientId} 查询客户端内部 ID 等常用调用。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ClientOperations {

    /**
     * 根据 {@code clientId} 属性查找客户端的内部 ID。
     *
     * @param rootUrl  Admin REST 根 URL
     * @param realm    目标领域
     * @param auth     Bearer 令牌（可为 null）
     * @param clientId 客户端标识符
     * @return 客户端 UUID
     */
    public static String getIdFromClientId(String rootUrl, String realm, String auth, String clientId) {
        return OperationUtils.getIdForType(rootUrl, realm, auth, "clients", "clientId", clientId, "clientId");
    }
}
