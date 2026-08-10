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
import java.util.Objects;

import org.keycloak.Config;
import org.keycloak.exportimport.ExportImportConfig;
import org.keycloak.exportimport.ExportProvider;
import org.keycloak.exportimport.ExportProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * 单文件导出 Provider 工厂（ID {@code singleFile}）：创建 {@link SingleFileExportProvider} 以导出单个或多个 realm。
 * <p>
 * 为兼容旧测试框架，可通过系统属性覆盖 SPI 配置项。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SingleFileExportProviderFactory implements ExportProviderFactory {

    /** Provider 标识符。 */
    public static final String PROVIDER_ID = "singleFile";
    /** 导出目标文件路径配置键。 */
    public static final String FILE = "file";
    /** 指定导出单个 realm 的名称配置键。 */
    public static final String REALM_NAME = "realmName";
    private Config.Scope config;

    /** 创建配置好目标文件与 realm 名称的 {@link SingleFileExportProvider}。 */
    @Override
    public ExportProvider create(KeycloakSession session) {
        String fileName = System.getProperty(ExportImportConfig.FILE, config.get(FILE));
        Objects.requireNonNull(fileName, "file name not configured");
        String realmName = System.getProperty(ExportImportConfig.REALM_NAME, config.get(REALM_NAME));
        return new SingleFileExportProvider(session.getKeycloakSessionFactory()).withFile(new File(fileName)).withRealmName(realmName);
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
                .helpText("File to export to")
                .add()

                .build();
    }

}
