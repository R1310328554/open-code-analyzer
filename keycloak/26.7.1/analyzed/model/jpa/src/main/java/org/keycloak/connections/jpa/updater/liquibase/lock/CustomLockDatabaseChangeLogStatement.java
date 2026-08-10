/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.connections.jpa.updater.liquibase.lock;

import liquibase.statement.core.LockDatabaseChangeLogStatement;

/**
 * 扩展 Liquibase 标准锁语句，携带 {@link org.keycloak.models.dblock.DBLockProvider.Namespace} 对应的锁行 ID。
 * <p>Keycloak 在 {@code DATABASECHANGELOGLOCK} 表中为各命名空间预留独立行，加锁时按 ID 选取目标行。</p>
 *
 * @author rmartinc
 */
public class CustomLockDatabaseChangeLogStatement extends LockDatabaseChangeLogStatement {

    /** 锁表行主键，对应 {@code DBLockProvider.Namespace#getId()}。 */
    final private int id;

    /** @param id 要锁定的命名空间 ID */
    public CustomLockDatabaseChangeLogStatement(int id) {
        this.id = id;
    }

    /** @return 锁表行 ID */
    public int getId() {
        return id;
    }

}
