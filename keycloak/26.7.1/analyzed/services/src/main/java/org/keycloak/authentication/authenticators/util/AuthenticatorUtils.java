/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authentication.authenticators.util;

import java.io.IOException;
import java.util.Map;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.common.util.Time;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.services.managers.BruteForceProtector;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jboss.logging.Logger;

import static org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator.USER_SET_BEFORE_USERNAME_PASSWORD_AUTH;

/**
 * 认证器通用工具类：暴力破解错误映射、虚拟密码哈希、已完成执行记录与 rememberMe 处理等。
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public final class AuthenticatorUtils {
    private static final Logger logger = Logger.getLogger(AuthenticatorUtils.class);

    /** 根据暴力破解保护状态返回对应事件错误码。 */
    public static String getDisabledByBruteForceEventError(BruteForceProtector protector, KeycloakSession session, RealmModel realm, UserModel user) {
        if (realm.isBruteForceProtected()) {
            if (protector.isPermanentlyLockedOut(session, realm, user)) {
                return Errors.USER_DISABLED;
            }
            else if (protector.isTemporarilyDisabled(session, realm, user)) {
                return Errors.USER_TEMPORARILY_DISABLED;
            }
            return null;
        }
        return null;
    }

    /** 从认证流程上下文获取暴力破解禁用错误码。 */
    public static String getDisabledByBruteForceEventError(AuthenticationFlowContext authnFlowContext, UserModel authenticatedUser) {
        return AuthenticatorUtils.getDisabledByBruteForceEventError(authnFlowContext.getProtector(), authnFlowContext.getSession(), authnFlowContext.getRealm(), authenticatedUser);
    }

    /**
     * 模拟“虚拟”密码哈希，使不存在用户名的请求与错误密码请求耗时相近，降低用户枚举风险。
     *
     * to simulate the password hashing overhead and takes same time like the request with existing username, but incorrect password.
     *
     * @param context
     */
    /** 使用 realm 密码策略执行一次虚拟密码哈希。 */
    public static void dummyHash(AuthenticationFlowContext context) {
        PasswordPolicy passwordPolicy = context.getRealm().getPasswordPolicy();
        PasswordHashProvider provider;
        if (passwordPolicy != null && passwordPolicy.getHashAlgorithm() != null) {
            provider = context.getSession().getProvider(PasswordHashProvider.class, passwordPolicy.getHashAlgorithm());
        } else {
            provider = context.getSession().getProvider(PasswordHashProvider.class);
        }
        int iterations = passwordPolicy != null ? passwordPolicy.getHashIterations() : -1;
        provider.encodedCredential("SlightlyLongerDummyPassword", iterations);
    }

    /**
     * 解析用户会话 note 中已完成的认证器执行记录。
     * @param note 序列化的 note 值
     * @return 执行 ID 到完成时间的映射
     */
    public static Map<String, Integer> parseCompletedExecutions(String note){
        // 默认为空映射
        if (note == null){
            note = "{}";
        }

        try {
            return JsonSerialization.readValue(note, new TypeReference<Map<String, Integer>>() {});
        } catch (IOException e) {
            logger.warnf("Invalid format of the completed authenticators map. Saved value was: %s", note);
            throw new IllegalStateException(e);
        }
    }

    /**
     * 更新认证会话中已完成认证器执行的 note。
     * @param authSession 当前认证会话
     * @param userSession 先前用户会话
     * @param executionId 刚完成的执行 ID
     */
    public static void updateCompletedExecutions(AuthenticationSessionModel authSession, UserSessionModel userSession, String executionId){
        Map<String, Integer> completedExecutions = parseCompletedExecutions(authSession.getUserSessionNotes().get(Constants.AUTHENTICATORS_COMPLETED));

        // 合并先前会话中已完成的认证器记录
        if (userSession != null){
            Map<String, Integer> prevCompleted = parseCompletedExecutions(userSession.getNote(Constants.AUTHENTICATORS_COMPLETED));
            logger.debugf("merging completed executions from previous authentication session %s", prevCompleted);
            completedExecutions.putAll(prevCompleted);
        }

        // 写入新执行记录并序列化 note
        completedExecutions.put(executionId, Time.currentTime());
        try {
            String updated = JsonSerialization.writeValueAsString(completedExecutions);
            authSession.setUserSessionNote(Constants.AUTHENTICATORS_COMPLETED, updated);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    // 重新认证出错时配置表单为“重新认证”模式（隐藏用户名、禁用注册）
    public static void setupReauthenticationInUsernamePasswordFormError(AuthenticationFlowContext context) {
        String userAlreadySetBeforeUsernamePasswordAuth = context.getAuthenticationSession().getAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH);

        if (Boolean.parseBoolean(userAlreadySetBeforeUsernamePasswordAuth)) {
            LoginFormsProvider form = context.form();
            form.setAttribute(LoginFormsProvider.USERNAME_HIDDEN, true);
            form.setAttribute(LoginFormsProvider.REGISTRATION_DISABLED, true);
        }
    }

    /**
     * 处理 rememberMe 表单输入：realm 启用且勾选时在认证会话与事件中记录。
     *
     * the <em>rememberMe</em> attribute set to <em>on</em> and the realm is
     * configured with the rememberMe option, the auth note is added to the
     * authentication session; otherwise, the note is removed from the auth session.
     * @param context The flow context
     * @param inputData The form data
     */
    /** 根据表单 rememberMe 字段设置或清除认证会话 note。 */
    public static void processRememberMe(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        String rememberMe = inputData.getFirst("rememberMe");
        boolean remember = context.getRealm().isRememberMe() && rememberMe != null && rememberMe.equalsIgnoreCase("on");
        if (remember) {
            context.getAuthenticationSession().setAuthNote(Details.REMEMBER_ME, "true");
            context.getEvent().detail(Details.REMEMBER_ME, "true");
        } else {
            context.getAuthenticationSession().removeAuthNote(Details.REMEMBER_ME);
        }
    }
}
