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

/**
 * 定义队列同步参数的接口；
 * 控制队列操作如何与 Valkey 或 Redis 从节点同步。
 *
 * @param <T> 参数对象类型
 * @author Nikita Koksharov
 */
public interface SyncArgs<T> {

    /**
     * 设置当前操作使用的同步模式。
     * <p>
     * Default value is SyncMode.AUTO
     *
     * @param syncMode 同步模式
     * @return 参数对象
     * @see SyncMode
     */
    T syncMode(SyncMode syncMode);

    /**
     * 设置与从节点同步失败时的处理方式。
     * <p>
     * Default value is SyncFailureMode.LOG_WARNING
     *
     * @param syncFailureMode 失败处理模式
     * @return 当前实例，支持链式调用
     * @see SyncFailureMode
     */
    T syncFailureMode(SyncFailureMode syncFailureMode);

    /**
     * 设置当前操作的同步超时时间；
     * 即等待从节点确认的最长时间。
     * <p>
     * Default value is 1 second.
     *
     * @param timeout 等待同步完成的最长时间
     * @return 参数对象
     */
    T syncTimeout(Duration timeout);

}
