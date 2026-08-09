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
package org.redisson.executor;

import org.redisson.executor.params.ScheduledAtFixedRateParameters;
import org.redisson.executor.params.ScheduledCronExpressionParameters;
import org.redisson.executor.params.ScheduledParameters;
import org.redisson.executor.params.ScheduledWithFixedDelayParameters;
import org.redisson.executor.params.TaskParameters;

/**
 * 远程执行器 Worker 端服务接口，由 {@link TasksRunnerService} 实现。
 * <p>
 * 定义 Callable/Runnable 即时执行与多种调度模式（一次性、固定速率、固定延迟、Cron）。
 * 对应客户端 {@link RemoteExecutorServiceAsync} 的同步 RPC 端点。
 *
 * @author Nikita Koksharov
 *
 */
public interface RemoteExecutorService {

    /** 同步执行 Callable 任务并返回结果。 */
    Object executeCallable(TaskParameters params);
 
    /** 同步执行 Runnable 任务。 */
    void executeRunnable(TaskParameters params);
    
    /** 调度 Callable 在指定延迟后执行。 */
    Object scheduleCallable(ScheduledParameters params);
    
    /** 调度 Runnable 在指定延迟后执行。 */
    void scheduleRunnable(ScheduledParameters params);
    
    /** 以固定速率周期性调度任务。 */
    void scheduleAtFixedRate(ScheduledAtFixedRateParameters params);
    
    /** 以固定延迟（上次完成后）周期性调度任务。 */
    void scheduleWithFixedDelay(ScheduledWithFixedDelayParameters params);

    /** 按 Cron 表达式调度任务。 */
    void schedule(ScheduledCronExpressionParameters params);
    
}
