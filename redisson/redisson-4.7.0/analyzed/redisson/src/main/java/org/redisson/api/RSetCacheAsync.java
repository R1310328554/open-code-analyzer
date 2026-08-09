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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis Set 异步 API。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 值类型
 */
public interface RSetCacheAsync<V> extends RSetAsync<V> {

    /**
     * 存储元素并设置存活时间（TTL）；到期后自动过期。
     *
     * @param value 待添加元素
     * @param ttl 键值条目存活时间；{@code 0} 表示永不过期
     * @param unit 时间单位
     * @return 新增成功则为 true；元素已存在则为 false
     */
    RFuture<Boolean> addAsync(V value, long ttl, TimeUnit unit);

    /**
     * 返回缓存中元素数量。
     * 因清理非实时，计数可能包含已过期但未删除的元素。
     *
     * @return 集合大小
     */
    @Override
    RFuture<Integer> sizeAsync();

    /**
     * 请改用 {@link #addIfAbsentAsync(Map)}。
     *
     * @param values 待添加元素
     * @param ttl 元素存活时间；{@code 0} 表示永不过期
     * @param unit 时间单位
     * @return 全部添加成功则为 true，否则 false
     */
    @Deprecated
    RFuture<Boolean> tryAddAsync(long ttl, TimeUnit unit, V... values);

    /**
     * 仅当元素此前不存在时添加到集合。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param ttl 元素 TTL
     * @param object 元素对象
     * @return 操作成功则为 true，否则 false
     */
    RFuture<Boolean> addIfAbsentAsync(Duration ttl, V object);

    /**
     * 仅当元素已存在时更新其 TTL 并保留在集合中。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param ttl 元素 TTL
     * @param object 元素对象
     * @return 操作成功则为 true，否则 false
     */
    RFuture<Boolean> addIfExistsAsync(Duration ttl, V object);

    /**
     * 仅当新 TTL 小于已存在元素当前 TTL 时更新。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param ttl 元素 TTL
     * @param object 元素对象
     * @return 操作成功则为 true，否则 false
     */
    RFuture<Boolean> addIfLessAsync(Duration ttl, V object);

    /**
     * 仅当新 TTL 大于已存在元素当前 TTL 时更新。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param ttl 元素 TTL
     * @param object 元素对象
     * @return 操作成功则为 true，否则 false
     */
    RFuture<Boolean> addIfGreaterAsync(Duration ttl, V object);

    /**
     * 批量添加元素；Map 键为元素、值为对应 TTL。
     *
     * @param objects 元素与 TTL 的映射
     * @return 新增元素数量（不含已存在元素）
     */
    RFuture<Integer> addAllAsync(Map<V, Duration> objects);

    /**
     * 批量添加仅当元素此前均不存在时生效的元素。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param objects 元素与 TTL 的映射
     * @return 新增元素数量
     */
    RFuture<Integer> addAllIfAbsentAsync(Map<V, Duration> objects);
    /**
     * 仅当全部元素此前均不存在时批量添加。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param objects 元素与 TTL 的映射
     * @return 新增元素数量
     */
    RFuture<Boolean> addIfAbsentAsync(Map<V, Duration> objects);

    /**
     * 批量添加仅当元素已存在时更新 TTL 的条目。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param objects 元素与 TTL 的映射
     * @return 新增元素数量
     */
    RFuture<Integer> addAllIfExistAsync(Map<V, Duration> objects);

    /**
     * 批量添加仅当新 TTL 大于已存在元素当前 TTL 时生效。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param objects 元素与 TTL 的映射
     * @return 新增元素数量
     */
    RFuture<Integer> addAllIfGreaterAsync(Map<V, Duration> objects);

    /**
     * 批量添加仅当新 TTL 小于已存在元素当前 TTL 时生效。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param objects 元素与 TTL 的映射
     * @return 新增元素数量
     */
    RFuture<Integer> addAllIfLessAsync(Map<V, Duration> objects);

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.SetAddListener
     * @see org.redisson.api.listener.SetRemoveListener
     * @see org.redisson.api.listener.SetExpiredListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    RFuture<Integer> addListenerAsync(ObjectListener listener);

}
