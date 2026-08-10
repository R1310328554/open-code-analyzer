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

package org.keycloak.models;


import java.util.Map;

import org.keycloak.common.util.Time;
import org.keycloak.sessions.CommonClientSessionModel;

/**
 * 已认证客户端会话模型：关联用户会话，管理刷新令牌、note 及会话生命周期。
 * <p>扩展 {@link CommonClientSessionModel}，支持按 reuseId 追踪多个刷新令牌。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface AuthenticatedClientSessionModel extends CommonClientSessionModel {

    /** note 键：客户端会话启动时间戳。 */
    final String STARTED_AT_NOTE = "startedAt";
    /** note 键：关联用户会话的启动时间戳。 */
    final String USER_SESSION_STARTED_AT_NOTE = "userSessionStartedAt";
    /** note 键：用户会话是否启用“记住我”。 */
    final String USER_SESSION_REMEMBER_ME_NOTE = "userSessionRememberMe";
    /** note 前缀：按 reuseId 存储刷新令牌 ID。 */
    final String REFRESH_TOKEN_PREFIX = "refreshTokenPrefix";
    /** note 前缀：刷新令牌使用次数。 */
    final String REFRESH_TOKEN_USE_PREFIX = "refreshTokenUsePrefix";
    /** note 前缀：刷新令牌上次刷新时间戳。 */
    final String REFRESH_TOKEN_LAST_REFRESH_PREFIX = "refreshTokenLastRefreshPrefix";

    /** @return 客户端会话唯一 ID */
    String getId();

    /** @return 客户端会话启动时间（秒）；离线迁移场景可能回退到用户会话启动时间 */
    default int getStarted() {
        String started = getNote(STARTED_AT_NOTE);
        if (started == null) {
            // 旧版迁移的离线会话可能缺少 startedAt note，回退到用户会话启动时间或 0
            // Note can be null for offline sessions migrated from old version where "startedAt" note was not yet available
            // Fallback to user session started for offline or 0
            return getUserSession().isOffline() ? getUserSessionStarted() : 0;
        }
        return Integer.parseInt(started);
    }

    /** @return 关联用户会话的启动时间戳 */
    default int getUserSessionStarted() {
        String started = getNote(USER_SESSION_STARTED_AT_NOTE);
        return started == null ? getUserSession().getStarted() : Integer.parseInt(started);
    }

    /** @return 用户会话是否启用了“记住我” */
    default boolean isUserSessionRememberMe() {
        return Boolean.parseBoolean(getNote(USER_SESSION_REMEMBER_ME_NOTE));
    }

    /**
     * 获取客户端会话时间戳。
     * @deprecated for removed, without replacement.
     */
    @Deprecated(since = "26.4", forRemoval = true)
    // This data may not be required as we can check the expiry time in the refresh token. 
    // If so, this method can be removed; otherwise we keep the method and remove the deprecation notice.
    int getTimestamp();

    /**
     * 设置客户端会话时间戳；若新值不大于当前值则忽略。
     * Set the timestamp for the client session.
     * If the timestamp is smaller or equal than the current timestamp, the operation is ignored.
     * @deprecated for removed, without replacement.
     */
    @Deprecated(since = "26.4", forRemoval = true)
    void setTimestamp(int timestamp);

    /**
     * 将客户端会话从用户会话中分离。
     * Detaches the client session from its user session.
     */
    void detachFromUserSession();
    /** @return 关联的用户会话 */
    UserSessionModel getUserSession();

    /**
     * 获取当前刷新令牌（已弃用）。
     * @deprecated use {@link #getRefreshToken(String)}
     */
    @Deprecated
    default String getCurrentRefreshToken() {
        return null;
    }

    /**
     * 设置当前刷新令牌（已弃用）。
     *  @deprecated use {@link #setRefreshToken(String, String)}}
     */
    @Deprecated
    default void setCurrentRefreshToken(String currentRefreshToken) {
    }

    /**
     * 获取当前刷新令牌使用次数（已弃用）。
     * @deprecated use {@link #getRefreshTokenUseCount(String)}
     */
    @Deprecated
    default int getCurrentRefreshTokenUseCount() {
        return 0;
    }

    /**
     * 设置当前刷新令牌使用次数（已弃用）。
     * @deprecated  use {@link #setRefreshTokenUseCount(String, int)}
     */
    @Deprecated
    default void setCurrentRefreshTokenUseCount(int currentRefreshTokenUseCount) {
    }

    /** @param reuseId 刷新令牌重用标识
     * @return 刷新令牌 ID */
    default String getRefreshToken(String reuseId) {
        return getNote(REFRESH_TOKEN_PREFIX + reuseId);
    }
    /** @param reuseId 重用标识
     * @param refreshTokenId 刷新令牌 ID */
    default void setRefreshToken(String reuseId, String refreshTokenId) {
        setNote(REFRESH_TOKEN_PREFIX + reuseId, refreshTokenId);
    }
    /** @param reuseId 重用标识
     * @return 刷新令牌已使用次数 */
    default int getRefreshTokenUseCount(String reuseId) {
        String count = getNote(REFRESH_TOKEN_USE_PREFIX + reuseId);
        return count == null ? 0 : Integer.parseInt(count);
    }
    /** @param reuseId 重用标识
     * @param refreshTokenUseCount 使用次数 */
    default void setRefreshTokenUseCount(String reuseId, int refreshTokenUseCount) {
        setNote(REFRESH_TOKEN_USE_PREFIX + reuseId, String.valueOf(refreshTokenUseCount));
    }
    /** @param reuseId 重用标识
     * @return 上次刷新时间戳 */
    default int getRefreshTokenLastRefresh(String reuseId) {
        String timestamp = getNote(REFRESH_TOKEN_LAST_REFRESH_PREFIX + reuseId);
        return timestamp == null ? 0 : Integer.parseInt(timestamp);
    }
    /** @param reuseId 重用标识
     * @param refreshTokenLastRefresh 上次刷新时间戳 */
    default void setRefreshTokenLastRefresh(String reuseId, int refreshTokenLastRefresh) {
        setNote(REFRESH_TOKEN_LAST_REFRESH_PREFIX + reuseId, String.valueOf(refreshTokenLastRefresh));
    }

    /** @param name note 名称
     * @return note 值 */
    String getNote(String name);
    /** @param name note 名称
     * @param value note 值 */
    void setNote(String name, String value);
    /** @param name 要移除的 note 名称 */
    void removeNote(String name);
    /** @return 所有 note 的只读映射 */
    Map<String, String> getNotes();

    /** 重置客户端会话：清除 action/redirectUri 及除用户会话相关 note 外的所有 note。 */
    default void restartClientSession() {
        setAction(null);
        setRedirectUri(null);
        setTimestamp(Time.currentTime());
        for (String note : getNotes().keySet()) {
            if (!AuthenticatedClientSessionModel.USER_SESSION_STARTED_AT_NOTE.equals(note)
                    && !AuthenticatedClientSessionModel.STARTED_AT_NOTE.equals(note)
                    && !AuthenticatedClientSessionModel.USER_SESSION_REMEMBER_ME_NOTE.equals(note)) {
                removeNote(note);
            }
        }
        setNote(AuthenticatedClientSessionModel.STARTED_AT_NOTE, String.valueOf(getTimestamp()));
    }
}
