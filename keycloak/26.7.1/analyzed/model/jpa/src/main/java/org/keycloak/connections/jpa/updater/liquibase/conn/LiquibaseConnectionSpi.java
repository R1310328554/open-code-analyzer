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

package org.keycloak.connections.jpa.updater.liquibase.conn;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * Liquibase 连接 SPI 定义，将 {@link LiquibaseConnectionProvider} 注册到 Keycloak 提供者体系。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LiquibaseConnectionSpi implements Spi {

    /** 内部 SPI，不对外暴露给管理员配置。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "connectionsLiquibase";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return LiquibaseConnectionProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return LiquibaseConnectionProviderFactory.class;
    }
}
