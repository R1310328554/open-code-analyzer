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
package org.keycloak.authentication.authenticators.browser;

import java.util.Map;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.ScriptModel;
import org.keycloak.models.UserModel;
import org.keycloak.scripting.InvocableScriptAdapter;
import org.keycloak.scripting.ScriptExecutionException;
import org.keycloak.scripting.ScriptingProvider;

import org.jboss.logging.Logger;

/**
 * 可在认证流程中执行已配置脚本的 {@link Authenticator}。
 * An {@link Authenticator} that can execute a configured script during authentication flow.
 * <p>
 * Scripts must at least provide one of the following functions:
 * <ol>
 * <li>{@code authenticate(..)} which is called from {@link Authenticator#authenticate(AuthenticationFlowContext)}</li>
 * <li>{@code action(..)} which is called from {@link Authenticator#action(AuthenticationFlowContext)}</li>
 * </ol>
 * </p>
 * <p>
 * Custom {@link Authenticator Authenticator's} should at least provide the {@code authenticate(..)} function.
 * The following script {@link javax.script.Bindings} are available for convenient use within script code.
 * <ol>
 * <li>{@code script} the {@link ScriptModel} to access script metadata</li>
 * <li>{@code realm} the {@link RealmModel}</li>
 * <li>{@code user} the current {@link UserModel}</li>
 * <li>{@code session} the active {@link KeycloakSession}</li>
 * <li>{@code authenticationSession} the current {@link org.keycloak.sessions.AuthenticationSessionModel}</li>
 * <li>{@code httpRequest} the current {@link org.keycloak.http.HttpRequest}</li>
 * <li>{@code LOG} a {@link org.jboss.logging.Logger} scoped to {@link ScriptBasedAuthenticator}</li>
 * </ol>
 * </p>
 * <p>
 * Note that the {@code user} variable is only defined when the user was identified by a preceding
 * authentication step, e.g. by the {@link UsernamePasswordForm} authenticator.
 * </p>
 * <p>
 * Additional context information can be extracted from the {@code context} argument passed to the {@code authenticate(context)}
 * or {@code action(context)} function.
 * <p>
 * An example {@link ScriptBasedAuthenticator} definition could look as follows:
 * <pre>
 * {@code
 *
 *   AuthenticationFlowError = Java.type("org.keycloak.authentication.AuthenticationFlowError");
 *
 *   function authenticate(context) {
 *
 *     var username = user ? user.username : "anonymous";
 *     LOG.info(script.name + " --> trace auth for: " + username);
 *
 *     if (   username === "tester"
 *         && user.getAttribute("someAttribute")
 *         && user.getAttribute("someAttribute").contains("someValue")) {
 *
 *         context.failure(AuthenticationFlowError.INVALID_USER);
 *         return;
 *     }
 *
 *     context.success();
 *   }
 * }
 * </pre>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class ScriptBasedAuthenticator implements Authenticator {

    private static final Logger LOGGER = Logger.getLogger(ScriptBasedAuthenticator.class);

    /** 配置项键：脚本源代码。 */
    static final String SCRIPT_CODE = "scriptCode";
    /** 配置项键：脚本名称。 */
    static final String SCRIPT_NAME = "scriptName";
    /** 配置项键：脚本描述。 */
    static final String SCRIPT_DESCRIPTION = "scriptDescription";

    /** 脚本中 action 处理函数名。 */
    static final String ACTION_FUNCTION_NAME = "action";
    /** 脚本中 authenticate 处理函数名。 */
    static final String AUTHENTICATE_FUNCTION_NAME = "authenticate";

    @Override
    /** 调用脚本中的 authenticate 函数处理认证步骤。 */
    public void authenticate(AuthenticationFlowContext context) {
        tryInvoke(AUTHENTICATE_FUNCTION_NAME, context);
    }

    @Override
    /** 调用脚本中的 action 函数处理表单提交。 */
    public void action(AuthenticationFlowContext context) {
        tryInvoke(ACTION_FUNCTION_NAME, context);
    }

    /** 尝试调用脚本指定函数；未配置或函数未定义时静默跳过，执行异常则标记 INTERNAL_ERROR。 */
    private void tryInvoke(String functionName, AuthenticationFlowContext context) {

        if (!hasAuthenticatorConfig(context)) {
            // 尚未配置的脚本认证器，标记成功以免因配置不完整锁定用户
            // we mark this execution as success to not lock out users due to incompletely configured authenticators.
            context.success();
            return;
        }

        InvocableScriptAdapter invocableScriptAdapter = getInvocableScriptAdapter(context);

        if (!invocableScriptAdapter.isDefined(functionName)) {
            return;
        }

        try {
            // 是否应将 context 包装为只读？
            invocableScriptAdapter.invokeFunction(functionName, context);
        } catch (ScriptExecutionException e) {
            LOGGER.error(e);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
        }
    }

    /** @return 认证器是否已有非空配置 */
    private boolean hasAuthenticatorConfig(AuthenticationFlowContext context) {
        if (context == null)
            return false;
        AuthenticatorConfigModel config = getAuthenticatorConfig(context);
        return config != null
                && config.getConfig() != null
                && !config.getConfig().isEmpty();
    }

    /** @return 当前执行的认证器配置模型 */
    protected AuthenticatorConfigModel getAuthenticatorConfig(AuthenticationFlowContext context) {
        return context.getAuthenticatorConfig();
    }

    /** 从配置创建 JavaScript 脚本并准备可调用适配器及绑定变量。 */
    private InvocableScriptAdapter getInvocableScriptAdapter(AuthenticationFlowContext context) {

        Map<String, String> config = getAuthenticatorConfig(context).getConfig();

        String scriptName = config.get(SCRIPT_NAME);
        String scriptCode = config.get(SCRIPT_CODE);
        String scriptDescription = config.get(SCRIPT_DESCRIPTION);

        RealmModel realm = context.getRealm();

        ScriptingProvider scripting = context.getSession().getProvider(ScriptingProvider.class);

        // TODO：按 scriptId 查找脚本，避免每次创建
        ScriptModel script = scripting.createScript(realm.getId(), ScriptModel.TEXT_JAVASCRIPT, scriptName, scriptCode, scriptDescription);

        // 如何处理长时间运行脚本 -> 超时？
        return scripting.prepareInvocableScript(script, bindings -> {
            bindings.put("script", script);
            bindings.put("realm", context.getRealm());
            bindings.put("user", context.getUser());
            bindings.put("session", context.getSession());
            bindings.put("httpRequest", context.getHttpRequest());
            bindings.put("authenticationSession", context.getAuthenticationSession());
            bindings.put("LOG", LOGGER);
        });
    }

    @Override
    /** @return 本步骤不要求上下文中已有用户（脚本内按需使用 user 变量） */
    public boolean requiresUser() {
        return false;
    }

    @Override
    /** @return 始终已配置（对所有用户适用） */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    /** 无操作（TODO：可通过脚本配置必需操作）。 */
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // TODO：使 RequiredActions 可在脚本中配置
        //NOOP
    }

    @Override
    public void close() {
        //NOOP
    }
}
