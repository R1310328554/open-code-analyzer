/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models;

import org.keycloak.migration.MigrationModel;
import org.keycloak.provider.Provider;

/**
 * 部署状态提供者 SPI：暴露数据库迁移/版本等部署元数据。
 * <p>供启动流程判断 schema 版本与迁移需求。</p>
 */
public interface DeploymentStateProvider extends Provider {

    /** @return 当前部署的 {@link org.keycloak.migration.MigrationModel} */
    MigrationModel getMigrationModel();

}
