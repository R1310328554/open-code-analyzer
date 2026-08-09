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
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 分布式 {@link java.util.concurrent.ExecutorService} API。
 * <p>任务提交至 Redis 队列，由 {@link org.redisson.api.RExecutorService} Worker 异步执行。
 *
 * @author Nikita Koksharov
 */
public interface RExecutorService extends ExecutorService, RExecutorServiceAsync {

    /**
     * MapReduce 专用执行器名称。
     */
    String MAPREDUCE_NAME = "redisson_mapreduce";
    
    /**
     * 同步提交有返回值的任务并在 Worker 上异步执行，返回表示待完成结果的 Future。
     * Future representing the pending results of the task. The
     * Future's {@code get} method will return the task's result upon
     * successful completion.
     *
     * @param task 待提交任务
     * @param <T> the type of the task's result
     * @return 表示任务待完成状态的 Future
     */
    @Override
    <T> RExecutorFuture<T> submit(Callable<T> task);

    /**
     * 同步提交带指定 ID 的有返回值任务并在 Worker 上异步执行。
     * with specified id for execution asynchronously.
     * Returns a Future representing the pending results of the task.
     *
     * @param id 任务 ID
     * @param task 待提交任务
     * @param <T> the type of the task's result
     * @return 表示任务待完成状态的 Future
     */
    <T> RExecutorFuture<T> submit(String id, Callable<T> task);

    /**
     * 同步提交带 TTL 的有返回值任务并在 Worker 上异步执行。
     * for execution asynchronously. Returns a Future representing the pending
     * results of the task. The Future's {@code get} method will return the
     * task's result upon successful completion.
     *
     * @param task 待提交任务
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @param <T> the type of the task's result
     * @return 表示任务待完成状态的 Future
     */
    <T> RExecutorFuture<T> submit(Callable<T> task, long timeToLive, TimeUnit timeUnit);

    /**
     * 同步提交带 ID 与 TTL 的有返回值任务并在 Worker 上异步执行。
     * defined <code>id</code> and <code>timeToLive</code> parameters
     * for execution asynchronously.
     * Returns a Future representing the pending results of the task.
     *
     * @param id 任务 ID
     * @param task 待提交任务
     * @param timeToLive 存活时间
     * @param <T> the type of the task's result
     * @return 表示任务待完成状态的 Future
     */
    <T> RExecutorFuture<T> submit(String id, Callable<T> task, Duration timeToLive);

    /**
     * 同步批量提交任务；全部任务原子写入执行器请求队列，任一失败则全部不写入。
     * All tasks are stored to executor request queue atomically, 
     * if case of any error none of tasks will be added.
     * 
     * @param tasks 待执行任务
     * @return Future 对象
     */
    RExecutorBatchFuture submit(Callable<?>... tasks);
    
    /**
     * 同步提交 Runnable 任务并在 Worker 上异步执行。
     * and returns a RExecutorFuture representing that task. The Future's {@code get} method will
     * return the given result upon successful completion.
     *
     * @param task 待提交任务
     * @param result 返回结果
     * @param <T> the type of the result
     * @return 表示任务待完成状态的 Future
     */
    @Override
    <T> RExecutorFuture<T> submit(Runnable task, T result);

    /**
     * Synchronously submits a Runnable task for execution asynchronously.
     * Returns a RExecutorFuture representing task completion. The Future's {@code get} method will
     * return {@code null} upon <em>successful</em> completion.
     *
     * @param task the task to submit
     * @return a Future representing pending completion of the task
     */
    @Override
    RExecutorFuture<?> submit(Runnable task);

    /**
     * 同步提交带指定 ID 的 Runnable 任务并在 Worker 上异步执行。
     * Returns a RExecutorFuture representing task completion.
     *
     * @param id 任务 ID
     * @param task 待提交任务
     * @return 表示任务待完成状态的 Future
     */
    RExecutorFuture<?> submit(String id, Runnable task);

    /**
     * 同步提交带 TTL 的任务并在 Worker 上异步执行。
     * for execution asynchronously. Returns a Future representing task completion.
     * The Future's {@code get} method will return the
     * task's result upon successful completion.
     *
     * @param task 待提交任务
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 表示任务待完成状态的 Future
     */
    RExecutorFuture<?> submit(Runnable task, long timeToLive, TimeUnit timeUnit);

    /**
     * 同步提交带 ID 与 TTL 的任务并在 Worker 上异步执行。
     * with defined <code>id</code> and <code>timeToLive</code> parameters
     * for execution asynchronously.
     * Returns a Future representing task completion.
     *
     * @param id 任务 ID
     * @param task 待提交任务
     * @param timeToLive 存活时间
     * @return 表示任务待完成状态的 Future
     */
    RExecutorFuture<?> submit(String id, Runnable task, Duration timeToLive);

    /**
     * Synchronously submits tasks batch for execution asynchronously.
     * All tasks are stored to executor request queue atomically, 
     * if case of any error none of tasks will be added.
     * 
     * @param tasks - tasks to execute
     * @return Future object
     */
    RExecutorBatchFuture submit(Runnable... tasks);
    
    /**
     * 返回执行器名称。
     * 
     * @return 执行器名称
     */
    String getName();
    
    /**
     * 删除执行器请求队列及状态对象。
     * 
     * @return 见方法说明
     */
    boolean delete();

    /*
     * Use {@link #registerWorkers(WorkerOptions)} setting instead
     * 
     */
    @Deprecated
    void registerWorkers(int workers);

    /*
     * Use {@link #registerWorkers(WorkerOptions)} setting instead
     * 
     */
    @Deprecated
    void registerWorkers(int workers, ExecutorService executor);

    /**
     * 注册 Worker 以消费任务队列。
     * 
     * @param options Worker 配置
     */
    void registerWorkers(WorkerOptions options);

    /**
     * 注销所有已注册的 Worker。
     *
     */
    void deregisterWorkers();

    /**
     * 返回等待执行或正在执行的任务数量。
     *
     * @return 任务数量
     */
    int getTaskCount();

    /**
     * 返回可用于执行任务的活跃 Worker 数量。
     * 
     * @return Worker 数量
     */
    int countActiveWorkers();

    /**
     * 若存在指定 ID 且处于等待或执行中的任务则返回 {@code true}。
     * by <code>taskId</code> awaiting execution or currently in execution.
     *
     * @param taskId 任务 ID
     * @return 见方法说明
     */
    boolean hasTask(String taskId);

    /**
     * 返回等待执行或正在执行的任务 ID 集合。
     *
     * @return 任务 ID 集合
     */
    Set<String> getTaskIds();

    /**
     * 按任务 ID 取消任务。
     * 
     * @see RExecutorFuture#getTaskId()
     * 
     * @param taskId 任务 ID
     * @return 见方法说明
     *          or <code>null</code> if task wasn't found
     */
    Boolean cancelTask(String taskId);
    
    /**
     * 同步批量提交 Runnable 任务；全部任务原子写入执行器请求队列，任一失败则全部不写入。
     * All tasks are stored to executor request queue atomically, 
     * if case of any error none of tasks will be added.
     * 
     * @param tasks 待执行任务
     */
    void execute(Runnable... tasks);

}
