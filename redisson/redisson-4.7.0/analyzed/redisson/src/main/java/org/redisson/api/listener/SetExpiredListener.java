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
package org.redisson.api.listener;

import org.redisson.api.ObjectListener;

/**
 * 监听 {@link org.redisson.api.RSetCache} 中带 TTL 的集合成员<b>过期</b>（expired）键空间事件。
 * <p>
 * 当设置了生存时间的成员被 Redis 淘汰进程移除时触发。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 集合元素类型
 */
@FunctionalInterface
public interface SetExpiredListener<V> extends ObjectListener {

    /**
     * 当 {@link org.redisson.api.RSetCache} 中的成员因 TTL 到期而被移除时触发。
     *
     * @param value 已过期的成员值
     */
    void onExpired(V value);

}
