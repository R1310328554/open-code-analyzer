/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.storage;

import java.io.InputStream;

import org.keycloak.exportimport.ExportAdapter;
import org.keycloak.exportimport.ExportOptions;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.partialimport.PartialImportResults;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * 导入导出管理器：管理 Realm 的导入、更新与导出。
 * Manage importing and updating of realms for the store.
 *
 * @author Alexander Schwartz
 */
public interface ExportImportManager {
    /** 将 Realm 表示导入到新 Realm（含用户导入回调）。 */
    void importRealm(RealmRepresentation rep, RealmModel newRealm, Runnable userImport);

    /** 对已有 Realm 执行部分导入。 */
    PartialImportResults partialImportRealm(RealmModel realm, InputStream requestBody);

    /** 根据表示更新已有 Realm。 */
    void updateRealm(RealmRepresentation rep, RealmModel realm);

    /** 在 Realm 中创建用户。 */
    UserModel createUser(RealmModel realm, UserRepresentation userRep);

    /** 导出 Realm 并通过回调写入。 */
    void exportRealm(RealmModel realm, ExportOptions options, ExportAdapter callback);

    /** 从输入流导入 Realm 并返回模型。 */
    RealmModel importRealm(InputStream requestBody);
}
