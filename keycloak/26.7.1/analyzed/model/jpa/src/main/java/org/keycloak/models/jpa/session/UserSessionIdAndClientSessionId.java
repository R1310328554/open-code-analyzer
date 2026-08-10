/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.jpa.session;

import java.util.Objects;

/**
 * 表示 {@link org.keycloak.models.AuthenticatedClientSessionModel} 的查询投影：
 * 用户会话 ID 及其关联的客户端会话标识。
 *
 * @param userSessionId         用户会话 ID（永不为 null）
 * @param clientSessionId       内部客户端的 client ID，或外部客户端的 "external"（LEFT JOIN 可能为 null）
 * @param clientStorageProvider 外部客户端的 storage provider（LEFT JOIN 可能为 null）
 * @param externalClientId      外部客户端 ID（LEFT JOIN 可能为 null）
 */
public record UserSessionIdAndClientSessionId(String userSessionId, String clientSessionId,
                                              String clientStorageProvider, String externalClientId) {

    public UserSessionIdAndClientSessionId {
        Objects.requireNonNull(userSessionId, "userSessionId");
        // clientSessionId、clientStorageProvider、externalClientId 在 LEFT JOIN 无客户端会话时可为 null
    }


}
