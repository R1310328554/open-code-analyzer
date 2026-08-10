/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.userprofile;

import java.util.Set;
import java.util.function.Predicate;

import org.keycloak.models.UserModel;
import org.keycloak.utils.StringUtil;

import static org.keycloak.userprofile.UserProfileConstants.ROLE_ADMIN;
import static org.keycloak.userprofile.UserProfileConstants.ROLE_USER;

/**
 * 用户资料管理上下文枚举：表示 Keycloak 中管理用户资料的不同场景（注册、账户、Admin API 等）。
 * <p>上下文决定管理用户资料时应遵守的条件，未来可扩展元数据或自定义上下文。</p>
 *
 * <p>This interface represents the different contexts from where user profiles are managed. The core contexts are already
 * available here representing the different areas in Keycloak where user profiles are managed.
 *
 * <p>The context is crucial to drive the conditions that should be respected when managing user profiles. It might be possible
 * to include in the future metadata about contexts. As well as support custom contexts.
 *
 * @author <a href="mailto:markus.till@bosch.io">Markus Till</a>
 */
public enum UserProfileContext {

    /**
     * 认证流程中用户自行更新资料（如更新个人资料流程）。
     * In this context, a user profile is managed by themselves during an authentication flow such as when updating the user profile.
     */
    UPDATE_PROFILE(false, true, true),

    /**
     * 通过管理接口（如 Admin API）管理用户资料。
     * In this context, a user profile is managed through the management interface such as the Admin API.
     */
    USER_API(true, false, false),

    /**
     * 用户通过账户控制台自行管理资料。
     * In this context, a user profile is managed by themselves through the account console.
     */
    ACCOUNT(false, true, true),

    /**
     * 用户通过身份代理（IdP broker）认证时自行管理资料。
     * In this context, a user profile is managed by themselves when authenticating through a broker.
     */
    IDP_REVIEW(false, true, false),

    /**
     * 用户注册到 realm 时自行填写资料。
     * In this context, a user profile is managed by themselves when registering to a realm.
     */
    REGISTRATION(false, true, false),

    /**
     * 用户通过应用发起的操作更新邮箱；此上下文仅支持 {@link UserModel#EMAIL} 属性。
     * In this context, a user profile is managed by themselves when updating their email through an application initiated action.
     * In this context, only the {@link UserModel#EMAIL} attribute is supported.
     */
    UPDATE_EMAIL(false, true, true, Set.of(UserModel.EMAIL)::contains),

    /**
     * In this context, a user profile is managed through the management interface such as the Admin API.
     */
    /** 通过 SCIM 协议管理用户资料。 */
    SCIM(false, false, false);

    /** 更新邮箱时是否重置 emailVerified 标志。 */
    private final boolean resetEmailVerified;
    /** 判断属性是否在此上下文中受支持。 */
    private final Predicate<String> attributeSelector;
    /** 是否为管理员上下文。 */
    private final boolean adminContext;
    /** 是否可属于认证流程上下文。 */
    private final boolean authFlowContext;
    
    /** 完整构造：指定管理员/认证流程标志、邮箱验证重置及属性选择器。 */
    UserProfileContext(boolean adminContext, boolean authFlowContext, boolean resetEmailVerified, Predicate<String> attributeSelector){
        this.adminContext = adminContext;
        this.authFlowContext = authFlowContext;
        this.resetEmailVerified = resetEmailVerified;
        this.attributeSelector = attributeSelector;
    }

    /** 默认属性选择器为 {@link StringUtil#isNotBlank}。 */
    UserProfileContext(boolean adminContext, boolean authFlowContext, boolean resetEmailVerified){
        this(adminContext, authFlowContext, resetEmailVerified, StringUtil::isNotBlank);
    }

    /**
     * 若为 {@code true} 表示适用于管理员；{@code false} 表示适用于普通用户。
     * @return true means that this context is applicable to administrators. False means that this context is applicable to regular users
     */
    public boolean isAdminContext() {
        return adminContext;
    }

    /**
     * 若上下文可属于认证流程则返回 {@code true}。
     * @return true if context CAN BE part of the authentication flow
     */
    public boolean canBeAuthFlowContext() {
        return authFlowContext;
    }

    /**
     * 更新邮箱时是否须将 {@code UserModel.emailVerified} 重置为 {@code false}。
     * @return true means that UserModel.emailVerified flag must be reset to false in this context when email address is updated
     */
    public boolean isResetEmailVerified() {
        return resetEmailVerified;
    }

    /**
     * 检查角色配置是否包含本上下文对应的角色。
     * Check if roles configuration contains role for this context.
     *
     * @param roles to be inspected
     * @return true if roles list contains role representing checked context
     */
    public boolean isRoleForContext(Set<String> roles) {
        if (roles == null)
            return false;
        return roles.contains(getContextRole());
    }

    private String getContextRole() {
        return isAdminContext() ? ROLE_ADMIN : ROLE_USER;
    }

    /** @param name 属性名
     * @return 该属性是否在此上下文中受支持 */
    public boolean isAttributeSupported(String name) {
        return attributeSelector.test(name);
    }
}
