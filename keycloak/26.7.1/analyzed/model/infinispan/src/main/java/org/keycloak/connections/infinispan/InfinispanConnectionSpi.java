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

package org.keycloak.connections.infinispan;

import org.keycloak.provider.Spi;

/**
 * Infinispan 连接 SPI，向 Keycloak 服务加载器注册 {@link InfinispanConnectionProvider} 及其实现工厂。
 * <p>
 * 该 SPI 为内部组件，负责在运行时提供嵌入式或远程 Infinispan 缓存管理器访问能力。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class InfinispanConnectionSpi implements Spi {

    /** SPI 在 Keycloak 中的注册名称。 */
    public static final String SPI_NAME = "connectionsInfinispan";

    /** {@inheritDoc} 标记为内部 SPI，不对外暴露给第三方扩展。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** {@inheritDoc} 返回 {@link #SPI_NAME}。 */
    @Override
    public String getName() {
        return SPI_NAME;
    }

    /** {@inheritDoc} 返回 Infinispan 连接提供者接口类型。 */
    @Override
    public Class<InfinispanConnectionProvider> getProviderClass() {
        return InfinispanConnectionProvider.class;
    }

    /** {@inheritDoc} 返回 Infinispan 连接提供者工厂接口类型。 */
    @Override
    public Class<InfinispanConnectionProviderFactory> getProviderFactoryClass() {
        return InfinispanConnectionProviderFactory.class;
    }

}
