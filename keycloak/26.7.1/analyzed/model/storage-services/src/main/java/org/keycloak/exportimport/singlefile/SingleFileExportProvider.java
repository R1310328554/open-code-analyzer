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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

import org.keycloak.exportimport.ExportProvider;
import org.keycloak.exportimport.util.ExportImportSessionTask;
import org.keycloak.exportimport.util.ExportUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.util.ObjectMapperResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jboss.logging.Logger;

/**
 * 单文件导出 Provider：将全部 realm 或指定 realm 序列化为一个 JSON 文件。
 * <p>
 * 在 {@link ExportImportSessionTask} 事务上下文中执行导出，并使用 Jackson 流式序列化写入目标文件。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SingleFileExportProvider implements ExportProvider {

    private static final Logger logger = Logger.getLogger(SingleFileExportProvider.class);

    /** 导出目标文件。 */
    private File file;

    private final KeycloakSessionFactory factory;
    /** 指定导出单个 realm 时使用的名称；为 null 表示导出全部 realm。 */
    private String realmName;

    /** 构造单文件导出 Provider。 */
    public SingleFileExportProvider(KeycloakSessionFactory factory) {
        this.factory = factory;
    }

    /** 设置导出目标文件。 */
    public SingleFileExportProvider withFile(File file) {
        this.file = file;
        return this;
    }

    /** 执行模型导出：单 realm 或全模型写入 {@link #file}。 */
    @Override
    public void exportModel() {

        new ExportImportSessionTask() {

            @Override
            protected void runExportImportTask(KeycloakSession session) throws IOException {
                if (realmName != null) {
                    ServicesLogger.LOGGER.realmExportRequested(realmName);
                    exportRealm(session, realmName);
                } else {
                    ServicesLogger.LOGGER.fullModelExportRequested();
                    logger.infof("Exporting model into file %s", file.getAbsolutePath());
                    Stream<RealmRepresentation> realms = session.realms().getRealmsStream()
                            .peek(realm -> session.getContext().setRealm(realm))
                            .map(realm -> ExportUtils.exportRealm(session, realm, true, true));

                    writeToFile(realms);
                }
            }
        }.runTask(factory);
        ServicesLogger.LOGGER.exportSuccess();
    }

    /** 导出指定名称的单个 realm 到 {@link #file}。 */
    private void exportRealm(KeycloakSession session, final String realmName) throws IOException {
        logger.infof("Exporting realm '%s' into file %s", realmName, this.file.getAbsolutePath());
        RealmModel realm = session.realms().getRealmByName(realmName);
        Objects.requireNonNull(realm, "realm not found by realm name '" + realmName + "'");
        session.getContext().setRealm(realm);
        RealmRepresentation realmRep = ExportUtils.exportRealm(session, realm, true, true);
        writeToFile(realmRep);
    }

    @Override
    public void close() {
    }

    /** 获取用于导出的 Jackson 序列化器（缩进输出、忽略空 Bean）。 */
    private ObjectMapper getObjectMapper() {
        ObjectMapper streamSerializer = ObjectMapperResolver.createStreamSerializer();
        streamSerializer.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        streamSerializer.enable(SerializationFeature.INDENT_OUTPUT);
        return streamSerializer;
    }

    /** 将 realm 表示对象或流写入 {@link #file}。 */
    private void writeToFile(Object reps) throws IOException {
        FileOutputStream stream = new FileOutputStream(this.file);
        getObjectMapper().writeValue(stream, reps);
    }

    /** 设置仅导出指定 realm 的名称。 */
    public ExportProvider withRealmName(String realmName) {
        this.realmName = realmName;
        return this;
    }
}
