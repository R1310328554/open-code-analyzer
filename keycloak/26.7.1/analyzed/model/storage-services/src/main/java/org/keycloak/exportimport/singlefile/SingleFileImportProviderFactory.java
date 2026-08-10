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

package org.keycloak.exportimport.singlefile;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.exportimport.ExportImportConfig;
import org.keycloak.exportimport.ImportProvider;
import org.keycloak.exportimport.ImportProviderFactory;
import org.keycloak.exportimport.Strategy;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import static org.keycloak.exportimport.ExportImportConfig.DEFAULT_STRATEGY;

/**
 * 单文件导入 Provider 工厂（ID {@code singleFile}）：从配置或系统属性创建 {@link SingleFileImportProvider}。
 * <p>
 * 为兼容旧测试框架，可通过系统属性覆盖 SPI 配置项。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SingleFileImportProviderFactory implements ImportProviderFactory {

    /** Provider 标识符（与 {@link SingleFileExportProviderFactory} 相同）。 */
    public static final String PROVIDER_ID = SingleFileExportProviderFactory.PROVIDER_ID;

    /** 指定导入单个 realm 的名称配置键。 */
    public static final String REALM_NAME = "realmName";
    /** 导入冲突处理策略配置键。 */
    public static final String STRATEGY = "strategy";

    /** 导入源 JSON 文件路径配置键。 */
    public static final String FILE = "file";

    private Config.Scope config;

    /** 创建配置好文件路径与导入策略的 {@link SingleFileImportProvider}。 */
    @Override
    public ImportProvider create(KeycloakSession session, Map<String, String> overrides) {
        Strategy strategy = Enum.valueOf(Strategy.class, System.getProperty(ExportImportConfig.STRATEGY, config.get(STRATEGY, DEFAULT_STRATEGY.toString())));
        String fileName = overrides.getOrDefault(ExportImportConfig.FILE, System.getProperty(ExportImportConfig.FILE, config.get(FILE)));
        if (fileName == null) {
            throw new IllegalArgumentException("Property " + FILE + " needs to be provided!");
        }
        return new SingleFileImportProvider(session.getKeycloakSessionFactory(), new File(fileName), strategy);
    }

    @Override
    public void init(Config.Scope config) {
        this.config = config;
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(REALM_NAME)
                .type("string")
                .helpText("Realm to export")
                .add()

                .property()
                .name(FILE)
                .type("string")
                .helpText("File to import from")
                .add()

                .property()
                .name(STRATEGY)
                .type("string")
                .helpText("Strategy for import: " + Strategy.IGNORE_EXISTING.name() + ", " + Strategy.OVERWRITE_EXISTING)
                .add()

                .build();
    }

}
