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

package org.keycloak.testsuite.domainextension.spi;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 域扩展示例 SPI 定义，将 {@link ExampleService} 注册为 Keycloak 可扩展服务。
 */
public class ExampleSpi implements Spi {

    /** {@inheritDoc} 示例 SPI 对外公开，非内部专用。 */
    @Override
    public boolean isInternal() {
        return false;
    }

    /** {@inheritDoc} 返回 SPI 名称 {@code example}。 */
    @Override
    public String getName() {
        return "example";
    }

    /** {@inheritDoc} 返回关联的 {@link ExampleService} 提供者类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return ExampleService.class;
    }

    /** {@inheritDoc} 返回 {@link ExampleServiceProviderFactory} 工厂类型。 */
    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ExampleServiceProviderFactory.class;
    }

}
