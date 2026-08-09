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
 * 固定延迟策略：每次重试返回相同的等待时长。
 * <p>
 * 适用于需要稳定、可预测重试间隔的场景。
 *
 * @author Nikita Koksharov
 *
 */
public class ConstantDelay implements DelayStrategy {

    /** 固定的重试间隔时长。 */
    private final Duration delay;

    /**
     * 创建固定延迟策略。
     *
     * @param delay 每次重试之间的固定等待时长
     */
    public ConstantDelay(Duration delay) {
        this.delay = delay;
    }

    /** 忽略重试次数，始终返回固定延迟。 */
    @Override
    public Duration calcDelay(int attempt) {
        return delay;
    }
}
