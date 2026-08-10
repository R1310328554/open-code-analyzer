/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.userprofile;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 用户配置 SPI：定义 {@link UserProfileProvider} 的注册与发现机制。
 *
 * @author <a href="mailto:markus.till@bosch.io">Markus Till</a>
 */
public class UserProfileSpi implements Spi {

    /** SPI 标识符 {@code userProfile}。 */
    public static final String ID = "userProfile";

    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@link #ID} */
    @Override
    public String getName() {
        return ID;
    }

    /** @return 提供者接口 {@link UserProfileProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return UserProfileProvider.class;
    }

    /** @return 工厂接口 {@link UserProfileProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return UserProfileProviderFactory.class;
    }
}
