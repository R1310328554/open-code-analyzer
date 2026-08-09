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
package org.redisson.api.options;

import org.redisson.config.DelayStrategy;
import org.redisson.config.ReadMode;

/**
 * Redisson 分布式对象通用运行时参数的只读视图（超时、重试、读取模式等）。
 *
 * @author Nikita Koksharov
 *
 */
public interface ObjectParams {

    /** @return 命令执行超时时间（毫秒） */
    int getTimeout();

    /** @return 失败后的重试次数 */
    int getRetryAttempts();

    /** @return 重试间隔策略 */
    DelayStrategy getRetryDelay();

    /** @return 集群/主从场景下的读取模式 */
    ReadMode getReadMode();

}
