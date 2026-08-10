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

package org.keycloak.protocol.oidc;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.common.util.FindFile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.util.JsonSerialization;
import org.keycloak.wellknown.WellKnownProvider;
import org.keycloak.wellknown.WellKnownProviderFactory;

import org.jboss.logging.Logger;

/**
 * OIDC Discovery（{@code .well-known/openid-configuration}）Provider 工厂。
 * <p>支持配置文件覆盖元数据及是否包含 client scope 列表。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class OIDCWellKnownProviderFactory implements WellKnownProviderFactory {

    /** Well-known Provider ID。 */
    public static final String PROVIDER_ID = "openid-configuration";

    private static final Logger logger = Logger.getLogger(OIDCWellKnownProviderFactory.class);

    private Map<String, Object> openidConfigOverride = null;
    private boolean includeClientScopes = true;

    /** @param session Keycloak 会话
     * @return OIDC well-known Provider */
    @Override
    public WellKnownProvider create(KeycloakSession session) {
        return new OIDCWellKnownProvider(session, openidConfigOverride, includeClientScopes);
    }

    /** 加载 openid-configuration 覆盖文件与 include-client-scopes 开关。 */
    @Override
    public void init(Config.Scope config) {
        String openidConfigurationOverride = config.get("openid-configuration-override");
        this.includeClientScopes = config.getBoolean("include-client-scopes", true);
        logger.debugf("Include Client Scopes in OIDC Well-known endpoint: %s", this.includeClientScopes);
        if (openidConfigurationOverride != null) {
            initConfigOverrideFromFile(openidConfigurationOverride);
        }
    }

    /** 从文件或 classpath 加载 JSON 覆盖配置。 */
    protected void initConfigOverrideFromFile(String openidConfigurationOverrideFile) {
        try {
            InputStream is = FindFile.findFile(openidConfigurationOverrideFile);
            this.openidConfigOverride = JsonSerialization.readValue(is, Map.class);
            logger.infof("Overriding default OIDC well-known endpoint configuration with the options from file '%s'", openidConfigurationOverrideFile);
        } catch (RuntimeException re) {
            logger.warnf(re, "Unable to find file specified for openid-configuration-override on custom location '%s'. Will stick to the default configuration for OIDC WellKnown endpoint", openidConfigurationOverrideFile);
        } catch (IOException ioe) {
            logger.warnf(ioe, "Error when trying to deserialize JSON from the file '%s'. Check the JSON format. Will stick to the default configuration for OIDC WellKnown endpoint", openidConfigurationOverrideFile);
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    /** @return {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    // 自定义 openid-configuration 实现应优先于默认工厂
    /** @return 较低优先级，便于自定义覆盖 */
    @Override
    public int getPriority() {
        return 100;
    }

    /** Provider 可配置项：覆盖文件路径、是否包含 client scopes。 */
    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("openid-configuration-override")
                .type("string")
                .helpText("The file path from where the metadata should be loaded from. You can use an absolute file path or, if the file is in the server classpath, use the 'classpath:' prefix to load the file from the classpath.")
                .add()
                .property()
                .name("include-client-scopes")
                .type("boolean")
                .helpText("If client scopes should be used to calculate the list of supported scopes.")
                .defaultValue(true)
                .add()
                .build();
    }

    /** @return 文件加载的 openid-configuration 覆盖映射 */
    protected Map<String, Object> getOpenidConfigOverride() {
        return openidConfigOverride;
    }
}
