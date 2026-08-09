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
 * 定义与 Valkey 或 Redis 从节点同步失败时的行为模式。
 *
 * @author Nikita Koksharov
 *
 */
public enum SyncFailureMode {

    /**
     * 同步失败时向调用方抛出异常。
     * <p>
     * 适用于需要应用代码立即感知并处理同步失败的场景。
     * </p>
     */
    THROW_EXCEPTION,

    /**
     * 同步失败时记录警告日志并继续执行。
     * <p>
     * 适用于非关键同步操作，即使同步异常应用仍可继续运行。
     * </p>
     */
    LOG_WARNING

}
