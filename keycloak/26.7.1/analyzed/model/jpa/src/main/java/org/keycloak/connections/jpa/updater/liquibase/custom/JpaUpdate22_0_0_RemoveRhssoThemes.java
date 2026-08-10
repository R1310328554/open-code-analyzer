/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.connections.jpa.updater.liquibase.custom;

import org.keycloak.theme.DefaultThemeSelectorProvider;

import liquibase.exception.CustomChangeException;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Table;

/**
 * <p>Keycloak 22.0.0 迁移：移除已废弃的 <em>rh-sso</em> 主题引用。</p>
 * <p>将领域与客户端上指向 RH-SSO 主题的设置清空或替换为 Keycloak 默认主题。</p>
 *
 * @author rmartinc
 */
public class JpaUpdate22_0_0_RemoveRhssoThemes extends CustomKeycloakTask {

    @Override
    protected void generateStatementsImpl() throws CustomChangeException {
        // 清除领域的 rh-sso 登录主题
        statements.add(new UpdateStatement(null, null, database.correctObjectName("REALM", Table.class))
                .addNewColumnValue("LOGIN_THEME", null)
                .setWhereClause("LOGIN_THEME=?")
                .addWhereParameter("rh-sso"));
        // 清除领域的 rh-sso 邮件主题
        statements.add(new UpdateStatement(null, null, database.correctObjectName("REALM", Table.class))
                .addNewColumnValue("EMAIL_THEME", null)
                .setWhereClause("EMAIL_THEME=?")
                .addWhereParameter("rh-sso"));
        // 将旧 account 主题统一为 keycloak.v2
        statements.add(new UpdateStatement(null, null, database.correctObjectName("REALM", Table.class))
                .addNewColumnValue("ACCOUNT_THEME", "keycloak.v2")
                .setWhereClause("ACCOUNT_THEME=? OR ACCOUNT_THEME=? OR ACCOUNT_THEME=?")
                .addWhereParameter("rh-sso")
                .addWhereParameter("rh-sso.v2")
                .addWhereParameter("keycloak"));
        // 删除客户端上指向 rh-sso 的 login_theme 属性（Oracle 需用 DBMS_LOB 比较 CLOB）
        if ("oracle".equals(database.getShortName())) {
            statements.add(new DeleteStatement(null, null, database.correctObjectName("CLIENT_ATTRIBUTES", Table.class))
                    .setWhere("NAME=? AND DBMS_LOB.substr(VALUE,10)=?")
                    .addWhereParameter(DefaultThemeSelectorProvider.LOGIN_THEME_KEY)
                    .addWhereParameter("rh-sso"));
        } else {
            statements.add(new DeleteStatement(null, null, database.correctObjectName("CLIENT_ATTRIBUTES", Table.class))
                    .setWhere("NAME=? AND VALUE=?")
                    .addWhereParameter(DefaultThemeSelectorProvider.LOGIN_THEME_KEY)
                    .addWhereParameter("rh-sso"));
        }
    }

    @Override
    protected String getTaskId() {
        return "Remove RH-SSO themes for keycloak 22.0.0";
    }

}
