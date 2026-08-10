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

package org.keycloak.keys;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 公钥存储 SPI，注册 {@link PublicKeyStorageProvider} 提供者类型。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PublicKeyStorageSpi implements Spi {

    /** 内部 SPI，不对扩展模块公开。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** SPI 名称：{@code publicKeyStorage}。 */
    @Override
    public String getName() {
        return "publicKeyStorage";
    }

    /** 公钥存储提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return PublicKeyStorageProvider.class;
    }

    /** 公钥存储工厂类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return PublicKeyStorageProviderFactory.class;
    }
}
