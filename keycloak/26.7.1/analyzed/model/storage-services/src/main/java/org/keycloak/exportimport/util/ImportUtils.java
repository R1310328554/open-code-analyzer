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

package org.keycloak.exportimport.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.keycloak.Config;
import org.keycloak.exportimport.ExportImportConfig;
import org.keycloak.exportimport.Strategy;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.storage.datastore.DefaultExportImportManager;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

/**
 * 导入工具类：从 JSON 流解析 realm 表示并写入模型，支持批量 realm 与用户导入。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ImportUtils {

    private static final Logger logger = Logger.getLogger(ImportUtils.class);

    /** 批量导入多个 realm：先导入 admin realm，再导入其余 realm，并在需要时重建管理客户端。 */
    public static void importRealms(KeycloakSession session, Collection<RealmRepresentation> realms, Strategy strategy) {
        boolean masterImported = false;

        // 优先导入 admin（master）realm
        for (RealmRepresentation realm : realms) {
            if (Config.getAdminRealm().equals(realm.getRealm())) {
                if (importRealm(session, realm, strategy, () -> {})) {
                    masterImported = true;
                }
            }
        }

        for (RealmRepresentation realm : realms) {
            if (!Config.getAdminRealm().equals(realm.getRealm())) {
                importRealm(session, realm, strategy, () -> {});
            }
        }

        // 若 master 已导入，可能需要为各 realm 重新创建管理客户端
        if (masterImported) {
            session.realms().getRealmsStream()
                    .filter(realm -> realm.getMasterAdminClient() == null)
                    .forEach(realm -> {
                        logger.infof("Re-created management client in master realm for realm '%s'", realm.getName());
                        new RealmManager(session).setupMasterAdminManagement(realm);
            });
        }
    }

    /**
     * 从表示全量导入 realm 并持久化到模型。
     *
     * @param session
     * @param rep
     * @param strategy specifies whether to overwrite or ignore existing realm or user entries
     * @param skipUserDependent If true, then import of any models, which needs users already imported in DB, will be skipped. For example authorization
     * @return newly imported realm (or existing realm if ignoreExisting is true and realm of this name already exists)
     * @deprecated
     */
    @Deprecated
    public static boolean importRealm(KeycloakSession session, RealmRepresentation rep, Strategy strategy, boolean skipUserDependent) {
        return importRealm(session, rep, strategy, skipUserDependent ? null : () -> {});
    }

    /**
     * 从表示全量导入 realm 并持久化到模型。
     *
     * @param session
     * @param rep
     * @param strategy specifies whether to overwrite or ignore existing realm or user entries
     * @param userImport use null to indicate additional users will not be imported
     * @return newly imported realm (or existing realm if ignoreExisting is true and realm of this name already exists)
     */
    public static boolean importRealm(KeycloakSession session, RealmRepresentation rep, Strategy strategy, Runnable userImport) {
        String realmName = rep.getRealm();
        RealmProvider model = session.realms();
        RealmModel realm = model.getRealmByName(realmName);

        if (realm != null) {
            session.getContext().setRealm(realm);
            if (strategy == Strategy.IGNORE_EXISTING) {
                logger.infof("Realm '%s' already exists. Import skipped", realmName);
                return false;
            } else {
                logger.infof("Realm '%s' already exists. Removing it before import", realmName);
                if (Config.getAdminRealm().equals(realm.getId())) {
                    // 因外键约束，先清除各 realm 的 masterAdmin 客户端引用
                   model.getRealmsStream().forEach(r -> r.setMasterAdminClient(null));
                }
                // TODO: 版本迁移场景下，应支持仅删除 realm 而保留其用户
                model.removeRealm(realm.getId());
            }
            session.getContext().setRealm(null);
        }

        RealmManager realmManager = new RealmManager(session);
        realmManager.importRealm(rep, userImport);

        if (System.getProperty(ExportImportConfig.ACTION) != null) {
            logger.infof("Realm '%s' imported", realmName);
        }

        return true;
    }

    /** 从 JSON 输入流解析 realm 表示，支持单 realm 对象或 realm 数组。 */
    public static Map<String, RealmRepresentation> getRealmsFromStream(ObjectMapper mapper, InputStream is) throws IOException {
        Map<String, RealmRepresentation> result = new HashMap<String, RealmRepresentation>();

        JsonFactory factory = mapper.getFactory();
        JsonParser parser = factory.createParser(is);
        try {
            parser.nextToken();

            if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                // 流中包含多个 realm
                parser.nextToken();

                while (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    RealmRepresentation realmRep = parser.readValueAs(RealmRepresentation.class);
                    parser.nextToken();
                    result.put(realmRep.getRealm(), realmRep);
                }
            } else if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                // 流中仅包含单个 realm
                RealmRepresentation realmRep = parser.readValueAs(RealmRepresentation.class);
                result.put(realmRep.getRealm(), realmRep);
            }
        } finally {
            parser.close();
        }

        return result;
    }

    // 假定在事务内调用
    /** 从 JSON 流按批导入本地或联邦用户到指定 realm。 */
    public static void importUsersFromStream(KeycloakSession session, String realmName, ObjectMapper mapper, InputStream is, boolean federated, Consumer<KeycloakSession> onUserCreated) throws IOException {
        RealmProvider model = session.realms();
        JsonFactory factory = mapper.getJsonFactory();
        RealmModel realm = model.getRealmByName(realmName);
        try (JsonParser parser = factory.createJsonParser(is)) {
            parser.nextToken();

            while (parser.nextToken() == JsonToken.FIELD_NAME) {
                if ("realm".equals(parser.getText())) {
                    parser.nextToken();
                    String currRealmName = parser.getText();
                    if (!currRealmName.equals(realmName)) {
                        throw new IllegalStateException("Trying to import users into invalid realm. Realm name: " + realmName + ", Expected realm name: " + currRealmName);
                    }
                } else if ((federated?"federatedUsers":"users").equals(parser.getText())) {
                    parser.nextToken();

                    if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                        parser.nextToken();
                    }

                    while (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                        UserRepresentation user = parser.readValueAs(UserRepresentation.class);
                        if (federated) {
                            DefaultExportImportManager.importFederatedUser(session, realm, user);
                        } else {
                            RepresentationToModel.createUser(session, realm, user);
                        }
                        onUserCreated.accept(session);
                        parser.nextToken();
                    }

                    if (parser.getCurrentToken() == JsonToken.END_ARRAY) {
                        parser.nextToken();
                    }
                }
            }
        }
    }

}
