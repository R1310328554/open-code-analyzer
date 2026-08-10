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
package org.keycloak.protocol.saml.preprocessor;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * {@link SamlAuthenticationPreprocessor} 的 SPI 定义：注册 SAML 认证预处理器扩展点。
 * 
 * @author <a href="mailto:gideon.caranzo@thalesgroup.com">Gideon Caranzo</a>
 *
 */
public class SamlAuthenticationPreprocessorSpi implements Spi {

    /** {@inheritDoc} 非内部 SPI，允许第三方实现 */
    @Override
    public boolean isInternal() {
        return false;
    }

    /** {@inheritDoc} SPI 名称：saml-authentication-preprocessor */
    @Override
    public String getName() {
        return "saml-authentication-preprocessor";
    }

    /** {@inheritDoc} 提供者接口类 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return SamlAuthenticationPreprocessor.class;
    }

    /** {@inheritDoc} 提供者工厂类（与 Provider 接口相同） */
    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return SamlAuthenticationPreprocessor.class;
    }

}
