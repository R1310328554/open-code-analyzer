/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.keycloak.util.JsonSerialization;

/**
 * rememberMe 列迁移用的查询投影：会话/用户标识及从 JSON {@code data} 解析出的 rememberMe 值。
 *
 * @param sessionAndUser 用户会话与用户 ID
 * @param rememberMe     是否启用 rememberMe
 */
record UserSessionIdAndRememberMe(UserSessionAndUser sessionAndUser, boolean rememberMe) {

    UserSessionIdAndRememberMe {
        Objects.requireNonNull(sessionAndUser);
    }

    /** 从 (userSessionId, userId, data) 三列 Object[] 投影构造实例。 */
    static UserSessionIdAndRememberMe fromQueryProjection(Object[] projection) {
        assert projection.length == 3;
        assert projection[0] != null;
        assert projection[1] != null;
        assert projection[2] != null;
        try {
            String sessionId = String.valueOf(projection[0]);
            String userId = String.valueOf(projection[1]);
            String data = String.valueOf(projection[2]);
            Map<?, ?> values = JsonSerialization.readValue(data, Map.class);
            // TODO 是否应将 PersistentUserSessionData 设为 public？
            boolean rememberMe = Boolean.parseBoolean(String.valueOf(values.get("rememberMe")));
            return new UserSessionIdAndRememberMe(new UserSessionAndUser(sessionId, userId), rememberMe);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
