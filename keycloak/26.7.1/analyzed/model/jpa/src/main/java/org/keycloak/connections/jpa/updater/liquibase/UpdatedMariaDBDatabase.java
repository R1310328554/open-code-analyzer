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

package org.keycloak.connections.jpa.updater.liquibase;

import java.util.HashSet;
import java.util.Set;

import liquibase.database.core.MariaDBDatabase;

/**
 * Keycloak 定制的 MariaDB {@link liquibase.database.Database} 实现。
 * <p>补充 Liquibase 未识别的保留字，并提高优先级以覆盖默认 {@link MariaDBDatabase}。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UpdatedMariaDBDatabase extends MariaDBDatabase {

    /** Liquibase 默认未收录、但 MariaDB 实际保留的标识符。 */
    private static final Set<String> RESERVED_WORDS = new HashSet<>();

    /** 判断标识符是否为保留字（含 Keycloak 额外维护的集合）。 */
    @Override
    public boolean isReservedWord(String string) {
        return super.isReservedWord(string) || RESERVED_WORDS.contains(string.toUpperCase());
    }

    /** 优先级高于工厂默认实现，确保 Keycloak 选用本类。 */
    @Override
    public int getPriority() {
        return super.getPriority() + 1; // 始终优先于 Liquibase 内置 MariaDBDatabase
    }

    static {
        RESERVED_WORDS.add("PERIOD");
    }
}
