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

package org.keycloak.quarkus.runtime.storage.database.liquibase.database;

import liquibase.database.core.MSSQLDatabase;

/**
 * SQL Server {@link MSSQLDatabase} 子类：缓存 {@link #getEngineEdition()} 结果，避免重复查询。
 */
public class CustomMSSQLDatabase extends MSSQLDatabase {

    /** 引擎版本缓存（Liquibase 单线程执行，无需同步）。 */
    private static String ENGINE_EDITION;

    @Override
    public String getEngineEdition() {
        // 引擎版本在会话内不变，仅查询一次即可
        // Liquibase 单线程运行，无并发更新风险
        if (ENGINE_EDITION == null) {
            return ENGINE_EDITION = super.getEngineEdition();
        }
        return ENGINE_EDITION;
    }
}
