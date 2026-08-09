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
 * 响应式 Redisson 事务对象：实现 {@link org.springframework.transaction.support.SmartTransactionObject}，
 * 供 {@link ReactiveRedissonTransactionManager} 在事务生命周期中持有
 * {@link ReactiveRedissonResourceHolder}。
 *
 * @author Nikita Koksharov
 */
public class ReactiveRedissonTransactionObject implements SmartTransactionObject {

    /** 绑定的 Redisson 响应式资源持有者。 */
    private ReactiveRedissonResourceHolder resourceHolder;

    /** 返回当前资源持有者。 */
    public ReactiveRedissonResourceHolder getResourceHolder() {
        return resourceHolder;
    }

    /** 设置或清空资源持有者（挂起/恢复时使用）。 */
    public void setResourceHolder(ReactiveRedissonResourceHolder resourceHolder) {
        this.resourceHolder = resourceHolder;
    }

    /** 若资源持有者存在则读取其 rollback-only 状态。 */
    @Override
    public boolean isRollbackOnly() {
        if (resourceHolder != null) {
            return resourceHolder.isRollbackOnly();
        }
        return false;
    }

    /** Redisson 响应式事务无需 flush，空实现。 */
    @Override
    public void flush() {
        // Redisson 响应式事务不支持 flush，跳过。
    }
}
