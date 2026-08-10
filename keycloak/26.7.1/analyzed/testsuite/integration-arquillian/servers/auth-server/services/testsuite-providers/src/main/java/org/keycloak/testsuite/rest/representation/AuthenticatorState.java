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

package org.keycloak.testsuite.rest.representation;

import java.io.Serializable;

/**
 * 认证器状态表示，用于测试 REST 端点传递客户端与用户上下文。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class AuthenticatorState implements Serializable {
    /** OAuth 客户端 ID。 */
    private String clientId;
    /** 当前用户名。 */
    private String username;

    /** 返回客户端 ID。 */
    public String getClientId() {
        return clientId;
    }

    /** 设置客户端 ID。 */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** 返回用户名。 */
    public String getUsername() {
        return username;
    }

    /** 设置用户名。 */
    public void setUsername(String username) {
        this.username = username;
    }
}
