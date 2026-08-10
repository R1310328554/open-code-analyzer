/*
 * Copyright 2017 Analytical Graphics, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.services.x509;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * X.509 客户端证书查找 SPI 定义。
 * <p>将 {@link X509ClientCertificateLookup} 注册为 Keycloak 可插拔提供者，
 * 供各反向代理（Nginx、HAProxy、Envoy 等）实现证书转发解析。</p>
 *
 * @author <a href="mailto:brat000012001@gmail.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @since 3/26/2017
 */

public class X509ClientCertificateLookupSpi implements Spi {

    /** {@inheritDoc} 非内部 SPI，可在配置中选择具体实现。 */
    @Override
    public boolean isInternal() {
        return false;
    }

    /** {@inheritDoc} SPI 名称：{@code x509cert-lookup}。 */
    @Override
    public String getName() {
        return "x509cert-lookup";
    }

    /** {@inheritDoc} 返回 {@link X509ClientCertificateLookup} 作为提供者类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return X509ClientCertificateLookup.class;
    }

    /** {@inheritDoc} 返回 {@link X509ClientCertificateLookupFactory} 作为工厂类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return X509ClientCertificateLookupFactory.class;
    }
}
