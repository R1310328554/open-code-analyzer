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
 * 缓存驱逐策略枚举，用于 {@link org.redisson.api.RMapCache} 等带容量限制的集合。
 *
 * @author Nikita Koksharov
 */
public enum EvictionMode {

        /** 最近最少使用（LRU）驱逐算法。 */
        LRU,

        /** 最不经常使用（LFU）驱逐算法。 */
        LFU,

}
