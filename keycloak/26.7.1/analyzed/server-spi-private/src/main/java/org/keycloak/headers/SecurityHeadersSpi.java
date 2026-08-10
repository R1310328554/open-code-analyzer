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
package org.keycloak.headers;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 安全响应头 SPI，注册 {@link SecurityHeadersProvider} 提供者类型。
 * <p>在 REST 响应写出前统一注入防 XSS、点击劫持等 HTTP 安全头。</p>
 */
public class SecurityHeadersSpi implements Spi {

    /** 内部 SPI，不对扩展模块公开。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** SPI 名称：{@code security-headers}。 */
    @Override
    public String getName() {
        return "security-headers";
    }

    /** 安全头提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return SecurityHeadersProvider.class;
    }

    /** 安全头工厂类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return SecurityHeadersProviderFactory.class;
    }

}
