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

package org.keycloak.sessions;

import java.util.Map;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;

/**
 * 根认证会话：通常对应一个浏览器会话，可含多个标签页；每标签页对应一个 {@link AuthenticationSessionModel}。
 *
 * Represents usually one browser session with potentially many browser tabs. Every browser tab is represented by
 * {@link AuthenticationSessionModel} of different client.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface RootAuthenticationSessionModel {

    /**
     * 返回根认证会话 ID。
     * Returns id of the root authentication session.
     * @return {@code String}
     */
    String getId();

    /**
     * 返回关联 Realm。
     * Returns realm associated to the root authentication session.
     * @return {@code RealmModel}
     */
    RealmModel getRealm();

    /**
     * 返回创建或最后更新时间戳。
     * Returns timestamp when the root authentication session was created or updated.
     * @return {@code int}
     */
    int getTimestamp();

    /**
     * 设置时间戳并更新过期时间。
     * Sets a timestamp when the root authentication session was created or updated.
     * It also updates the expiration time for the root authentication session entity.
     * @param timestamp {@code int}
     */
    void setTimestamp(int timestamp);

    /**
     * 返回子认证会话映射（key 为 tabId）。
     * Returns authentication sessions for the root authentication session.
     * Key is tabId, Value is AuthenticationSessionModel.
     * @return {@code Map<String, AuthenticationSessionModel>} authentication sessions or empty map if no
     * authentication sessions are present. Never return null.
     */
    Map<String, AuthenticationSessionModel> getAuthenticationSessions();

    /**
     * 按客户端与 tabId 获取子认证会话。
     * Returns an authentication session for the particular client and tab or null if it doesn't yet exists.
     * @param client {@code ClientModel} If {@code null} is provided the method will return {@code null}.
     * @param tabId {@code String} If {@code null} is provided the method will return {@code null}.
     * @return {@code AuthenticationSessionModel} or {@code null} in no authentication session is found.
     */
    AuthenticationSessionModel getAuthenticationSession(ClientModel client, String tabId);

    /**
     * 为指定客户端创建新的子认证会话。
     * Create a new authentication session and returns it.
     * @param client {@code ClientModel} Can't be {@code null}.
     * @return {@code AuthenticationSessionModel} non-null fresh authentication session. Never returns {@code null}.
     */
    AuthenticationSessionModel createAuthenticationSession(ClientModel client);

    /**
     * 按 tabId 移除子会话；若无剩余子会话则删除根会话。
     * Removes the authentication session specified by tab id from the root authentication session.
     * If there's no child authentication session left in the root authentication session, it's removed as well.
     * @param tabId {@code String} Can't be {@code null}.
     */
    void removeAuthenticationSessionByTabId(String tabId);

    /**
     * 完全重启会话状态（保留 ID），并绑定新 Realm。
     * Will completely restart whole state of authentication session. It will just keep same ID. It will setup it with provided realm.
     * @param realm {@code RealmModel} Associated realm to the root authentication session.
     */
    void restartSession(RealmModel realm);

}
