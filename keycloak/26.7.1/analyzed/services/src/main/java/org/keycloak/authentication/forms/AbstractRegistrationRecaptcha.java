/*
 *
 *  * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authentication.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

/**
 * 注册表单 reCAPTCHA 抽象基类：同时实现 {@link FormAction} 与 {@link FormActionFactory}，负责在注册页注入 reCAPTCHA 脚本并校验用户响应。
 */
public abstract class AbstractRegistrationRecaptcha implements FormAction, FormActionFactory {

    /** 表单字段名：reCAPTCHA 响应令牌。 */
    public static final String G_RECAPTCHA_RESPONSE = "g-recaptcha-response";
    /** 管理控制台引用分类。 */
    public static final String RECAPTCHA_REFERENCE_CATEGORY = "recaptcha";

    // 配置键常量
    /** 配置键：reCAPTCHA 站点密钥。 */
    public static final String SITE_KEY = "site.key";
    /** 配置键：reCAPTCHA 动作名称（如 register）。 */
    public static final String ACTION = "action";
    /** 配置键：是否为 reCAPTCHA v3（隐形/评分型）。 */
    public static final String INVISIBLE = "recaptcha.v3";
    /** 配置键：是否使用 recaptcha.net 域名。 */
    public static final String USE_RECAPTCHA_NET = "useRecaptchaNet";

    private static final Logger LOGGER = Logger.getLogger(AbstractRegistrationRecaptcha.class);

    @Override
    public String getReferenceCategory() {
        return RECAPTCHA_REFERENCE_CATEGORY;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    /** 根据配置返回 recaptcha.net 或 google.com 域名。 */
    protected String getRecaptchaDomain(Map<String, String> config) {
        return Boolean.parseBoolean(config.get(USE_RECAPTCHA_NET)) ? "recaptcha.net" : "google.com";
    }

    @Override
    /** 向注册页注入 reCAPTCHA 脚本与站点密钥等属性。 */
    public void buildPage(FormContext context, LoginFormsProvider form) {
        LOGGER.trace("Building page with reCAPTCHA");

        Map<String, String> config = context.getAuthenticatorConfig().getConfig();

        if (config == null) {
            form.addError(new FormMessage(null, Messages.RECAPTCHA_NOT_CONFIGURED));
            return;
        }

        if (!validateConfig(config)) {
            form.addError(new FormMessage(null, Messages.RECAPTCHA_NOT_CONFIGURED));
            return;
        }

        String userLanguageTag = context.getSession().getContext().resolveLocale(context.getUser())
                .toLanguageTag();
        boolean invisible = Boolean.parseBoolean(config.get(INVISIBLE));
        String action = StringUtil.isNullOrEmpty(config.get(ACTION)) ? "register" : config.get(ACTION);

        form.setAttribute("recaptchaRequired", true);
        form.setAttribute("recaptchaSiteKey", config.get(SITE_KEY));
        form.setAttribute("recaptchaAction", action);
        form.setAttribute("recaptchaVisible", !invisible);
        form.addScript(getScriptUrl(config, userLanguageTag));
    }

    /** @return reCAPTCHA JavaScript 脚本 URL */
    protected abstract String getScriptUrl(Map<String, String> config, String userLanguageTag);

    /** 检查 reCAPTCHA 配置是否完整有效。 */
    protected abstract boolean validateConfig(Map<String, String> config);

    @Override
    /** 校验表单提交的 reCAPTCHA 响应令牌。 */
    public void validate(ValidationContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String captcha = formData.getFirst(G_RECAPTCHA_RESPONSE);
        LOGGER.trace("Got captcha: " + captcha);

        Map<String, String> config = context.getAuthenticatorConfig().getConfig();

        if (!Validation.isBlank(captcha)) {
            if (validate(context, captcha, config)) {
                context.success();
                return;
            }
        }

        List<FormMessage> errors = new ArrayList<>();
        errors.add(new FormMessage(null, Messages.RECAPTCHA_FAILED));
        context.error(Errors.INVALID_REGISTRATION);
        context.validationError(formData, errors);
        context.excludeOtherErrors();

    }

    /** 子类实现：向 Google API 验证 captcha 令牌。 */
    protected abstract boolean validate(ValidationContext context, String captcha, Map<String, String> config);

    @Override
    public void success(FormContext context) {
    }

    @Override
    /** @return 注册 reCAPTCHA 不要求已识别用户 */
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public FormAction create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    /** @return 动作名、recaptcha.net 及 v3 开关等配置项 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(ACTION)
                .label("Action Name")
                .helpText("A meaningful name for this reCAPTCHA context (e.g. login, register). "
                        + "An action name can only contain alphanumeric characters, "
                        + "slashes and underscores and is not case-sensitive.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("register")
                .add()
                .property()
                .name(USE_RECAPTCHA_NET)
                .label("Use recaptcha.net")
                .helpText("Whether to use recaptcha.net instead of google.com, "
                        + "which may have other cookies set.")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(false)
                .add()
                .property()
                .name(INVISIBLE)
                .label("reCAPTCHA v3")
                .helpText("Whether the site key belongs to a v3 (invisible, score-based reCAPTCHA) "
                        + "or v2 site (visible, checkbox-based).")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(false)
                .add()
                .build();
    }
}
