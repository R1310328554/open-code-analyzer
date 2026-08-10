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

import java.util.Objects;

import org.keycloak.models.session.PersistentAuthenticatedClientSessionAdapter;
import org.keycloak.storage.StorageId;

/**
 * JPA 会话持久化工具类：offline 标志转换、外部客户端 ID 解析等。
 */
public final class JpaSessionUtil {

    private JpaSessionUtil() {}

    /** 将 offline 布尔值转为 DB 存储字符串（"1"/"0"）。 */
    public static String offlineToString(boolean offline) {
        return offline ? "1" : "0";
    }

    /** 从 DB 字符串解析 offline 标志。 */
    public static boolean offlineFromString(String offlineStr) {
        return "1".equals(offlineStr);
    }

    /** 判断 client session 是否引用外部存储的客户端。 */
    public static boolean isExternalClient(PersistentClientSessionEntity entity) {
        return !entity.getExternalClientId().equals(PersistentClientSessionEntity.LOCAL);
    }

    /** 构造外部客户端的完整 StorageId。 */
    public static String getExternalClientId(PersistentClientSessionEntity entity) {
        return new StorageId(entity.getClientStorageProvider(), entity.getExternalClientId()).getId();
    }

    /** 返回 client session 对应的客户端 UUID（本地或外部）。 */
    public static String getClientId(PersistentClientSessionEntity entity) {
        return isExternalClient(entity) ? getExternalClientId(entity) : entity.getClientId();
    }

    /**
     * 从 {@link UserSessionIdAndClientSessionId} 记录中提取客户端 ID。
     * <p>
     * 内部客户端 ID 存于 clientSessionId；外部客户端通过 clientStorageProvider + externalClientId 组合。
     *
     * @param sessions 包含客户端信息的会话 ID 记录
     * @return 客户端 UUID；无 client session 时返回 null
     */
    public static String getClientId(UserSessionIdAndClientSessionId sessions) {
        return Objects.equals(sessions.clientSessionId(), PersistentClientSessionEntity.EXTERNAL) ?
                new StorageId(sessions.clientStorageProvider(), sessions.externalClientId()).getId() :
                sessions.clientSessionId();
    }

    /** 判断 client session 适配器是否已关联有效客户端。 */
    public static boolean hasClient(PersistentAuthenticatedClientSessionAdapter clientSession) {
        return clientSession.getClient() != null;
    }
}
