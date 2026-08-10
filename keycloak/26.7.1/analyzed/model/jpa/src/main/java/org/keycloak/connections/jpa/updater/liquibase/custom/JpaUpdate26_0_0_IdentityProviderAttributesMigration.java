/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.OrganizationModel;

import liquibase.exception.CustomChangeException;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Table;

/**
 * Keycloak 26.0.0 身份提供者属性列迁移。
 * <p>将组织 ID、登录页隐藏等配置从 IDENTITY_PROVIDER_CONFIG 表提升到 IDENTITY_PROVIDER 表对应列，并删除旧配置行。</p>
 */
public class JpaUpdate26_0_0_IdentityProviderAttributesMigration extends CustomKeycloakTask {

    @Override
    protected void generateStatementsImpl() throws CustomChangeException {

        // 将组织 ID 从 config 表写入 IDP.ORGANIZATION_ID
        try (PreparedStatement ps = connection.prepareStatement("SELECT c.IDENTITY_PROVIDER_ID, c.VALUE" +
                "  FROM " + getTableName("IDENTITY_PROVIDER_CONFIG") + " c WHERE c.NAME = '" + OrganizationModel.ORGANIZATION_ATTRIBUTE + "'");
             ResultSet resultSet = ps.executeQuery()
        ) {
            while (resultSet.next()) {
                String id = resultSet.getString(1);
                String value = resultSet.getString(2);
                statements.add(new UpdateStatement(null, null, database.correctObjectName("IDENTITY_PROVIDER", Table.class))
                        .addNewColumnValue("ORGANIZATION_ID", value)
                        .setWhereClause("INTERNAL_ID=?")
                        .addWhereParameter(id));
            }
            statements.add(new DeleteStatement(null, null, database.correctObjectName("IDENTITY_PROVIDER_CONFIG", Table.class))
                    .setWhere("NAME=?")
                    .addWhereParameter(OrganizationModel.ORGANIZATION_ATTRIBUTE));

        } catch (Exception e) {
            throw new CustomChangeException(getTaskId() + ": Exception when updating data from previous version", e);
        }

        // 将 hideOnLoginPage 旧配置迁移到 IDP.HIDE_ON_LOGIN
        try (PreparedStatement ps = connection.prepareStatement("SELECT c.IDENTITY_PROVIDER_ID, c.VALUE" +
                "  FROM " + getTableName("IDENTITY_PROVIDER_CONFIG") + " c WHERE c.NAME = '" + IdentityProviderModel.LEGACY_HIDE_ON_LOGIN_ATTR + "'");
             ResultSet resultSet = ps.executeQuery()
        ) {
            while (resultSet.next()) {
                String id = resultSet.getString(1);
                String value = resultSet.getString(2);
                statements.add(new UpdateStatement(null, null, database.correctObjectName("IDENTITY_PROVIDER", Table.class))
                        .addNewColumnValue("HIDE_ON_LOGIN", Boolean.parseBoolean(value))
                        .setWhereClause("INTERNAL_ID=?")
                        .addWhereParameter(id));
            }
            statements.add(new DeleteStatement(null, null, database.correctObjectName("IDENTITY_PROVIDER_CONFIG", Table.class))
                    .setWhere("NAME=?")
                    .addWhereParameter(IdentityProviderModel.LEGACY_HIDE_ON_LOGIN_ATTR));

        } catch (Exception e) {
            throw new CustomChangeException(getTaskId() + ": Exception when updating data from previous version", e);
        }

        // kc.org.broker.public 与 HIDE_ON_LOGIN 语义相反：public=true 表示登录页可见
        try (PreparedStatement ps = connection.prepareStatement("SELECT c.IDENTITY_PROVIDER_ID, c.VALUE" +
                "  FROM " + getTableName("IDENTITY_PROVIDER_CONFIG") + " c WHERE c.NAME = 'kc.org.broker.public'");
             ResultSet resultSet = ps.executeQuery()
        ) {
            while (resultSet.next()) {
                String id = resultSet.getString(1);
                String value = resultSet.getString(2);
                statements.add(new UpdateStatement(null, null, database.correctObjectName("IDENTITY_PROVIDER", Table.class))
                        .addNewColumnValue("HIDE_ON_LOGIN", !Boolean.parseBoolean(value))
                        .setWhereClause("INTERNAL_ID=?")
                        .addWhereParameter(id));
            }
            statements.add(new DeleteStatement(null, null, database.correctObjectName("IDENTITY_PROVIDER_CONFIG", Table.class))
                    .setWhere("NAME=?")
                    .addWhereParameter("kc.org.broker.public"));

        } catch (Exception e) {
            throw new CustomChangeException(getTaskId() + ": Exception when updating data from previous version", e);
        }
    }

    @Override
    protected String getTaskId() {
        return "Migrate kc.org and hideOnLoginPage from the IDP config to the IDP itself";
    }
}
