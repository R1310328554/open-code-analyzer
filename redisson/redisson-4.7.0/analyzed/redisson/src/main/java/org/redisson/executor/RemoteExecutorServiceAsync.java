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

import org.redisson.api.RFuture;
import org.redisson.api.annotation.RRemoteAsync;
import org.redisson.executor.params.ScheduledAtFixedRateParameters;
import org.redisson.executor.params.ScheduledCronExpressionParameters;
import org.redisson.executor.params.ScheduledParameters;
import org.redisson.executor.params.ScheduledWithFixedDelayParameters;
import org.redisson.executor.params.TaskParameters;

/**
 * {@link RemoteExecutorService} 的异步远程调用接口。
 * <p>
 * 由 {@link org.redisson.api.annotation.RRemoteAsync} 标注，
 * 客户端通过 {@link org.redisson.remote.BaseRemoteService} 代理调用，
 * 各方法返回 {@link org.redisson.api.RFuture} 而非阻塞等待结果。
 *
 * @author Nikita Koksharov
 *
 */
@RRemoteAsync(RemoteExecutorService.class)
public interface RemoteExecutorServiceAsync {

    /** 异步提交 Callable 任务并返回带结果的 Future。 */
    <T> RFuture<T> executeCallable(TaskParameters params);
    
    /** 异步提交 Runnable 任务，无返回值。 */
    RFuture<Void> executeRunnable(TaskParameters params);
    
    /** 异步调度 Callable，在 {@link ScheduledParameters#getStartTime()} 触发。 */
    <T> RFuture<T> scheduleCallable(ScheduledParameters params);
    
    /** 异步调度 Runnable，在指定开始时间执行。 */
    RFuture<Void> scheduleRunnable(ScheduledParameters params);
    
    /** 异步注册固定频率（fixed-rate）周期任务。 */
    RFuture<Void> scheduleAtFixedRate(ScheduledAtFixedRateParameters params);
    
    /** 异步注册固定延迟（fixed-delay）周期任务。 */
    RFuture<Void> scheduleWithFixedDelay(ScheduledWithFixedDelayParameters params);

    /** 异步按 Cron 表达式注册周期任务。 */
    RFuture<Void> schedule(ScheduledCronExpressionParameters params);
    
}
