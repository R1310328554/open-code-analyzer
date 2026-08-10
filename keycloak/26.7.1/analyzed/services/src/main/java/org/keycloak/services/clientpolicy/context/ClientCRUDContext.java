/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.context;

import org.keycloak.models.ClientModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;

/**
 * 客户端 CRUD 策略上下文：表示动态客户端注册或 Admin REST API 上的注册/读取/更新/注销请求。
 * <p>提供提议表示、目标客户端、认证用户/客户端及伴随 JWT 等可选信息，供条件与 Executor 评估。</p>
 */
public interface ClientCRUDContext extends ClientPolicyContext {

    /**
     * 返回用于创建新客户端或更新现有客户端的 {@link ClientRepresentation}。
     * <p>REGISTER/UPDATE 事件通常非 null；读取/注销事件可能为 null。</p>
     *
     * @return 提议的客户端表示
     */
    default ClientRepresentation getProposedClientRepresentation() {
        return null;
    }

    /**
     * 返回待更新/读取/注销的现有 {@link ClientModel}。
     * <p>REGISTER 事件时返回 null。</p>
     *
     * @return 目标客户端模型
     */
    default ClientModel getTargetClient() {
        return null;
    }

    /**
     * 返回已认证用户的 {@link UserModel}（如 Admin API 调用者）。
     *
     * @return 已认证用户
     */
    default UserModel getAuthenticatedUser() {
        return null;
    }

    /**
     * 返回已认证客户端的 {@link ClientModel}（如服务账户或令牌 azp 对应客户端）。
     *
     * @return 已认证客户端
     */
    default ClientModel getAuthenticatedClient() {
        return null;
    }

    /**
     * 返回注册/读取/更新/注销请求伴随的 {@link JsonWebToken}（初始访问令牌、注册访问令牌或 Admin Bearer 令牌等）。
     *
     * @return 伴随 JWT
     */
    default JsonWebToken getToken() {
        return null;
    }
}
