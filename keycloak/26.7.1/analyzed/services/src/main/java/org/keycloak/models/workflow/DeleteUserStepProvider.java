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

package org.keycloak.models.workflow;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.UserCache;
import org.keycloak.storage.UserStoragePrivateUtil;
import org.keycloak.storage.UserStorageUtil;

import org.jboss.logging.Logger;

/**
 * 工作流步骤：删除工作流上下文中的目标用户。
 * <p>联合用户默认仅删本地副本；配置 {@link #PROPAGATE_TO_SP} 为 true 时传播至外部用户存储。</p>
 */
public class DeleteUserStepProvider implements WorkflowStepProvider {

    /** 是否将删除操作传播至外部用户存储提供方的配置键。 */
    public static final String PROPAGATE_TO_SP = "propagate-to-provider";

    private final KeycloakSession session;
    private final ComponentModel stepModel;
    private final Logger log = Logger.getLogger(DeleteUserStepProvider.class);

    /** @param session Keycloak 会话 @param model 工作流步骤组件配置 */
    public DeleteUserStepProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.stepModel = model;
    }

    @Override
    public void close() {
    }

    /** 按联合/本地策略删除用户并在启用缓存时驱逐 {@link UserCache}。 */
    @Override
    public void run(WorkflowExecutionContext context) {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, context.getResourceId());

        if (user == null) {
            return;
        }

        UserManager userManager = new UserManager(session);
        if (!user.isFederated() || stepModel.get(PROPAGATE_TO_SP, false)) {
          log.debugv("Deleting user {0} ({1})", user.getUsername(), user.getId());
          userManager.removeUser(realm, user);
          return;
        }

        // 联合用户仅删除本地存储副本，不传播至外部 IdP
        userManager.removeUser(realm, user, UserStoragePrivateUtil.userLocalStorage(session));
        log.debugv("Deleting federated user {0} ({1}) from local storage only", user.getUsername(), user.getId());
        UserCache userCache = UserStorageUtil.userCache(session);
        // 启用用户缓存时从缓存中驱逐已删用户
        if (userCache != null) {
            userCache.evict(realm, user);
        }
    }

    /** @return 账户删除通知消息模板键 */
    @Override
    public String getNotificationMessage() {
        return "accountDeleteNotificationBody";
    }

    /** @return 账户删除通知主题模板键 */
    @Override
    public String getNotificationSubject() {
        return "accountDeleteNotificationSubject";
    }
}
