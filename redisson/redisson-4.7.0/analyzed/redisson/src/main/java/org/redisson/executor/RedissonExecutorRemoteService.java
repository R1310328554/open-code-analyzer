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

import org.redisson.RedissonExecutorService;
import org.redisson.RedissonObject;
import org.redisson.RedissonRemoteService;
import org.redisson.RedissonShutdownException;
import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.executor.*;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 远程执行器专用的 {@link RedissonRemoteService} 扩展。
 * <p>
 * 重写任务拉取逻辑以处理过期/超时任务，并在 Worker 端执行任务方法时
 * 触发 started/finished/success/failure 监听器。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonExecutorRemoteService extends RedissonRemoteService {

    /** 日志记录器。 */
    private static final Logger log = LoggerFactory.getLogger(RedissonExecutorRemoteService.class);

    /** 任务过期时间有序集合键名。 */
    private String tasksExpirationTimeName;
    /** 进行中任务计数器键名。 */
    private String tasksCounterName;
    /** 执行器状态键名。 */
    private String statusName;
    /** 任务重试间隔键名。 */
    private String tasksRetryIntervalName;
    /** 终止通知 Pub/Sub 主题名。 */
    private String terminationTopicName;
    /** 调度队列有序集合键名。 */
    private String schedulerQueueName;
    /** 单任务执行超时（毫秒），0 表示不限制。 */
    private long taskTimeout;
    /** 任务开始监听器列表。 */
    private List<TaskStartedListener> startedListeners;
    /** 任务结束监听器列表。 */
    private List<TaskFinishedListener> finishedListeners;
    /** 任务失败监听器列表。 */
    private List<TaskFailureListener> failureListeners;
    /** 任务成功监听器列表。 */
    private List<TaskSuccessListener> successListeners;

    /** 构造远程执行器 RPC 服务。 */
    public RedissonExecutorRemoteService(Codec codec, String name,
                                         CommandAsyncExecutor commandExecutor, String executorId) {
        super(codec, name, commandExecutor, executorId);
    }

    /**
     * 拉取任务：若任务已过期则从各 Redis 结构清理并返回 null；
     * 否则返回哈希表中的请求体。
     */
    @Override
    protected RFuture<RemoteServiceRequest> getTask(String requestId, RMap<String, RemoteServiceRequest> tasks) {
        return commandExecutor.evalWriteNoRetryAsync(((RedissonObject) tasks).getRawName(), codec, RedisCommands.EVAL_OBJECT,
                  // 检查任务是否在过期 ZSET 中且已超时
                  "local value = redis.call('zscore', KEYS[2], ARGV[1]); " +
                  "if (value ~= false and tonumber(value) < tonumber(ARGV[2])) then "
                    + "redis.call('zrem', KEYS[2], ARGV[1]); "

                    + "redis.call('zrem', KEYS[7], ARGV[1]); "
                    + "redis.call('zrem', KEYS[7], 'ff:' .. ARGV[1]);"

                    + "redis.call('hdel', KEYS[1], ARGV[1]); "
                    + "if redis.call('decr', KEYS[3]) == 0 then "
                        + "redis.call('del', KEYS[3]);"
                        + "if redis.call('get', KEYS[4]) == ARGV[3] then "
                            + "redis.call('del', KEYS[5]);"
                            + "redis.call('set', KEYS[4], ARGV[4]);"
                            + "redis.call('publish', KEYS[6], ARGV[4]);"
                        + "end;"
                    + "end;"

                    + "return nil;"
                + "end;"
                + "return redis.call('hget', KEYS[1], ARGV[1]); ",
        Arrays.asList(((RedissonObject) tasks).getRawName(), tasksExpirationTimeName, tasksCounterName, statusName,
                            tasksRetryIntervalName, terminationTopicName, schedulerQueueName),
        requestId, System.currentTimeMillis(), RedissonExecutorService.SHUTDOWN_STATE, RedissonExecutorService.TERMINATED_STATE);
    }

    /** 在 Worker 端反射调用任务方法，并通知各生命周期监听器。 */
    @Override
    protected <T> void invokeMethod(RemoteServiceRequest request, RemoteServiceMethod method,
                                    CompletableFuture<RemoteServiceCancelRequest> cancelRequestFuture,
                                    CompletableFuture<RRemoteServiceResponse> responsePromise) {
        // 通知任务已开始
        startedListeners.forEach(l -> l.onStarted(request.getId()));

        // 可选：超时后自动发起取消请求
        if (taskTimeout > 0) {
            commandExecutor.getServiceManager().newTimeout(t -> {
                cancelRequestFuture.complete(new RemoteServiceCancelRequest(true, false));
            }, taskTimeout, TimeUnit.MILLISECONDS);
        }

        try {
            Object result = method.getMethod().invoke(method.getBean(), request.getArgs());

            RemoteServiceResponse response = new RemoteServiceResponse(request.getId(), result);
            responsePromise.complete(response);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException
                && e.getCause() instanceof RedissonShutdownException) {
                if (cancelRequestFuture != null) {
                    cancelRequestFuture.cancel(false);
                }
                return;
            }
            RemoteServiceResponse response = new RemoteServiceResponse(request.getId(), e.getCause());
            responsePromise.complete(response);
            log.error("Can't execute: {}", request, e);
        }

        if (cancelRequestFuture != null) {
            cancelRequestFuture.cancel(false);
        }

        if (commandExecutor.getNow(responsePromise) instanceof RemoteServiceResponse) {
            RemoteServiceResponse response = (RemoteServiceResponse) commandExecutor.getNow(responsePromise);
            if (response.getError() == null) {
                successListeners.forEach(l -> l.onSucceeded(request.getId(), response.getResult()));
            } else {
                failureListeners.forEach(l -> l.onFailed(request.getId(), response.getError()));
            }
        } else {
            failureListeners.forEach(l -> l.onFailed(request.getId(), null));
        }

        // 通知任务已结束（无论成败）
        finishedListeners.forEach(l -> l.onFinished(request.getId()));
    }

    /** 按类型拆分并注册任务生命周期监听器。 */
    public void setListeners(List<TaskListener> listeners) {
        startedListeners = listeners.stream()
                                .filter(x -> x instanceof TaskStartedListener)
                                .map(x -> (TaskStartedListener) x)
                                .collect(Collectors.toList());

        finishedListeners = listeners.stream()
                                .filter(x -> x instanceof TaskFinishedListener)
                                .map(x -> (TaskFinishedListener) x)
                                .collect(Collectors.toList());

        failureListeners = listeners.stream()
                                .filter(x -> x instanceof TaskFailureListener)
                                .map(x -> (TaskFailureListener) x)
                                .collect(Collectors.toList());

        successListeners = listeners.stream()
                                .filter(x -> x instanceof TaskSuccessListener)
                                .map(x -> (TaskSuccessListener) x)
                                .collect(Collectors.toList());
    }

    /** 设置单任务执行超时（毫秒）。 */
    public void setTaskTimeout(long taskTimeout) {
        this.taskTimeout = taskTimeout;
    }

    /** 设置调度队列 Redis 键名。 */
    public void setSchedulerQueueName(String schedulerQueueName) {
        this.schedulerQueueName = schedulerQueueName;
    }

    /** 设置任务过期时间 ZSET 键名。 */
    public void setTasksExpirationTimeName(String tasksExpirationTimeName) {
        this.tasksExpirationTimeName = tasksExpirationTimeName;
    }

    /** 设置进行中任务计数器键名。 */
    public void setTasksCounterName(String tasksCounterName) {
        this.tasksCounterName = tasksCounterName;
    }

    /** 设置执行器状态键名。 */
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    /** 设置任务重试间隔键名。 */
    public void setTasksRetryIntervalName(String tasksRetryIntervalName) {
        this.tasksRetryIntervalName = tasksRetryIntervalName;
    }

    /** 设置终止通知主题名。 */
    public void setTerminationTopicName(String terminationTopicName) {
        this.terminationTopicName = terminationTopicName;
    }
}
