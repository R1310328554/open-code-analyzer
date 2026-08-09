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

import java.util.concurrent.TimeUnit;

/**
 * {@link org.redisson.api.RExecutorService} 的可选配置。
 * <p>
 * 控制任务重试间隔与任务标识生成策略。
 *
 * @author Nikita Koksharov
 */
@Deprecated
public final class ExecutorOptions {
    
    private long taskRetryInterval = 5 * 60000;

    private IdGenerator idGenerator = IdGenerator.random();

    private ExecutorOptions() {
    }
    
    /** @return 使用默认参数的 {@link ExecutorOptions} 实例 */
    public static ExecutorOptions defaults() {
        return new ExecutorOptions();
    }
    
    /** @return 任务重试间隔（毫秒） */
    public long getTaskRetryInterval() {
        return taskRetryInterval;
    }
    
    /**
     * 设置任务重试间隔：自任务开始起若仍未标记完成（成功或失败），
     * 则 Worker 在该间隔后重新执行。
     * <p>
     * 设为 {@code 0} 禁用重试；默认 {@code 5} 分钟。
     *
     * @param timeout 间隔数值
     * @param unit 时间单位
     * @return 当前实例（链式调用）
     */
    public ExecutorOptions taskRetryInterval(long timeout, TimeUnit unit) {
        this.taskRetryInterval = unit.toMillis(timeout);
        return this;
    }

    /** @return 任务标识生成器 */
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    /**
     * 设置任务标识生成器。
     *
     * @param idGenerator 标识生成器实现
     * @return 当前实例（链式调用）
     */
    public ExecutorOptions idGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        return this;
    }

}
