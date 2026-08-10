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

package org.keycloak.connections.jpa.updater.liquibase.custom;

import java.util.function.Predicate;

import org.keycloak.models.LDAPConstants;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.storage.UserStorageProvider;

import liquibase.exception.CustomChangeException;

/**
 * 2.5.0 迁移：将非 LDAP 用户联邦存储迁移为 Component 模型。
 * <p>遍历已注册的 {@link UserStorageProvider} 工厂（排除 LDAP），逐个调用 {@link AbstractUserFedToComponent} 转换逻辑。</p>
 *
 * @author <a href="mailto:bburke@redhat.com">Bill Burke</a>
 */
public class MigrateUserFedToComponent extends AbstractUserFedToComponent {

    /** 除 LDAP 外所有 UserStorageProvider 实现均转为 COMPONENT 配置。 */
    @Override
    protected void generateStatementsImpl() {
        kcSession.getKeycloakSessionFactory().getProviderFactoriesStream(UserStorageProvider.class)
                .map(ProviderFactory::getId)
                .filter(Predicate.isEqual(LDAPConstants.LDAP_PROVIDER).negate())
                .forEach(this::convertFedProviderToComponent);
    }

    @Override
    protected String getTaskId() {
        return "Update 2.5.0.Final";
    }

    /** 包装 CustomChangeException 为 RuntimeException 以适配 Stream forEach。 */
    private void convertFedProviderToComponent(String id) {
        try {
            convertFedProviderToComponent(id, null);
        } catch (CustomChangeException ex) {
            throw new RuntimeException(ex);
        }
    }
}
