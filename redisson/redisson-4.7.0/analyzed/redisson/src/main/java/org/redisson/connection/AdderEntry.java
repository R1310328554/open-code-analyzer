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
package org.redisson.connection;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 聚合计数条目，用于跟踪一组 ID 及其并发使用次数。
 * <p>
 * 内部使用线程安全的 {@link ConcurrentHashMap} 集合与 {@link AtomicInteger} 计数器。
 *
 * @author Nikita Koksharov
 *
 */
public class AdderEntry {

    /** 关联的 ID 集合（线程安全）。 */
    private final Set<String> ids = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** 当前使用计数。 */
    private final AtomicInteger usage = new AtomicInteger();

    /** 返回关联 ID 集合。 */
    public Set<String> getIds() {
        return ids;
    }

    /** 返回使用计数器。 */
    public AtomicInteger getUsage() {
        return usage;
    }

}
