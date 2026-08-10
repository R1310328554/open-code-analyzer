/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authentication.actiontoken.execactions;

import java.util.LinkedList;
import java.util.List;

import org.keycloak.authentication.actiontoken.DefaultActionToken;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 「执行必需操作」操作令牌，携带待执行的 required action 列表与可选重定向 URI。
 *
 * @author hmlnarik
 */
public class ExecuteActionsActionToken extends DefaultActionToken {

    /** 令牌类型标识 execute-actions。 */
    public static final String TOKEN_TYPE = "execute-actions";
    /** JSON 字段：必需操作 ID 列表。 */
    private static final String JSON_FIELD_REQUIRED_ACTIONS = "rqac";
    /** JSON 字段：完成后重定向 URI。 */
    private static final String JSON_FIELD_REDIRECT_URI = "reduri";

    @JsonProperty(JSON_FIELD_REQUIRED_ACTIONS)
    /** 待执行的必需操作 ID 列表。 */
    private List<String> requiredActions;

    @JsonProperty(JSON_FIELD_REDIRECT_URI)
    /** 操作完成后的重定向地址。 */
    private String redirectUri;

    /** 构造执行必需操作令牌（不含邮箱）。 */
    public ExecuteActionsActionToken(String userId, int absoluteExpirationInSecs, List<String> requiredActions, String redirectUri, String clientId) {
        super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null);
        setRequiredActions(requiredActions == null ? new LinkedList<>() : new LinkedList<>(requiredActions));
        setRedirectUri(redirectUri);
        this.issuedFor = clientId;
    }

    /** 构造执行必需操作令牌（含邮箱校验字段）。 */
    public ExecuteActionsActionToken(String userId, String email, int absoluteExpirationInSecs, List<String> requiredActions, String redirectUri, String clientId) {
        this(userId, absoluteExpirationInSecs, requiredActions, redirectUri, clientId);
        setEmail(email);
    }

    private ExecuteActionsActionToken() {
    }

    /** @return 必需操作 ID 列表 */
    public List<String> getRequiredActions() {
        return requiredActions;
    }

    public void setRequiredActions(List<String> requiredActions) {
        this.requiredActions = requiredActions;
    }

    /** @return 重定向 URI */
    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
