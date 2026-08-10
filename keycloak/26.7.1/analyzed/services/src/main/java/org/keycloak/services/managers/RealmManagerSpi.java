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

package org.keycloak.services.managers;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;
import org.keycloak.storage.ImportRealmFromRepresentationEvent;

/**
 * 领域管理 SPI，注册 {@link RealmManagerProviderFactory}。
 * <p>用于监听 {@link ImportRealmFromRepresentationEvent} 等领域事件；迁移完成后可移除。</p>
 *
 * @author Alexander Schwartz
 */
@Deprecated
public class RealmManagerSpi implements Spi {
    /** {@inheritDoc} 内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** {@inheritDoc} 返回 {@code realm-manager} */
    @Override
    public String getName() {
        return "realm-manager";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return RealmManagerProviderFactory.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return RealmManagerProviderFactory.class;
    }
}
