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
package org.redisson.config;

import java.time.Duration;

/**
 * 重试间隔计算策略接口，用于连接失败、命令重试等场景的退避控制。
 * <p>
 * 常见实现：{@link ConstantDelay}、{@link EqualJitterDelay}、
 * {@link FullJitterDelay}、{@link DecorrelatedJitterDelay}。
 *
 * @author Nikita Koksharov
 *
 */
public interface DelayStrategy {

    /**
     * 计算下次重试前应等待的时长。
     *
     * @param attempt 从 0 开始的当前重试次数（0 表示第一次重试）
     * @return 下次重试前的等待时长
     */
    Duration calcDelay(int attempt);

}
