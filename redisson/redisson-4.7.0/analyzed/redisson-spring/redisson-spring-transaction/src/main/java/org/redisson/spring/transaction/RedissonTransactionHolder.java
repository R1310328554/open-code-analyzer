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

import org.redisson.api.RTransaction;

/**
 * Spring 事务同步上下文中保存 {@link org.redisson.api.RTransaction} 的资源持有者。
 * <p>由 {@link RedissonTransactionManager} 绑定到
 * {@link org.springframework.transaction.support.TransactionSynchronizationManager}，
 * 键为 {@link org.redisson.api.RedissonClient} 实例。
 *
 * @author Nikita Koksharov
 */
public class RedissonTransactionHolder {

    private RTransaction transaction;

    /** 返回当前线程绑定的 Redisson 事务对象。 */
    public RTransaction getTransaction() {
        return transaction;
    }

    /** 设置或清除绑定的 Redisson 事务（事务完成后通常置为 {@code null}）。 */
    public void setTransaction(RTransaction transaction) {
        this.transaction = transaction;
    }
    
}
