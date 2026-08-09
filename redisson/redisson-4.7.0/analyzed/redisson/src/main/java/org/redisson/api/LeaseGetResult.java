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
package org.redisson.api;

/**
 * {@link RLeasedMap#getWithLease(Object, java.time.Duration)} 的查询结果。
 * <p>
 * 缓存命中时 {@link #getValue()} 返回值且 {@link #getLeaseToken()} 为 {@code null}；
 * 未命中时 {@link #getValue()} 为 {@code null}，{@link #getLeaseToken()} 返回租约令牌
（无租约信息时亦为 {@code null}）。
 *
 * @author nhancdt2602
 * @param <V> 值类型
 */
public final class LeaseGetResult<V> {

    private final V value;
    private final boolean leaseAcquired;
    private final String leaseToken;

    /** @param value 缓存值；未命中为 {@code null}
     *  @param leaseAcquired 是否在未命中时成功获取租约
     *  @param leaseToken 租约令牌；命中或无租约时为 {@code null} */
    public LeaseGetResult(V value, boolean leaseAcquired, String leaseToken) {
        this.value = value;
        this.leaseAcquired = leaseAcquired;
        this.leaseToken = leaseToken;
    }

    /** @return 缓存值；未命中时 {@code null} */

    public V getValue() {
        return value;
    }

    /** @return 缓存未命中（{@link #getValue()} 为 {@code null}）时为 {@code true} */

    public boolean isCacheMiss() {
        return value == null;
    }

    /** @return 未命中且成功获取租约时为 {@code true}；命中时恒为 {@code false} */

    public boolean isLeaseAcquired() {
        return leaseAcquired;
    }

    /** @return 未命中时的租约令牌；命中或无租约信息时为 {@code null} */

    public String getLeaseToken() {
        return leaseToken;
    }
}
