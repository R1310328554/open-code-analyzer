/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.forms.login.freemarker.model;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.keycloak.crypto.JavaAlgorithm;
import org.keycloak.jose.jws.crypto.HashUtils;

/**
 * 认证会话标识 Bean：向模板暴露根认证会话 ID、其 SHA-384 哈希与浏览器标签页 ID。
 * <p>供前端脚本在不泄露完整会话 ID 的情况下关联会话。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthenticationSessionBean {

    private final String authSessionId;
    private final String authSessionIdHash;
    private final String tabId;

    /** @param authSessionId 根认证会话 ID @param tabId 浏览器标签页 ID */
    public AuthenticationSessionBean(String authSessionId, String tabId) {
        this.authSessionId = authSessionId;
        this.authSessionIdHash = Base64.getEncoder().withoutPadding().encodeToString(HashUtils.hash(JavaAlgorithm.SHA384, authSessionId.getBytes(StandardCharsets.UTF_8)));
        this.tabId = tabId;
    }

    /** @return 根认证会话 ID */
    public String getAuthSessionId() {
        return authSessionId;
    }

    /** @return 会话 ID 的 Base64(SHA-384) 哈希 */
    public String getAuthSessionIdHash() {
        return authSessionIdHash;
    }

    /** @return 浏览器标签页 ID */
    public String getTabId() {
        return tabId;
    }

}
