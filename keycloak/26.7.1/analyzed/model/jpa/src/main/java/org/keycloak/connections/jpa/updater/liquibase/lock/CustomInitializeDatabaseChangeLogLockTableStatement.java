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

import java.util.Set;

import liquibase.statement.core.InitializeDatabaseChangeLogLockTableStatement;

/**
 * 扩展 Liquibase 锁表初始化语句，携带数据库中已存在的锁 ID 集合。
 * <p>供 {@link CustomInsertLockRecordGenerator} 仅插入缺失的命名空间锁行，避免重复或误删。</p>
 *
 * @author rmartinc
 */
public class CustomInitializeDatabaseChangeLogLockTableStatement extends InitializeDatabaseChangeLogLockTableStatement {

    /** 迁移前 DATABASECHANGELOGLOCK 表中已有的 ID。 */
    private final Set<Integer> currentIds;

    public CustomInitializeDatabaseChangeLogLockTableStatement(Set<Integer> currentIds) {
        this.currentIds = currentIds;
    }

    public Set<Integer> getCurrentIds() {
        return currentIds;
    }
}
