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

package org.keycloak.models.sessions.infinispan.changes.remote.updater.user;

import java.util.Map;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.UserSessionProvider;

/**
 * 通过 {@link Map} 接口只读暴露 {@link UserSessionModel} 下的 {@link AuthenticatedClientSessionModel}，
 * 键为 Client ID。
 * <p>
 * 直接通过 {@link Map} 修改会抛出 {@link UnsupportedOperationException}。新增映射请使用
 * {@link UserSessionProvider#createClientSession(RealmModel, ClientModel, UserSessionModel)} 等 API；
 * 移除映射请调用 {@link AuthenticatedClientSessionModel#detachFromUserSession()}。
 */
public interface AuthenticatedClientSessionMapping extends Map<String, AuthenticatedClientSessionModel> {

    /**
     * 通知关联的 {@link UserSessionModel} 已重启。
     * <p>
     * 所有 {@link AuthenticatedClientSessionModel} 须从用户会话中分离。
     */
    void onUserSessionRestart();

}
