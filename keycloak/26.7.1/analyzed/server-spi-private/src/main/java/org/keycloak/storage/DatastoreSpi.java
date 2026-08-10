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

import org.keycloak.provider.Spi;

/**
 * 数据存储 SPI：注册 {@link DatastoreProvider} 与 {@link DatastoreProviderFactory}。
 */
public class DatastoreSpi implements Spi {

    /** @return 内部 SPI，不对外暴露 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code datastore} */
    @Override
    public String getName() {
        return "datastore";
    }

    /** @return 提供者接口 {@link DatastoreProvider} */
    @Override
    public Class<DatastoreProvider> getProviderClass() {
        return DatastoreProvider.class;
    }

    /** @return 工厂接口 {@link DatastoreProviderFactory} */
    @Override
    public Class<DatastoreProviderFactory> getProviderFactoryClass() {
        return DatastoreProviderFactory.class;
    }
    
}
