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

import org.keycloak.models.KeycloakTransaction;
import org.keycloak.provider.Provider;

/**
 * Infinispan 缓存操作的 Provider，其提交/回滚纳入 {@link KeycloakTransaction} 生命周期。
 * <p>
 * 利用 Infinispan 非阻塞/异步 API 并发发起缓存操作，在事务末尾统一等待全部完成。
 */
public interface InfinispanTransactionProvider extends Provider {

    /**
     * 注册一条 {@link NonBlockingTransaction}。
     *
     * @param transaction {@link NonBlockingTransaction} 实例
     */
    void registerTransaction(NonBlockingTransaction transaction);

}
