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
package org.keycloak.models;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 身份提供者存储 SPI，注册 {@link IdentityProviderStorageProvider} 提供者类型。
 * <p>管理领域级 IdP 别名、配置与元数据的持久化访问。</p>
 */
public class IdentityProviderStorageSpi implements Spi {

    /** SPI 注册名称常量。 */
    public static final String NAME = "identity-provider-storage";

    /** IdP 存储工厂类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return IdentityProviderStorageProviderFactory.class;
    }

    /** IdP 存储提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return IdentityProviderStorageProvider.class;
    }

    /** SPI 名称：{@code identity-provider-storage}。 */
    @Override
    public String getName() {
        return NAME;
    }

    /** 内部 SPI，不对扩展模块公开。 */
    @Override
    public boolean isInternal() {
        return true;
    }
}
