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

/**
 * 用户会话 ID 与用户 ID 的轻量投影，用于过期清理等 NamedQuery 结果映射。
 *
 * @param userSessionId 用户会话 ID
 * @param userId        用户 ID
 */
record UserSessionAndUser(String userSessionId, String userId) {

    /** 从 Object[] 查询投影构造实例（长度须为 2，元素非 null）。 */
    static UserSessionAndUser fromQueryProjection(Object[] projection) {
        assert projection.length == 2;
        assert projection[0] != null;
        assert projection[1] != null;
        return new UserSessionAndUser(String.valueOf(projection[0]), String.valueOf(projection[1]));
    }

}
