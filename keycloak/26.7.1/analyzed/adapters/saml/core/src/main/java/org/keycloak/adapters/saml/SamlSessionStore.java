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

package org.keycloak.adapters.saml;

import java.util.List;

import org.keycloak.adapters.spi.AdapterSessionStore;

/**
 * SAML 会话存储接口，扩展通用适配器会话存储并跟踪登录/登出状态。
 *
 * <p>管理 {@link SamlSession} 的读写、当前 SAML 动作（登录中/登出中）及错误状态键。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface SamlSessionStore extends AdapterSessionStore {
    /** 会话中当前 SAML 动作类型的键名。 */
    public static final String CURRENT_ACTION = "SAML_CURRENT_ACTION";
    /** 登录失败时 SAML 状态码的会话键。 */
    public static final String SAML_LOGIN_ERROR_STATUS = "SAML_LOGIN_ERROR_STATUS";
    /** 登出失败时 SAML 状态码的会话键。 */
    public static final String SAML_LOGOUT_ERROR_STATUS = "SAML_LOGOUT_ERROR_STATUS";

    /** SAML 流程当前动作枚举。 */
    enum CurrentAction {
        /** 无进行中的 SAML 动作。 */
        NONE,
        /** 正在执行登录流程。 */
        LOGGING_IN,
        /** 正在执行登出流程。 */
        LOGGING_OUT
    }
    /** 设置当前 SAML 动作状态。 */
    void setCurrentAction(CurrentAction action);
    /** @return 是否处于登录流程中 */
    boolean isLoggingIn();
    /** @return 是否处于登出流程中 */
    boolean isLoggingOut();

    /** @return 当前是否已登录 */
    boolean isLoggedIn();
    /** @return 已保存的 SAML 账户会话 */
    SamlSession getAccount();
    /** 持久化 SAML 账户至会话。 */
    void saveAccount(SamlSession account);
    /** @return 认证完成后的重定向 URI */
    String getRedirectUri();
    /** 清除当前账户并登出。 */
    void logoutAccount();
    /** 按主体名登出匹配会话。 */
    void logoutByPrincipal(String principal);
    /** 按 SSO 会话 ID 列表批量登出。 */
    void logoutBySsoId(List<String> ssoIds);

}
