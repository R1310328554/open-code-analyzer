/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.ipatuura_user_spi;

import java.util.LinkedList;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.http.simple.SimpleHttpResponse;
import org.keycloak.ipatuura_user_spi.authenticator.IpatuuraAuthenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import org.jboss.logging.Logger;

/**
 * Ipatuura 用户联邦工厂：注册 SCIM URL/登录凭据配置并创建 {@link IpatuuraUserStorageProvider}。
 *
 * @author <a href="mailto:jstephen@redhat.com">Justin Stephenson</a>
 * @version $Revision: 1 $
 */
public class IpatuuraUserStorageProviderFactory implements UserStorageProviderFactory<IpatuuraUserStorageProvider>, EnvironmentDependentProviderFactory {

    private static final Logger logger = Logger.getLogger(IpatuuraUserStorageProviderFactory.class);
    public static final String PROVIDER_NAME = "ipatuura";
    protected static final List<String> PROVIDERS = new LinkedList<>();
    protected static final List<ProviderConfigProperty> configMetadata;

    static {
        PROVIDERS.add("ipa");
        PROVIDERS.add("ad");
        PROVIDERS.add("ldap");

        configMetadata = ProviderConfigurationBuilder.create()
                /* SCIMv2 服务端 URL */
                .property().name("scimurl").type(ProviderConfigProperty.STRING_TYPE).label("Ipatuura Server URL")
                .helpText("Backend ipatuura server URL in the format: server.example.com:8080")
                .add()
                /* 用于 HTTP 请求认证的管理员用户名 */
                .property().name("loginusername").type(ProviderConfigProperty.STRING_TYPE).label("Login username")
                .helpText("Username to authenticate through the server")
                .add()
                /* 用于 HTTP 请求认证的管理员密码 */
                .property().name("loginpassword").type(ProviderConfigProperty.PASSWORD).label("Login password")
                .helpText("password to authenticate through the server")
                .secret(true).add().build();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    /** {@inheritDoc} 探测 SCIM 后端连通性。 */
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config)
            throws ComponentValidationException {
        Ipatuura ipatuura = new Ipatuura(session, config);

        SimpleHttpResponse response;

        try {
            response = ipatuura.clientRequest("", "GET", null);
            response.close();
        } catch (Exception e) {
            throw new ComponentValidationException("Cannot connect to provided URL!", e);
        }
    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    /** {@inheritDoc} 创建 Ipatuura 联邦提供器实例。 */
    @Override
    public IpatuuraUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        Ipatuura ipatuura = new Ipatuura(session, model);
        return new IpatuuraUserStorageProvider(session, model, ipatuura, this);
    }

    /** 创建 Kerberos SPNEGO token 解析器。 */
    protected IpatuuraAuthenticator createSCIMAuthenticator() {
        return new IpatuuraAuthenticator();
    }

    /** {@inheritDoc} 需启用 {@link Profile.Feature#IPA_TUURA_FEDERATION}。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.IPA_TUURA_FEDERATION);
    }
}
