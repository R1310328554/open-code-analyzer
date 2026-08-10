/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.deployment.DeployedConfigurationsManager;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.provider.ScriptProviderMetadata;

/**
 * 已部署脚本认证器工厂：从 {@link ScriptProviderMetadata} 动态创建基于脚本的认证器，并在 postInit 中注册配置。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public final class DeployedScriptAuthenticatorFactory extends ScriptBasedAuthenticatorFactory {

    private ScriptProviderMetadata metadata;
    private AuthenticatorConfigModel model;
    private List<ProviderConfigProperty> configProperties;
    private Authenticator authenticator = new ScriptBasedAuthenticator() {
        @Override
        protected AuthenticatorConfigModel getAuthenticatorConfig(AuthenticationFlowContext context) {
            return model;
        }
    };

    public DeployedScriptAuthenticatorFactory(ScriptProviderMetadata metadata) {
        this.metadata = metadata;
    }

    /** 无参构造，供反射实例化。 */
    public DeployedScriptAuthenticatorFactory() {
        // 供反射使用
    }

    @Override
    /** @return 绑定固定配置的脚本认证器实例 */
    public Authenticator create(KeycloakSession session) {
        return authenticator;
    }

    @Override
    public String getId() {
        return metadata.getId();
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getDisplayType() {
        return model.getAlias();
    }

    @Override
    public String getHelpText() {
        return model.getAlias();
    }

    @Override
    /** 从 metadata 创建 {@link AuthenticatorConfigModel} 并加载配置属性。 */
    public void init(Config.Scope config) {
        model = createModel(metadata);
        configProperties = super.getConfigProperties();
    }

    @Override
    /** 在事务中向 {@link DeployedConfigurationsManager} 注册已部署认证器配置。 */
    public void postInit(KeycloakSessionFactory factory) {
        KeycloakModelUtils.runJobInTransaction(factory, session -> {
            new DeployedConfigurationsManager(session).registerDeployedAuthenticatorConfig(model);
        });
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public AuthenticatorConfigModel getConfig() {
        return model;
    }

    public void setMetadata(ScriptProviderMetadata metadata) {
        this.metadata = metadata;
    }

    public ScriptProviderMetadata getMetadata() {
        return metadata;
    }

    /** 从脚本 metadata 构建认证器配置（含 scriptName/scriptCode/scriptDescription）。 */
    private AuthenticatorConfigModel createModel(ScriptProviderMetadata metadata) {
        AuthenticatorConfigModel model = new AuthenticatorConfigModel();

        model.setId(metadata.getId());
        model.setAlias(sanitizeString(metadata.getName()));

        Map<String, String> config = new HashMap<>();

        model.setConfig(config);

        config.put("scriptName", metadata.getName());
        config.put("scriptCode", metadata.getCode());
        config.put("scriptDescription", metadata.getDescription());

        return model;
    }

    /** 将名称中的 / 与 . 替换为 -，用作配置 alias。 */
    private String sanitizeString(String value) {
        return value.replace('/', '-').replace('.', '-');
    }
}
