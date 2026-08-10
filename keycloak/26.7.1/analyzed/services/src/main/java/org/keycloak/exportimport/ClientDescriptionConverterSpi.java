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

package org.keycloak.exportimport;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 客户端描述转换 SPI，注册 {@link ClientDescriptionConverter} 提供者类型。
 * <p>用于将外部 JSON 客户端描述解析为 {@link org.keycloak.representations.idm.ClientRepresentation}。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientDescriptionConverterSpi implements Spi {

    /** 内部 SPI，不对扩展模块公开。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code client-description-converter} */
    @Override
    public String getName() {
        return "client-description-converter";
    }

    /** @return 客户端描述转换提供者接口类型 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return ClientDescriptionConverter.class;
    }

    /** @return 客户端描述转换工厂类型 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ClientDescriptionConverterFactory.class;
    }

}
