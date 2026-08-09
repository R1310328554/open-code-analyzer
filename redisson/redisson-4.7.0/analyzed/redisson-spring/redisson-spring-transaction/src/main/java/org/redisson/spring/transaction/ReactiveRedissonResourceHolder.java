/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.spring.transaction;

import org.redisson.api.RTransactionReactive;
import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * 响应式 Redisson 事务资源持有者：在 {@link org.springframework.transaction.reactive.TransactionSynchronizationManager}
 * 中以 {@link org.redisson.api.RedissonReactiveClient} 为键绑定 {@link org.redisson.api.RTransactionReactive}。
 * <p>继承 {@link org.springframework.transaction.support.ResourceHolderSupport} 以支持 rollback-only 标记。
 *
 * @author Nikita Koksharov
 *
 */
public class ReactiveRedissonResourceHolder extends ResourceHolderSupport {

    /** 当前 Reactor 上下文绑定的 Redisson 响应式事务。 */
    private RTransactionReactive transaction;

    /** 返回绑定的 {@link RTransactionReactive}。 */
    public RTransactionReactive getTransaction() {
        return transaction;
    }

    /** 设置或清空绑定的 {@link RTransactionReactive}（完成清理时使用）。 */
    public void setTransaction(RTransactionReactive transaction) {
        this.transaction = transaction;
    }

}
