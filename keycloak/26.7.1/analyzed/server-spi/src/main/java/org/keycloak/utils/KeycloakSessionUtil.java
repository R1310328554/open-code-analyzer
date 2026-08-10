/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.utils;

import org.keycloak.common.util.Resteasy;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

/**
 * Keycloak 会话工具类：通过 Resteasy 线程上下文获取/设置当前 {@link KeycloakSession}，
 * 以及从会话中提取 realm 名称。
 */
public class KeycloakSessionUtil {

    /** 会话中未找到 realm 时的占位返回值。 */
    private static final String NO_REALM = "no_realm_found_in_session";

    /** 工具类，禁止实例化。 */
    private KeycloakSessionUtil() {

    }

    /**
     * 获取当前线程关联的 {@link KeycloakSession}。
     * Get the {@link KeycloakSession} currently associated with the thread.
     *
     * @return the current session
     */
    public static KeycloakSession getKeycloakSession() {
        return Resteasy.getContextData(KeycloakSession.class);
    }

    /**
     * 将 {@link KeycloakSession} 绑定到当前线程。
     * <br>警告：不应直接调用，由 Keycloak 框架管理。
     * Associate the {@link KeycloakSession} with the current thread.
     * <br>Warning: should not be called directly. Keycloak will manage this.
     *
     * @param session
     * @return the existing {@link KeycloakSession} or null
     */
    public static KeycloakSession setKeycloakSession(KeycloakSession session) {
        return Resteasy.pushContext(KeycloakSession.class, session);
    }

    /** 从会话上下文中提取 realm 名称；无法获取时返回 {@link #NO_REALM}。
     * @param session Keycloak 会话
     * @return realm 名称或占位符 */
    public static String getRealmNameFromContext(KeycloakSession session) {
        if(session == null) {
            return NO_REALM;
        }

        KeycloakContext context = session.getContext();
        if(context == null) {
            return NO_REALM;
        }

        RealmModel realm = context.getRealm();
        if (realm == null) {
            return NO_REALM;
        }

        if(realm.getName() != null) {
            return realm.getName();
        } else {
            return NO_REALM;
        }
    }

}
