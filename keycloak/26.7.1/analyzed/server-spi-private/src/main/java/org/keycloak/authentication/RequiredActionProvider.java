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

package org.keycloak.authentication;

import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.provider.Provider;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.RequiredActionHelper;



/**
 * 必需操作 Provider：用户登录前须完成的一次性动作（如改密、配置 OTP）。
 *
 * RequiredAction provider.  Required actions are one-time actions that a user must perform before they are logged in.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RequiredActionProvider extends Provider {

    /**
     * 是否支持应用发起的必需操作（AIA）。
     *
     * Determines what type of support is provided for application-initiated
     * actions.
     * 
     * @return InititatedActionsSupport
     */
    default InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.NOT_SUPPORTED;
    }

    /**
     * AIA 被用户取消时的回调。
     *
     * Callback to let the action know that an application-initiated action
     * was canceled.
     *
     * @param session The Keycloak session.
     * @param authSession The authentication session.
     *
     */
    default void initiatedActionCanceled(KeycloakSession session, AuthenticationSessionModel authSession) {
        return;
    }
    
    /**
     * 每次用户认证时调用，判断是否应触发本必需操作并在 UserModel 上设置。
     * 例如 UpdatePassword 检查密码是否过期。
     *
     * Called every time a user authenticates.  This checks to see if this required action should be triggered.
     * The implementation of this method is responsible for setting the required action on the UserModel.
     *
     * For example, the UpdatePassword required actions checks the password policies to see if the password has expired.
     *
     * @param context
     */
    void evaluateTriggers(RequiredActionContext context);

    /**
     * 用户有待办必需操作时，首次调用以渲染浏览器挑战页。
     *
     * If the user has a required action set, this method will be the initial call to obtain what to display to the
     * user's browser.  Return null if no action should be done.
     *
     * @param context
     * @return
     */
    void requiredActionChallenge(RequiredActionContext context);

    /**
     * 处理用户提交的必需操作表单。
     *
     * Called when a required action has form input you want to process.
     *
     * @param context
     */
    void processAction(RequiredActionContext context);


    /**
     * @deprecated 请使用 {@link #getMaxAuthAge(KeycloakSession)}；本方法已无效果。
     *
     * @deprecated in favor of {@link #getMaxAuthAge(KeycloakSession)} to support individual configuration of max auth age for all required actions. This method has no effect anymore.
     *
     * AIA 场景下登录后允许执行的最长间隔（秒）；0 表示始终要求重新认证。
     * 默认读取必需操作配置的 max_auth_age，否则使用 KeycloakConstants 默认值。
     *
     * Defines the max time after a user login, after which re-authentication is requested for an AIA. 0 means that re-authentication is always requested.
     * On default uses configured max_auth_age value from the required action config. If not configured, it uses the default max_auth_age value from the KeycloakConstants class.
     */
    @Deprecated(since = "26.3.0", forRemoval = true)
    default int getMaxAuthAge() {
        return Constants.KC_ACTION_MAX_AGE;
    }

    /**
     * Defines the max time after a user login, after which re-authentication is requested for an AIA. 0 means that re-authentication is always requested.
     * On default uses configured max_auth_age value from the required action config. If not configured, it uses the default max_auth_age value from the KeycloakConstants class.
     */
    default int getMaxAuthAge(KeycloakSession session) {
        if (session == null) {
            // session 为 null 时兼容旧实现，回退默认 maxAuthAge
            return Constants.KC_ACTION_MAX_AGE;
        }

        KeycloakContext keycloakContext = session.getContext();
        RealmModel realm = keycloakContext.getRealm();
        int maxAge;

        // 尝试读取必需操作配置
        AuthenticationSessionModel authSession = keycloakContext.getAuthenticationSession();
        if (authSession != null) {

            // 解析当前必需操作的 alias
            String providerId = authSession.getClientNote(Constants.KC_ACTION);
            RequiredActionProviderModel requiredAction = RequiredActionHelper.getRequiredActionByProviderId(realm, providerId);

            if (requiredAction != null) {
                RequiredActionConfigModel configModel = realm.getRequiredActionConfigByAlias(requiredAction.getAlias());
                if (configModel != null && configModel.containsConfigKey(Constants.MAX_AUTH_AGE_KEY)) {
                    maxAge = RequiredActionFactory.parseMaxAuthAge(configModel);
                    if (maxAge >= 0) {
                        return maxAge;
                    }
                }
            }
        }

        // 回退到默认值
        return Constants.KC_ACTION_MAX_AGE;
    }

}
