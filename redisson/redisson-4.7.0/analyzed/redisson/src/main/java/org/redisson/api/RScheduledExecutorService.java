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
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的分布式 {@link java.util.concurrent.ScheduledExecutorService} 实现。
 * <p>任务序列化后存入 Redis，由 Worker 节点按调度时间执行；
 * 支持延迟、固定速率、固定延迟及 Cron 表达式。
 * 
 * @author Nikita Koksharov
 *
 */
public interface RScheduledExecutorService extends RExecutorService, ScheduledExecutorService, RScheduledExecutorServiceAsync {

    /**
     * 在指定延迟后调度 {@link Runnable} 任务执行，返回 {@link RScheduledFuture}。
     *
     * @param command the task to execute
     * @param delay the time from now to delay execution
     * @param unit the time unit of the delay parameter
     * @return 表示任务待完成状态的 {@link RScheduledFuture}，
     *         完成后 {@code get()} 返回 {@code null}
     */
    @Override
    RScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

    /**
     * Synchronously schedules a Runnable task
     * with defined <code>id</code> for execution asynchronously
     * after the given <code>delay</code>.
     * Returns a RScheduledFuture representing that task.
     *
     * @param id task id
     * @param task the task to execute
     * @param delay the time from now to delay execution
     * @return a ScheduledFuture representing pending completion of
     *         the task and whose {@code get()} method will return
     *         {@code null} upon completion
     */
    RScheduledFuture<?> schedule(String id, Runnable task, Duration delay);

    /**
     * Synchronously schedules a Runnable task with defined <code>timeToLive</code> parameter
     * for execution asynchronously after the given <code>delay</code>.
     * Returns a RScheduledFuture representing that task.
     * The Future's {@code get} method will return the given result upon successful completion.
     *
     * @param task the task to execute
     * @param delay the time from now to delay execution
     * @param unit the time unit of the delay parameter
     * @param timeToLive time to live interval
     * @param ttlUnit unit of time to live interval
     * @return a ScheduledFuture representing pending completion of
     *         the task and whose {@code get()} method will return
     *         {@code null} upon completion
     */
    RScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit, long timeToLive, TimeUnit ttlUnit);

    /**
     * Synchronously schedules a Runnable task with
     * defined <code>id</code> and <code>timeToLive</code> parameters
     * for execution asynchronously after the given <code>delay</code>.
     * Returns a RScheduledFuture representing that task.
     *
     * @param id task id
     * @param task the task to execute
     * @param delay the time from now to delay execution
     * @param timeToLive time to live interval
     * @return a ScheduledFuture representing pending completion of
     *         the task and whose {@code get()} method will return
     *         {@code null} upon completion
     */
    RScheduledFuture<?> schedule(String id, Runnable task, Duration delay, Duration timeToLive);

    /**
     * Synchronously schedules a value-returning task for execution asynchronously
     * after the given <code>delay</code>. Returns a RScheduledFuture representing that task.
     * The Future's {@code get} method will return the given result upon successful completion.
     *
     * @param callable the function to execute
     * @param delay the time from now to delay execution
     * @param unit the time unit of the delay parameter
     * @param <V> the type of the callable's result
     * @return a ScheduledFuture that can be used to extract result or cancel
     */
    @Override
    <V> RScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

    /**
     * Synchronously schedules a value-returning task with defined <code>id</code> for execution asynchronously
     * after the given <code>delay</code>. Returns a RScheduledFuture representing that task.
     *
     * @param id task id
     * @param callable the function to execute
     * @param delay the time from now to delay execution
     * @param <V> the type of the callable's result
     * @return a ScheduledFuture that can be used to extract result or cancel
     */
    <V> RScheduledFuture<V> schedule(String id, Callable<V> callable, Duration delay);

    /**
     * Synchronously schedules a value-returning task with defined <code>timeToLive</code> parameter
     * for execution asynchronously after the given <code>delay</code>.
     * Returns a RScheduledFuture representing that task.
     * The Future's {@code get} method will return the given result upon successful completion.
     *
     * @param callable the function to execute
     * @param delay the time from now to delay execution
     * @param unit the time unit of the delay parameter
     * @param timeToLive time to live interval
     * @param ttlUnit unit of time to live interval
     * @param <V> the type of the callable's result
     * @return a ScheduledFuture that can be used to extract result or cancel
     */
    <V> RScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit, long timeToLive, TimeUnit ttlUnit);

    /**
     * Synchronously schedules a value-returning task
     * with defined <code>id</code> and <code>timeToLive</code> parameters
     * for execution asynchronously after the given <code>delay</code>.
     * Returns a RScheduledFuture representing that task.
     *
     * @param id task id
     * @param callable the function to execute
     * @param delay the time from now to delay execution
     * @param timeToLive time to live interval
     * @param <V> the type of the callable's result
     * @return a ScheduledFuture that can be used to extract result or cancel
     */
    <V> RScheduledFuture<V> schedule(String id, Callable<V> callable, Duration delay, Duration timeToLive);

    /**
     * 以固定速率周期性调度 {@link Runnable} 任务（首次延迟 {@code initialDelay}，
     * 之后每隔 {@code period} 执行一次）。
     * 任一次执行抛异常则停止后续调度；也可通过取消或关闭执行器终止。
     *
     * @param task the task to execute
     * @param initialDelay the time to delay first execution
     * @param period the period between successive executions
     * @param unit the time unit of the initialDelay and period parameters
     * @return 表示任务待完成状态的 {@link RScheduledFuture}
     */
    @Override
    RScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);

    /**
     * Synchronously schedules a Runnable task with defined <code>id </code>
     * for execution asynchronously after the given <code>initialDelay</code>,
     * and subsequently with the given <code>period</code>.
     * Subsequent executions are stopped if any execution of the task throws an exception.
     * Otherwise, task could be terminated via cancellation or
     * termination of the executor.
     *
     * @param id task id
     * @param task the task to execute
     * @param initialDelay the time to delay first execution
     * @param period the period between successive executions
     * @return a ScheduledFuture representing pending completion of
     *         the task, and whose {@code get()} method will throw an
     *         exception upon cancellation
     */
    RScheduledFuture<?> scheduleAtFixedRate(String id, Runnable task, Duration initialDelay, Duration period);

    /**
     * Synchronously schedules a Runnable task for execution asynchronously
     * after the given <code>initialDelay</code>, and subsequently with the given
     * <code>delay</code> started from the task finishing moment.
     * Subsequent executions are stopped if any execution of the task throws an exception.
     * Otherwise, task could be terminated via cancellation or
     * termination of the executor.
     *
     * @param task the task to execute
     * @param initialDelay the time to delay first execution
     * @param delay the delay between the termination of one
     * execution and the commencement of the next
     * @param unit the time unit of the initialDelay and delay parameters
     * @return a ScheduledFuture representing pending completion of
     *         the task, and whose {@code get()} method will throw an
     *         exception upon cancellation
     */
    @Override
    RScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit);

    /**
     * Synchronously schedules a Runnable task with
     * specified <code>id</code> for execution asynchronously
     * after the given <code>initialDelay</code>, and subsequently with the given
     * <code>delay</code> started from the task finishing moment.
     * Subsequent executions are stopped if any execution of the task throws an exception.
     * Otherwise, task could be terminated via cancellation or
     * termination of the executor.
     *
     * @param id task id
     * @param task the task to execute
     * @param initialDelay the time to delay first execution
     * @param delay the delay between the termination of one
     * execution and the commencement of the next
     * @return a ScheduledFuture representing pending completion of
     *         the task, and whose {@code get()} method will throw an
     *         exception upon cancellation
     */
    RScheduledFuture<?> scheduleWithFixedDelay(String id, Runnable task, Duration initialDelay, Duration delay);
    
    /**
     * 按 Cron 表达式调度 {@link Runnable} 任务。
     * 任一次执行抛异常则停止后续调度；也可通过取消或关闭执行器终止。
     *
     * @param task the task to execute
     * @param cronSchedule cron schedule object
     * @return {@link RScheduledFuture} 对象
     */
    RScheduledFuture<?> schedule(Runnable task, CronSchedule cronSchedule);

    /**
     * Synchronously schedules a Runnable task with
     * defined <code>id</code> for execution asynchronously
     * cron schedule object.
     * Subsequent executions are stopped if any execution of the task throws an exception.
     * Otherwise, task could be terminated via cancellation or
     * termination of the executor.
     *
     * @param id task id
     * @param task the task to execute
     * @param cronSchedule cron schedule object
     * @return future object
     */
    RScheduledFuture<?> schedule(String id, Runnable task, CronSchedule cronSchedule);

}
