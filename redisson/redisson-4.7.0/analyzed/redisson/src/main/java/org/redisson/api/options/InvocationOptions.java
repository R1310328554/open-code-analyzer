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

import java.time.Duration;

/**
 * Redis 命令调用的通用选项，涵盖响应超时与重试策略。
 *
 * @author Nikita Koksharov
 *
 */
public interface InvocationOptions<T extends InvocationOptions<T>> {

    /**
     * 设置 Redis 服务器响应超时。自命令成功发送后开始计时。
     * <p>
     * 默认取 Redisson 全局配置中的同名参数
     *
     * @param timeout Redis 响应超时
     * @return 选项实例
     */
    T timeout(Duration timeout);

    /**
     * 设置命令发送重试次数。若在 <code>retryAttempts</code> 次内仍无法将命令
     * 发送至 Redis 服务器则抛出异常；发送成功后则启动响应超时计时。
     * <p>
     * 默认取 Redisson 全局配置中的同名参数
     *
     * @param retryAttempts 命令重试次数
     * @return 选项实例
     */
    T retryAttempts(int retryAttempts);

    /**
     * 请改用 {@link #retryDelay(DelayStrategy)}。
     *
     * @param interval 重试时间间隔
     * @return 选项实例
     */
    @Deprecated
    T retryInterval(Duration interval);

    /**
     * 设置再次发送命令前的延迟策略。
     *
     * @param delayStrategy 延迟策略实现
     * @return 选项实例
     */
    T retryDelay(DelayStrategy delayStrategy);

}
