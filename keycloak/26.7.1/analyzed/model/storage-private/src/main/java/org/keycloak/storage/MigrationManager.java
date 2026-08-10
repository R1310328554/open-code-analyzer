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

import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * 处理数据存储迁移以及导入的领域表示（{@link RealmRepresentation}）。
 * 后续将由存储层直接承担迁移职责。
 *
 * @author Alexander Schwartz
 */
public interface MigrationManager {

    /** 执行全局数据存储迁移。 */
    void migrate();

    /**
     * 迁移指定领域及其导入表示。
     *
     * @param realm 目标领域
     * @param rep 导入的领域表示
     * @param skipUserDependent 是否跳过依赖用户的迁移步骤
     */
    void migrate(RealmModel realm, RealmRepresentation rep, boolean skipUserDependent);
}
