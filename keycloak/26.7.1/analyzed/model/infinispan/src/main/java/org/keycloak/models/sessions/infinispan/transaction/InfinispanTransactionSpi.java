/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.transaction;

import org.keycloak.provider.Spi;

/**
 * Infinispan 会话事务 SPI 的 {@link Spi} 实现。
 * <p>
 * 注册 {@link InfinispanTransactionProvider} 及其工厂，供 Keycloak 在 JTA 事务边界内
 * 协调非阻塞 Infinispan 缓存操作与数据库写入。
 */
public class InfinispanTransactionSpi implements Spi {

    /** SPI 标识符，对应服务加载时的名称。 */
    private static final String ID = "infinispanTransactions";

    @Override
    public boolean isInternal() {
        // 内部 SPI，不对外暴露给第三方扩展
        return true;
    }

    @Override
    public String getName() {
        return ID;
    }

    @Override
    public Class<InfinispanTransactionProvider> getProviderClass() {
        return InfinispanTransactionProvider.class;
    }

    @Override
    public Class<InfinispanTransactionProviderFactory> getProviderFactoryClass() {
        return InfinispanTransactionProviderFactory.class;
    }
}
