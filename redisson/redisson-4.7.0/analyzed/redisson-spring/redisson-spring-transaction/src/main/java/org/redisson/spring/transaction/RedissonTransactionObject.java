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

import org.springframework.transaction.support.SmartTransactionObject;

/**
 * {@link RedissonTransactionManager} 使用的 {@link org.springframework.transaction.support.SmartTransactionObject}。
 * <p>封装 {@link RedissonTransactionHolder} 与 rollback-only 标志，供 Spring 事务模板驱动 commit/rollback。
 *
 * @author Nikita Koksharov
 */
public class RedissonTransactionObject implements SmartTransactionObject {

    private boolean isRollbackOnly;
    private RedissonTransactionHolder transactionHolder;

    /** 返回关联的资源持有者；新事务开始时可能为 {@code null}。 */
    public RedissonTransactionHolder getTransactionHolder() {
        return transactionHolder;
    }

    /** 绑定或清除事务持有者（挂起时置 {@code null}）。 */
    public void setTransactionHolder(RedissonTransactionHolder transaction) {
        this.transactionHolder = transaction;
    }

    /** 设置 rollback-only 标志。 */
    public void setRollbackOnly(boolean isRollbackOnly) {
        this.isRollbackOnly = isRollbackOnly;
    }
    
    /** 是否已标记为仅回滚。 */
    @Override
    public boolean isRollbackOnly() {
        return isRollbackOnly;
    }

    /** Redisson 事务无 Hibernate 式 flush；此处为空实现。 */
    @Override
    public void flush() {
        // skip
    }

}
