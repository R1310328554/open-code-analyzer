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

package org.keycloak.quarkus.runtime.vault;

import org.keycloak.Config;
import org.keycloak.config.VaultOptions;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.quarkus.runtime.configuration.Configuration;

/**
 * Quarkus 环境下基于 KeyStore 文件的 {@link org.keycloak.vault.VaultProviderFactory}，仅当 {@link VaultOptions#VAULT} 配置为 {@code keystore} 时启用。
 */
public class FilesKeystoreVaultProviderFactory extends org.keycloak.vault.FilesKeystoreVaultProviderFactory
    implements EnvironmentDependentProviderFactory {

    /** 工厂 SPI 标识符：{@code keystore}。 */
    public static final String ID = "keystore";

    @Override
    public String getId() {
        return ID;
    }

    /** 当前 Quarkus 配置选中本工厂时返回 true。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return getId().equals(Configuration.getConfigValue(VaultOptions.VAULT).getValue());
    }
}
