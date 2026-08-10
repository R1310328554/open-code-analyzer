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

package org.keycloak.migration.migrators;

import org.keycloak.migration.ModelVersion;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * 单次模型版本迁移步骤接口。
 * <p>实现类在启动迁移链中按 {@link #getVersion()} 顺序执行 {@link #migrate}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface Migration {

    /** 执行本版本的数据库/模型结构迁移。 */
    void migrate(KeycloakSession session);

    /**
     * 领域 JSON 完整导入后调用，用于补充迁移逻辑。
     * <p>实现不应假设导入文件内容与导出时完全一致。</p>
     *
     * @param session
     * @param realm
     * @param rep
     * @param skipUserDependent
     */
    default
    void migrateImport(KeycloakSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {

    }

    /** 本迁移步骤对应的目标模型版本。 */
    ModelVersion getVersion();

}
