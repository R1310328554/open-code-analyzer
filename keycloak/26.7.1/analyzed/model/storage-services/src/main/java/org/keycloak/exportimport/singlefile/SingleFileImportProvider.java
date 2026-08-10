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
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.exportimport.AbstractFileBasedImportProvider;
import org.keycloak.exportimport.Strategy;
import org.keycloak.exportimport.util.ExportImportSessionTask;
import org.keycloak.exportimport.util.ImportUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;

/**
 * 单文件导入 Provider：从单个 JSON 文件导入一个或多个 realm。
 * <p>
 * 解析文件后缓存 {@link RealmRepresentation}，避免重复解析；在 {@link ExportImportSessionTask} 中调用 {@link ImportUtils#importRealms}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SingleFileImportProvider extends AbstractFileBasedImportProvider {

    private static final Logger logger = Logger.getLogger(SingleFileImportProvider.class);
    private final KeycloakSessionFactory factory;

    /** 导入源 JSON 文件。 */
    private final File file;
    /** 导入冲突处理策略。 */
    private final Strategy strategy;

    // 按 Provider 缓存 realm 表示，避免重复解析同一文件
    protected Map<String, RealmRepresentation> realmReps;

    /** 构造单文件导入 Provider。 */
    public SingleFileImportProvider(KeycloakSessionFactory factory, File file, Strategy strategy) {
        this.factory = factory;
        this.file = file;
        this.strategy = strategy;
    }

    /** 从 {@link #file} 全量导入所有 realm。 */
    @Override
    public void importModel() throws IOException {
        logger.infof("Full importing from file %s", this.file.getAbsolutePath());
        checkRealmReps();

        new ExportImportSessionTask() {

            @Override
            protected void runExportImportTask(KeycloakSession session) {
                ImportUtils.importRealms(session, realmReps.values(), strategy);
            }

        }.runTask(factory);
    }

    /** 检查导出内容是否包含 master（admin）realm。 */
    @Override
    public boolean isMasterRealmExported() throws IOException {
        checkRealmReps();
        return (realmReps.containsKey(Config.getAdminRealm()));
    }

    /** 懒加载解析 {@link #file} 并填充 {@link #realmReps}。 */
    protected void checkRealmReps() throws IOException {
        if (realmReps == null) {
            InputStream is = parseFile(file);
            realmReps = ImportUtils.getRealmsFromStream(JsonSerialization.mapper, is);
        }
    }

    @Override
    public void close() {

    }
}
