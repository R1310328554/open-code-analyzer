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
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.common.Profile;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * SPNEGO/Kerberos 认证器工厂，注册发起 SPNEGO 协议协商的认证器；Kerberos 特性未启用时返回禁用版单例。
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SpnegoAuthenticatorFactory implements AuthenticatorFactory {

    /** 提供者 ID：auth-spnego。 */
    public static final String PROVIDER_ID = "auth-spnego";
    /** Kerberos 启用时的单例 {@link SpnegoAuthenticator}。 */
    public static final SpnegoAuthenticator SINGLETON = new SpnegoAuthenticator();
    /** Kerberos 禁用时的占位单例，调用 authenticate 将抛出 IllegalStateException。 */
    public static final SpnegoAuthenticator SINGLETON_DISABLED = new SpnegoAuthenticator() {

        @Override
        public void authenticate(AuthenticationFlowContext context) {
            throw new IllegalStateException("Not possible to authenticate as Kerberos feature is disabled");
        }
    };

    @Override
    /** @return 根据 Kerberos 特性启用状态返回对应单例 */
    public Authenticator create(KeycloakSession session) {
        return isKerberosFeatureEnabled() ? SINGLETON : SINGLETON_DISABLED;
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
    /** @return Kerberos 凭证类型引用分类 */
    public String getReferenceCategory() {
        return UserCredentialModel.KERBEROS;
    }

    @Override
    /** @return 是否可配置（SPNEGO 不可配置） */
    public boolean isConfigurable() {
        return false;
    }

    @Override
    /** @return Kerberos 启用时为标准选项，否则仅 DISABLED */
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return isKerberosFeatureEnabled() ? REQUIREMENT_CHOICES : new AuthenticationExecutionModel.Requirement[]{ AuthenticationExecutionModel.Requirement.DISABLED };
    }


    @Override
    /** @return 认证器提供者 ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Kerberos";
    }

    @Override
    /** @return 认证器帮助说明文本（Kerberos 未启用时提示禁用） */
    public String getHelpText() {
        return isKerberosFeatureEnabled()
                ? "Initiates the SPNEGO protocol.  Most often used with Kerberos."
                : "DISABLED. Please enable Kerberos feature and make sure Kerberos available in your platform. Initiates the SPNEGO protocol. Most often used with Kerberos.";
    }

    @Override
    /** @return 可配置属性列表（本认证器无配置项） */
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    /** @return 是否允许用户自助配置 */
    public boolean isUserSetupAllowed() {
        return false;
    }

    /** @return 是否启用 KERBEROS 特性 */
    private boolean isKerberosFeatureEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.KERBEROS);
    }
}
