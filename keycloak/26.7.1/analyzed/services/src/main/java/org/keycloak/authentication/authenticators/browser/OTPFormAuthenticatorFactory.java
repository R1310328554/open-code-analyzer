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

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * OTP 表单认证器工厂，注册在独立 OTP 表单页校验一次性密码的认证器。
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class OTPFormAuthenticatorFactory implements AuthenticatorFactory {

    /** 提供者 ID：auth-otp-form。 */
    public static final String PROVIDER_ID = "auth-otp-form";
    /** 单例 {@link OTPFormAuthenticator} 实例。 */
    public static final OTPFormAuthenticator SINGLETON = new OTPFormAuthenticator();

    @Override
    /** @return 单例 OTP 表单认证器 */
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    /** @return 认证器提供者 ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** @return OTP 凭证类型引用分类 */
    public String getReferenceCategory() {
        return OTPCredentialModel.TYPE;
    }

    @Override
    /** @return 是否可配置（OTP 表单不可配置） */
    public boolean isConfigurable() {
        return false;
    }

    @Override
    /** @return 是否允许用户自助配置 OTP */
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    /** @return 允许的执行要求选项 */
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "OTP Form";
    }

    @Override
    /** @return 认证器帮助说明文本 */
    public String getHelpText() {
        return "Validates a OTP on a separate OTP form.";
    }

    @Override
    /** @return 可配置属性列表（本认证器无配置项） */
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }
}
