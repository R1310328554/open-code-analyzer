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
package org.redisson.cache;

import org.redisson.misc.WrappedLock;

/**
 * 缓存条目接口，扩展 {@link ExpirableValue}。
 * <p>
 * 提供键、值访问及条目级锁。
 */
public interface CachedValue<K, V> extends ExpirableValue {

    /** 返回条目键。 */
    K getKey();

    /** 返回条目值。 */
    V getValue();

    /** 返回条目级互斥锁。 */
    WrappedLock getLock();
}
