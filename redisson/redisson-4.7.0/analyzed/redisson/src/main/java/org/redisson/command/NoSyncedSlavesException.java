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
package org.redisson.command;

/**
 * 批量执行后同步从库数量不足时抛出的运行时异常。
 * <p>当 {@link org.redisson.api.BatchOptions} 配置了 {@code syncSlaves} 且
 * WAIT/WAITAOF 返回的已同步从库数低于预期时触发。
 *
 * @author Nikita Koksharov
 *
 */
public class NoSyncedSlavesException extends RuntimeException {

    /** @param message 描述未满足同步从库数量的详情 */
    public NoSyncedSlavesException(String message) {
        super(message);
    }

}
