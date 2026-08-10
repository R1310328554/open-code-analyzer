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

package org.keycloak.exportimport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

import org.jboss.logging.Logger;

import static org.keycloak.exportimport.ExportImportConfig.PROVIDER;
import static org.keycloak.exportimport.ExportImportConfig.PROVIDER_DEFAULT;

/**
 * 导出/导入管理器：根据 {@link ExportImportConfig} 初始化 {@link ExportProvider} 或 {@link ImportProvider}，
 * 并在启动时扫描目录自动导入领域快照。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ExportImportManager {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(ExportImportManager.class);

    private final KeycloakSessionFactory sessionFactory;
    private final KeycloakSession session;

    /** 导出提供者；非导出模式时为 null。 */
    private ExportProvider exportProvider;
    /** 待执行的导入提供者列表。 */
    private List<ImportProvider> importProviders = List.of();

    /** 根据系统属性与配置解析导出或导入提供者。 */
    public ExportImportManager(KeycloakSession session) {
        this.sessionFactory = session.getKeycloakSessionFactory();
        this.session = session;

        String exportImportAction = ExportImportConfig.getAction();

        if (ExportImportConfig.ACTION_EXPORT.equals(exportImportAction)) {
            // 后续重构：若集成测试不再依赖系统属性，可改为标准 Provider 配置方式
            // a default provider in its standard way.
            // Quarkus 环境下将此项设为 "provider" 尚不可用，会在 KeycloakProcessor.loadFactories() 中触发 NPE
            // （Config.getProvider() 时 value 为 null）
            // 调用 KeycloakProcessor.loadFactories() 中的 Config.getProvider() 时发生
            String providerId = System.getProperty(PROVIDER, Config.scope("export").get("exporter", PROVIDER_DEFAULT));
            exportProvider = session.getProvider(ExportProvider.class, providerId);
            if (exportProvider == null) {
                throw new RuntimeException("Export provider '" + providerId + "' not found");
            }
        } else if (ExportImportConfig.ACTION_IMPORT.equals(exportImportAction)) {
            String providerId = System.getProperty(PROVIDER, Config.scope("import").get("importer", PROVIDER_DEFAULT));
            ImportProvider importProvider = session.getProvider(ImportProvider.class, providerId);
            if (importProvider == null) {
                throw new RuntimeException("Import provider '" + providerId + "' not found");
            }
            importProviders = List.of(importProvider);
        } else if (ExportImportConfig.getDir().isPresent()) { // 启动时按目录自动导入
            ExportImportConfig.setStrategy(Strategy.IGNORE_EXISTING);
            ExportImportConfig.setReplacePlaceholders(true);
            // 启用导入过程日志
            ExportImportConfig.setAction(ExportImportConfig.ACTION_IMPORT);
            importProviders = getStartupImportProviders();
        }
    }

    /** @return 待导入数据是否包含 master 领域 */
    public boolean isImportMasterIncluded() {
        return importProviders.stream().anyMatch(provider -> {
                    try {
                        return provider.isMasterRealmExported();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to run import", e);
                    }
                });
    }

    /** @return 当前是否配置了导出动作 */
    public boolean isRunExport() {
        return exportProvider != null;
    }

    /** 依次执行所有已配置的导入提供者。 */
    public void runImport() {
        importProviders.forEach(ip -> {
            try {
                ip.importModel();
            } catch (IOException e) {
                throw new RuntimeException("Failed to run import", e);
            }
        });
    }

    /** 扫描迁移目录，为 {@code dir} 与 {@code singleFile} 提供者创建启动导入实例。 */
    private List<ImportProvider> getStartupImportProviders() {
        var dirProp = ExportImportConfig.getDir();
        if (dirProp.isEmpty()) {
            return List.of();
        }
        String dir = dirProp.get();

        Stream<ProviderFactory> factories = sessionFactory.getProviderFactoriesStream(ImportProvider.class);

        return factories.flatMap(factory -> {
            String providerId = factory.getId();

            if ("dir".equals(providerId)) {
                return Stream.of(session.getProvider(ImportProvider.class, providerId));
            }
            if ("singleFile".equals(providerId)) {
                Set<String> filesToImport = new HashSet<>();

                File[] files = Paths.get(dir).toFile().listFiles();
                Objects.requireNonNull(files, "directory not found");
                for (File file : files) {
                    Path filePath = file.toPath();

                    if (!(Files.exists(filePath) && Files.isRegularFile(filePath) && filePath.toString().endsWith(".json"))) {
                        logger.debugf("Ignoring import file because it is not a valid file: %s", file);
                        continue;
                    }

                    String fileName = file.getName();

                    if (fileName.contains("-realm.json") || fileName.contains("-users-")) {
                        continue;
                    }

                    filesToImport.add(file.getAbsolutePath());
                }

                if (factory instanceof ImportProviderFactory) {
                    return filesToImport.stream().map(file -> ((ImportProviderFactory)factory).create(session, Map.of(ExportImportConfig.FILE, file)));
                }
            }
            return Stream.empty();
        }).toList();
    }

    /** 执行已配置的导出提供者。 */
    public void runExport() {
        try {
            exportProvider.exportModel();
        } catch (IOException e) {
            throw new RuntimeException("Failed to run export", e);
        }
    }

}
