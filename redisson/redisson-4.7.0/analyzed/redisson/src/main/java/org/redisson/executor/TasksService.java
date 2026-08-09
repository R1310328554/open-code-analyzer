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
import org.redisson.api.RBlockingQueueAsync;
import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.executor.params.TaskParameters;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.remote.*;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 分布式执行器客户端任务提交服务，继承 {@link org.redisson.remote.BaseRemoteService}。
 * <p>
 * 负责将 {@link TaskParameters} 写入 Redis 队列、取消任务及查询任务是否存在。
 *
 * @author Nikita Koksharov
 *
 */
public class TasksService extends BaseRemoteService {

    /** 终止通知 Pub/Sub 主题。 */
    protected String terminationTopicName;
    /** 活跃任务计数 Redis key。 */
    protected String tasksCounterName;
    /** 执行器状态 key。 */
    protected String statusName;
    /** 任务元数据哈希 key。 */
    protected String tasksName;
    /** 任务 latch key 前缀。 */
    protected String tasksLatchName;
    /** 调度 ZSET key。 */
    protected String schedulerQueueName;
    /** 调度变更通知频道。 */
    protected String schedulerChannelName;
    /** 失败重试间隔 key。 */
    protected String tasksRetryIntervalName;
    /** 任务 TTL 过期 ZSET key。 */
    protected String tasksExpirationTimeName;
    /** 默认任务重试间隔（毫秒）。 */
    protected long tasksRetryInterval;
    
    /** 构造任务服务，绑定编解码器与 Redis 命令执行器。 */
    public TasksService(Codec codec, String name, CommandAsyncExecutor commandExecutor, String executorId) {
        super(codec, name, commandExecutor, executorId);
    }

    public void setTasksLatchName(String tasksLatchName) {
        this.tasksLatchName = tasksLatchName;
    }

    public void setTasksExpirationTimeName(String tasksExpirationTimeName) {
        this.tasksExpirationTimeName = tasksExpirationTimeName;
    }

    public void setTasksRetryIntervalName(String tasksRetryIntervalName) {
        this.tasksRetryIntervalName = tasksRetryIntervalName;
    }
    
    public void setTasksRetryInterval(long tasksRetryInterval) {
        this.tasksRetryInterval = tasksRetryInterval;
    }
    
    public void setTerminationTopicName(String terminationTopicName) {
        this.terminationTopicName = terminationTopicName;
    }
    
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
    
    public void setTasksCounterName(String tasksCounterName) {
        this.tasksCounterName = tasksCounterName;
    }
    
    public void setTasksName(String tasksName) {
        this.tasksName = tasksName;
    }
    
    public void setSchedulerChannelName(String schedulerChannelName) {
        this.schedulerChannelName = schedulerChannelName;
    }
    
    public void setSchedulerQueueName(String scheduledQueueName) {
        this.schedulerQueueName = scheduledQueueName;
    }

    /** 入队并将 add Future 关联到 {@link RemotePromise}，失败时抛 IllegalStateException。 */
    @Override
    protected final CompletableFuture<Boolean> addAsync(String requestQueueName,
                                                        RemoteServiceRequest request, RemotePromise<Object> result) {
        CompletableFuture<Boolean> future = addAsync(requestQueueName, request);
        result.setAddFuture(future);
        
        return future.thenApply(res -> {
            if (!res) {
                throw new IllegalStateException("Task hasn't been added. Check if executorService exists and task id is unique");
            }

            return true;
        });
    }

    /** 返回用于 add 脚本的命令执行器，子类可覆写为批量模式。 */
    protected CommandAsyncExecutor getAddCommandExecutor() {
        return commandExecutor;
    }
    
    /** 原子将任务写入 Redis：哈希、执行队列、调度 ZSET 及过期时间。 */
    protected CompletableFuture<Boolean> addAsync(String requestQueueName, RemoteServiceRequest request) {
        TaskParameters params = (TaskParameters) request.getArgs()[0];

        String taskName = tasksLatchName + ":" + request.getId();

        long retryStartTime = 0;
        if (tasksRetryInterval > 0) {
            retryStartTime = System.currentTimeMillis() + tasksRetryInterval;
        }
        long expireTime = 0;
        if (params.getTtl() > 0) {
            expireTime = System.currentTimeMillis() + params.getTtl();
        }

        RFuture<Boolean> f = getAddCommandExecutor().evalWriteNoRetryAsync(tasksCounterName, StringCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                        // 检查执行器是否未处于 shutdown 状态
                        "if redis.call('exists', KEYS[2]) == 0 then "
                            + "redis.call('hset', KEYS[5], ARGV[2], ARGV[3]);"
                            + "redis.call('del', KEYS[9]);"
                            + "redis.call('rpush', KEYS[6], ARGV[2]); "
                            + "redis.call('incr', KEYS[1]);"

                            + "if tonumber(ARGV[5]) > 0 then "
                                + "redis.call('zadd', KEYS[8], ARGV[5], ARGV[2]);"
                            + "end; "

                            + "if tonumber(ARGV[1]) > 0 then "
                                + "local scheduledName = 'ff:' .. ARGV[2];"
                                + "redis.call('set', KEYS[7], ARGV[4]);"
                                + "redis.call('zadd', KEYS[3], ARGV[1], scheduledName);"
                                + "local v = redis.call('zrange', KEYS[3], 0, 0); "
                                // 新任务成为调度队首时 publish 开始时间
                                + "if v[1] == scheduledName then "
                                    + "redis.call('publish', KEYS[4], ARGV[1]); "
                                + "end; "
                            + "end;"
                            + "return 1;"
                        + "end;"
                        + "return 0;",
                        Arrays.asList(tasksCounterName, statusName, schedulerQueueName, schedulerChannelName,
                                            tasksName, requestQueueName, tasksRetryIntervalName, tasksExpirationTimeName, taskName),
                        retryStartTime, request.getId(), encode(request), tasksRetryInterval, expireTime);
        return f.toCompletableFuture();
    }
    
    /** 从队列、调度 ZSET 与 tasks 哈希中移除任务并更新计数器。 */
    @Override
    protected CompletableFuture<Boolean> removeAsync(String requestQueueName, String taskId) {
        RFuture<Boolean> f = commandExecutor.evalWriteNoRetryAsync(requestQueueName, LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
          "if redis.call('exists', KEYS[3]) == 0 then " +
                    "return nil;" +
                "end;" +

                "redis.call('zrem', KEYS[2], 'ff:' .. ARGV[1]); "
              + "redis.call('zrem', KEYS[8], ARGV[1]); "
              + "local task = redis.call('hget', KEYS[6], ARGV[1]); "
              + "redis.call('hdel', KEYS[6], ARGV[1]); "

              + "local removed = redis.call('lrem', KEYS[1], 1, ARGV[1]); "

               // 从执行器队列移除
              + "if task ~= false and removed > 0 then "
                  + "if redis.call('decr', KEYS[3]) == 0 then "
                     + "redis.call('del', KEYS[3]);"
                     + "if redis.call('get', KEYS[4]) == ARGV[2] then "
                        + "redis.call('del', KEYS[7]);"
                        + "redis.call('set', KEYS[4], ARGV[3]);"
                        + "redis.call('publish', KEYS[5], ARGV[3]);"
                     + "end;"
                  + "end;"
                  + "return 1;"
              + "end;"
              + "if task == false then "
                  + "return nil; "
              + "end;"
              + "return 0;",
          Arrays.asList(requestQueueName, schedulerQueueName, tasksCounterName, statusName, terminationTopicName,
                                tasksName, tasksRetryIntervalName, tasksExpirationTimeName),
          taskId, RedissonExecutorService.SHUTDOWN_STATE, RedissonExecutorService.TERMINATED_STATE);
        return f.toCompletableFuture();
    }

    /** 使用 TaskParameters 中预设的 requestId。 */
    @Override
    protected String generateRequestId(Object[] args) {
        TaskParameters params = (TaskParameters) args[0];
        return params.getRequestId();
    }

    /** 异步取消：先尝试从队列移除，否则发送 cancel 请求并轮询响应。 */
    public RFuture<Boolean> cancelExecutionAsync(String requestId) {
        String requestQueueName = getRequestQueueName(RemoteExecutorService.class);
        CompletableFuture<Boolean> removeFuture = removeAsync(requestQueueName, requestId);
        CompletableFuture<Boolean> f = removeFuture.thenCompose(res -> {
            if (res == null) {
                return CompletableFuture.completedFuture(null);
            }
            if (res) {
                return CompletableFuture.completedFuture(true);
            }

            RMap<String, RemoteServiceCancelRequest> canceledRequests = getMap(cancelRequestMapName);
            canceledRequests.putAsync(requestId, new RemoteServiceCancelRequest(true, true));
            canceledRequests.expireAsync(60, TimeUnit.SECONDS);

            CompletableFuture<RemoteServiceCancelResponse> response = scheduleCancelResponseCheck(cancelResponseMapName, requestId);
            return response.thenApply(r -> {
                if (r == null) {
                    return false;
                }
                return r.isCanceled();
            });
        });

        removeFuture.thenAccept(r -> {
            commandExecutor.getServiceManager().newTimeout(timeout -> {
                f.complete(false);
            }, 60, TimeUnit.SECONDS);
        });

        return new CompletableFutureWrapper<>(f);
    }

    /** 每 3 秒轮询 cancel 响应 map，直至收到确认或确认任务已不存在。 */
    private CompletableFuture<RemoteServiceCancelResponse> scheduleCancelResponseCheck(String mapName, String requestId) {
        CompletableFuture<RemoteServiceCancelResponse> cancelResponse = new CompletableFuture<>();

        commandExecutor.getServiceManager().newTimeout(timeout -> {
            if (cancelResponse.isDone()) {
                return;
            }

            RMap<String, RemoteServiceCancelResponse> canceledResponses = getMap(mapName);
            RFuture<RemoteServiceCancelResponse> removeFuture = canceledResponses.removeAsync(requestId);
            CompletableFuture<RemoteServiceCancelResponse> future = removeFuture.thenCompose(response -> {
                if (response == null) {
                    RFuture<Boolean> f = hasTaskAsync(requestId);
                    return f.thenCompose(r -> {
                        if (r) {
                            return scheduleCancelResponseCheck(mapName, requestId);
                        }

                        RemoteServiceCancelResponse resp = new RemoteServiceCancelResponse(requestId, false);
                        return CompletableFuture.completedFuture(resp);
                    });
                }

                RBlockingQueueAsync<RRemoteServiceResponse> queue = getBlockingQueue(responseQueueName, codec);
                return queue.removeAsync(response).thenApply(r -> response);
            }).whenComplete((r, ex) -> {
                if (ex != null) {
                    scheduleCancelResponseCheck(mapName, requestId);
                }
            }).toCompletableFuture();

            commandExecutor.transfer(future, cancelResponse);
        }, 3000, TimeUnit.MILLISECONDS);
        return cancelResponse;
    }

    /** 异步检查 taskId 是否仍存在于 tasks 哈希表。 */
    public RFuture<Boolean> hasTaskAsync(String taskId) {
        return commandExecutor.writeAsync(tasksName, LongCodec.INSTANCE, RedisCommands.HEXISTS, tasksName, taskId);
    }

}
