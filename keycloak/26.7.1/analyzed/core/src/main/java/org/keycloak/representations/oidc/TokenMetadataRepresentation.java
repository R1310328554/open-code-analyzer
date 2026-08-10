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
package org.keycloak.representations.oidc;

import org.keycloak.representations.AccessToken;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OAuth 2.0 Token Introspection（RFC 7662）响应的扩展表示，继承 {@link AccessToken} 并附加
 * introspection 端点特有的 {@code active}、{@code username} 与 {@code client_id} 字段。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class TokenMetadataRepresentation extends AccessToken {

    /** 令牌是否仍处于有效/未撤销状态。 */
    @JsonProperty("active")
    private boolean active;

    /** 令牌所属用户的用户名。 */
    @JsonProperty("username")
    private String userName;

    /** 令牌关联的客户端 ID。 */
    @JsonProperty("client_id")
    private String clientId;

    /** @return 令牌是否有效 */
    public boolean isActive() {
        return this.active;
    }

    /** @param active 令牌是否有效 */
    public void setActive(boolean active) {
        this.active = active;
    }

    /** @return 用户名 */
    public String getUserName() {
        return this.userName;
    }

    /** @param userName 用户名 */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** @return 客户端 ID */
    public String getClientId() {
        return this.clientId;
    }

    /** @param clientId 客户端 ID */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
