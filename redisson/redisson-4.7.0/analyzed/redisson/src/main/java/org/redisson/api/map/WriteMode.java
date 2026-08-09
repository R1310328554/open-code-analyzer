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
package org.redisson.api.map;

/**
 * Map 与 {@link MapWriter} 协同写入的模式。
 *
 * @author Nikita Koksharov
 *
 */
public enum WriteMode {

    /**
     * 写后（Write-Behind）模式：Map 上的写入先落 Redis，
     * 再通过 {@link MapWriter} 异步刷到外部存储。
     */
    WRITE_BEHIND,

    /**
     * 写穿（Write-Through）模式：Map 写操作与 {@link MapWriter} 同步执行。
     * 若 {@link MapWriter} 抛出异常，将原样传递给 Map 调用方。
     */
    WRITE_THROUGH

}
