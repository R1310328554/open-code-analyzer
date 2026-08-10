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

package org.keycloak.models;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 领域 SPI，注册 {@link RealmProvider} 提供者类型。
 * <p>管理 Keycloak 领域、客户端、用户等核心模型数据的 CRUD。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class RealmSpi implements Spi {

    /** SPI 注册名称常量。 */
    public static final String NAME = "realm";

    /** 内部 SPI，不对扩展模块公开。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** SPI 名称：{@code realm}。 */
    @Override
    public String getName() {
        return NAME;
    }

    /** 领域提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return RealmProvider.class;
    }

    /** 领域工厂类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return RealmProviderFactory.class;
    }

}
