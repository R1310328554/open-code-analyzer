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

package org.keycloak.sessions;

import java.util.Map;
import java.util.Set;

import org.keycloak.models.UserModel;

/**
 * 认证会话模型：表示单次登录流程的状态；同一浏览器各标签页各有一个实例，整浏览器由 {@link RootAuthenticationSessionModel} 表示。
 *
 * Represents the state of the authentication. If the login is requested from different tabs of same browser, every browser
 * tab has it's own state of the authentication. So there is separate AuthenticationSessionModel for every tab. Whole browser
 * is represented by {@link RootAuthenticationSessionModel}
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface AuthenticationSessionModel extends CommonClientSessionModel {

    /**
     * 返回本子会话（通常为浏览器标签页）ID；定位需根会话 ID、客户端 UUID 与本 tabId。
     * @return ID of this subsession (in other words, usually browser tab). For lookup the AuthenticationSessionModel, you need:
     * ID of rootSession (parent), client UUID and tabId. For lookup the ID of the parent, use {@link #getParentSession().getId()}
     */
    String getTabId();

    /**
     * 返回作为父级的根认证会话。
     * Returns the root authentication session that is parent of this authentication session.
     * @return {@code RootAuthenticationSessionModel}
     */
    RootAuthenticationSessionModel getParentSession();

    /**
     * 返回各认证器执行状态映射。
     * Returns execution status of the authentication session.
     * @return {@code Map<String, ExecutionStatus>} Never returns {@code null}.
     */
    Map<String, ExecutionStatus> getExecutionStatus();

    /**
     * 设置指定认证器的执行状态。
     * Sets execution status of the authentication session.
     * @param authenticator {@code String} Can't be {@code null}.
     * @param status {@code ExecutionStatus} Can't be {@code null}.
     */
    void setExecutionStatus(String authenticator, ExecutionStatus status);

    /**
     * 清空全部认证器执行状态。
     * Clears execution status of the authentication session.
     */
    void clearExecutionStatus();

    /**
     * 返回已关联的已认证用户。
     * Returns authenticated user that is associated to the authentication session.
     * @return {@code UserModel} or null if there's no authenticated user.
     */
    UserModel getAuthenticatedUser();

    /**
     * 设置已认证用户。
     * Sets authenticated user that is associated to the authentication session.
     * @param user {@code UserModel} If {@code null} then {@code null} will be set to the authenticated user.
     */
    void setAuthenticatedUser(UserModel user);

    /**
     * 返回附加于此会话的必需操作别名集合。
     * Returns required actions (aliases) that are attached to this client session.
     * @return {@code Set<String>} Never returns {@code null}.
     */
    Set<String> getRequiredActions();

    /**
     * 添加必需操作（字符串别名）。
     * Adds a required action to the authentication session.
     * @param action {@code String} Can't be {@code null}.
     */
    void addRequiredAction(String action);

    /**
     * 移除必需操作（字符串别名）。
     * Removes a required action from the authentication session.
     * @param action {@code String} Can't be {@code null}.
     */
    void removeRequiredAction(String action);

    /**
     * 添加必需操作（枚举类型）。
     * Adds a required action to the authentication session.
     * @param action {@code UserModel.RequiredAction} Can't be {@code null}.
     */
    void addRequiredAction(UserModel.RequiredAction action);

    /**
     * 移除必需操作（枚举类型）。
     * Removes a required action from the authentication session.
     * @param action {@code UserModel.RequiredAction} Can't be {@code null}.
     */
    void removeRequiredAction(UserModel.RequiredAction action);

    /**
     * 设置用户会话备注；附加到 UserSession 时使用。
     * Sets the given user session note to the given value. User session notes are notes
     * you want be applied to the UserSessionModel when the client session is attached to it.
     * @param name {@code String} If {@code null} is provided the method won't have an effect.
     * @param value {@code String} If {@code null} is provided the method won't have an effect.
     */
    void setUserSessionNote(String name, String value);

    /**
     * 获取全部用户会话备注映射。
     * Retrieves value of given user session note. User session notes are notes
     * you want be applied to the UserSessionModel when the client session is attached to it.
     * @return {@code Map<String, String>} never returns {@code null}
     */
    Map<String, String> getUserSessionNotes();

    /**
     * 清空全部用户会话备注。
     * Clears all user session notes. User session notes are notes
     * you want be applied to the UserSessionModel when the client session is attached to it.
     */
    void clearUserSessionNotes();

    /**
     * 读取认证备注（认证器/流程使用，重启会话时清除）。
     * Retrieves value of the given authentication note to the given value. Authentication notes are notes
     * used typically by authenticators and authentication flows. They are cleared when
     * authentication session is restarted.
     * @param name {@code String} If {@code null} is provided then the method will return {@code null}.
     * @return {@code String} or {@code null} if no authentication note is set.
     */
    String getAuthNote(String name);

    /**
     * 设置认证备注。
     * Sets the given authentication note to the given value. Authentication notes are notes
     * used typically by authenticators and authentication flows. They are cleared when
     * authentication session is restarted.
     * @param name {@code String} If {@code null} is provided the method won't have an effect.
     * @param value {@code String} If {@code null} is provided the method won't have an effect.
     */
    void setAuthNote(String name, String value);

    /**
     * 移除指定认证备注。
     * Removes the given authentication note. Authentication notes are notes
     * used typically by authenticators and authentication flows. They are cleared when
     * authentication session is restarted.
     * @param name {@code String} If {@code null} is provided the method won't have an effect.
     */
    void removeAuthNote(String name);

    /**
     * 清空全部认证备注。
     * Clears all authentication note. Authentication notes are notes
     * used typically by authenticators and authentication flows. They are cleared when
     * authentication session is restarted.
     */
    void clearAuthNotes();

    /**
     * 读取客户端协议备注（重启会话时不清理）。
     * Retrieves value of the given client note to the given value. Client notes are notes
     * specific to client protocol. They are NOT cleared when authentication session is restarted.
     * @param name {@code String} If {@code null} if provided then the method will return {@code null}.
     * @return {@code String} or {@code null} if no client's note is set.
     */
    String getClientNote(String name);

    /**
     * 设置客户端协议备注。
     * Sets the given client note to the given value. Client notes are notes
     * specific to client protocol. They are NOT cleared when authentication session is restarted.
     * @param name {@code String} If {@code null} is provided the method won't have an effect.
     * @param value {@code String} If {@code null} is provided the method won't have an effect.
     */
    void setClientNote(String name, String value);

    /**
     * 移除客户端协议备注。
     * Removes the given client note. Client notes are notes
     * specific to client protocol. They are NOT cleared when authentication session is restarted.
     * @param name {@code String} If {@code null} is provided the method won't have an effect.
     */
    void removeClientNote(String name);

    /**
     * 获取全部客户端备注映射。
     * Retrieves the (name, value) map of client notes. Client notes are notes
     * specific to client protocol. They are NOT cleared when authentication session is restarted.
     * @return {@code Map<String, String>} never returns {@code null}.
     */
    Map<String, String> getClientNotes();

    /**
     * 清空全部客户端备注。
     * Clears all client notes. Client notes are notes
     * specific to client protocol. They are NOT cleared when authentication session is restarted.
     */
    void clearClientNotes();

    /**
     * 获取会话中的客户端 Scope ID 集合。
     * Gets client scope IDs from the authentication session.
     * @return {@code Set<String>} never returns {@code null}.
     */
    Set<String> getClientScopes();

    /**
     * 设置会话中的客户端 Scope ID 集合。
     * Sets client scope IDs to the authentication session.
     * @param clientScopes {@code Set<String>} Can't be {@code null}.
     */
    void setClientScopes(Set<String> clientScopes);
}
