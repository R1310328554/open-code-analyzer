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

package org.keycloak.exportimport.dir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.keycloak.Config;
import org.keycloak.connections.jpa.support.EntityManagers;
import org.keycloak.exportimport.AbstractFileBasedImportProvider;
import org.keycloak.exportimport.Strategy;
import org.keycloak.exportimport.util.ExportImportSessionTask;
import org.keycloak.exportimport.util.ExportImportSessionTask.Mode;
import org.keycloak.exportimport.util.ImportUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.resources.KeycloakApplication;
import org.keycloak.storage.datastore.DefaultExportImportManager;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.KeycloakSessionUtil;

import org.jboss.logging.Logger;

/**
 * 目录导入 Provider：从包含 {@code *-realm.json} 与分片用户文件的目录导入 realm。
 * <p>
 * 先导入 realm 定义，再按批处理导入本地与联邦用户；支持单 realm 或全模型导入策略。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DirImportProvider extends AbstractFileBasedImportProvider {

    /** 导入冲突处理策略（OVERWRITE、IGNORE 等）。 */
    private final Strategy strategy;
    private final KeycloakSessionFactory factory;

    private static final Logger logger = Logger.getLogger(DirImportProvider.class);

    /** 导入源目录；未指定时使用临时目录下的 {@code keycloak-export}。 */
    private File rootDirectory;

    /** 仅导入指定 realm 时使用的 realm 名称；为 null 表示导入目录内全部 realm。 */
    private String realmName;

    public DirImportProvider(KeycloakSessionFactory factory, Strategy strategy) {
        this.factory = factory;
        this.strategy = strategy;
    }

    /** 设置导入源目录；目录必须已存在。 */
    public DirImportProvider withDir(String dir) {
        this.rootDirectory = new File(dir);

        if (!this.rootDirectory.exists()) {
            throw new IllegalStateException("Directory " + this.rootDirectory + " doesn't exist");
        }

        logger.infof("Importing from directory %s", this.rootDirectory.getAbsolutePath());
        return this;
    }

    /** 限制仅导入指定名称的 realm。 */
    public DirImportProvider withRealmName(String realmName) {
        this.realmName = realmName;
        return this;
    }

    private File getRootDirectory() {
        if (rootDirectory == null) {
            this.rootDirectory = new File(KeycloakApplication.getTmpDirectory(), "keycloak-export");
            if (!this.rootDirectory.exists()) {
                throw new IllegalStateException("Directory " + this.rootDirectory + " doesn't exist");
            }

            logger.infof("Importing from directory %s", this.rootDirectory.getAbsolutePath());
        }
        return rootDirectory;
    }

    /** 执行目录导入：单 realm 或扫描全部 {@code *-realm.json} 文件。 */
    @Override
    public void importModel() throws IOException {
        if (realmName != null) {
            ServicesLogger.LOGGER.realmImportRequested(realmName, strategy.toString());
            importRealm(realmName, strategy);
        } else {
            ServicesLogger.LOGGER.fullModelImport(strategy.toString());
            List<String> realmNames = getRealmsToImport();

            for (String realmName : realmNames) {
                importRealm(realmName, strategy);
                Optional.ofNullable(KeycloakSessionUtil.getKeycloakSession())
                        .ifPresent(session -> EntityManagers.flush(session, true));
            }
        }
        ServicesLogger.LOGGER.importSuccess();
    }

    /** 判断待导入 realm 列表是否包含 master realm。 */
    @Override
    public boolean isMasterRealmExported() {
        List<String> realmNames = getRealmsToImport();
        return realmNames.contains(Config.getAdminRealm());
    }

    /** 扫描目录中 {@code *-realm.json} 文件并解析 realm 名称；master realm 排在首位。 */
    private List<String> getRealmsToImport() {
        File[] realmFiles = getRootDirectory().listFiles((dir, name) -> (name.endsWith("-realm.json")));
        Objects.requireNonNull(realmFiles, "Directory not found: " + getRootDirectory().getName());
        List<String> realmNames = new ArrayList<>();
        for (File file : realmFiles) {
            String fileName = file.getName();
            // 从 "foo-realm.json" 解析出 "foo"
            String realmName = fileName.substring(0, fileName.length() - 11);

            // 确保 master realm 最先导入
            if (Config.getAdminRealm().equals(realmName)) {
                realmNames.add(0, realmName);
            } else {
                realmNames.add(realmName);
            }
        }
        return realmNames;
    }

    /** 导入单个 realm：先读 realm JSON，再通过批处理任务导入用户与联邦用户分片文件。 */
    public void importRealm(final String realmName, final Strategy strategy) throws IOException {
        File realmFile = new File(getRootDirectory() + File.separator + realmName + "-realm.json");
        File[] userFiles = getRootDirectory().listFiles((dir, name) -> name.matches(realmName + "-users-[0-9]+\\.json"));
        Objects.requireNonNull(userFiles, "directory not found: " + getRootDirectory().getName());
        File[] federatedUserFiles = getRootDirectory().listFiles((dir, name) -> name.matches(realmName + "-federated-users-[0-9]+\\.json"));
        Objects.requireNonNull(federatedUserFiles, "directory not found: " + getRootDirectory().getName());

        // 先导入 realm 主体配置
        InputStream is = parseFile(realmFile);
        final RealmRepresentation realmRep = JsonSerialization.readValue(is, RealmRepresentation.class);
        if (!realmRep.getRealm().equals(realmName)) {
            throw new IllegalStateException(String.format("File name / realm name mismatch. %s, contains realm %s. File name should be %s", realmFile.getName(), realmRep.getRealm(), realmRep.getRealm() + "-realm.json"));
        }

        new ExportImportSessionTask() {

            @Override
            public void runExportImportTask(KeycloakSession session) {
                ImportUtils.importRealm(session, realmRep, strategy, () -> {
                    importUsers(realmName, userFiles, false);
                    importUsers(realmName, federatedUserFiles, true);
                });
            }

        }.runTask(factory);
    }

    /** 从匹配的用户分片文件批量导入本地或联邦用户。 */
    private void importUsers(final String realmName, File[] userFiles, boolean federated) {
        for (final File userFile : userFiles) {
            try (InputStream fis = parseFile(userFile)) {
                new ExportImportSessionTask() {
                    @Override
                    protected void runExportImportTask(KeycloakSession session) throws IOException {
                        session.getContext().setRealm(session.realms().getRealmByName(realmName));
                        ImportUtils.importUsersFromStream(session, realmName, JsonSerialization.mapper, fis, federated, new DefaultExportImportManager.Batcher());
                        logger.infof("Imported %susers from %s", federated?"federated ":"", userFile.getAbsolutePath());
                    }
                }.runTask(factory, Mode.BATCHED);
            } catch (IOException e) {
                throw new RuntimeException("Error during import: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void close() {

    }
}
