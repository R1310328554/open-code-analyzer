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
import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.executor.params.ScheduledParameters;
import org.redisson.remote.RemoteServiceRequest;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * 定时/调度任务的远程服务实现，继承 {@link TasksService}。
 * <p>
 * 将任务写入 Redis 有序集合（scheduler queue）而非立即执行队列，
 * 并支持固定 requestId 以便周期任务复用同一标识。
 *
 * @author Nikita Koksharov
 *
 */
public class ScheduledTasksService extends TasksService {

    /** 可选的固定 requestId，周期任务重调度时复用。 */
    private String requestId;
    
    /** 构造调度任务服务，绑定编解码器与执行器名称。 */
    public ScheduledTasksService(Codec codec, String name, CommandAsyncExecutor commandExecutor, String redissonId) {
        super(codec, name, commandExecutor, redissonId);
    }
    
    /** 设置固定 requestId，覆盖默认 UUID 生成逻辑。 */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    /**
     * 将调度任务原子写入 Redis：scheduler ZSET、任务哈希、计数器等。
     * <p>
     * 若执行器处于 shutdown 状态，则写入失败重试队列（{@code ff:} 前缀）。
     */
    @Override
    protected CompletableFuture<Boolean> addAsync(String requestQueueName, RemoteServiceRequest request) {
        ScheduledParameters params = (ScheduledParameters) request.getArgs()[0];

        String taskName = tasksLatchName + ":" + request.getId();

        long expireTime = 0;
        if (params.getTtl() > 0) {
            expireTime = System.currentTimeMillis() + params.getTtl();
        }
        
        String script = "";
        if (requestId != null) {
            script += "if redis.call('hget', KEYS[5], ARGV[2]) == false then "
                        + "return 0;"
                    + "end;";
        }
        
        script +=
                // 检查执行器是否未处于 shutdown 状态
                "if redis.call('exists', KEYS[2]) == 0 then "
                    + "local retryInterval = redis.call('get', KEYS[6]); "
                    + "if retryInterval ~= false then "
                        + "local time = tonumber(ARGV[1]) + tonumber(retryInterval);"
                        + "redis.call('zadd', KEYS[3], time, 'ff:' .. ARGV[2]);"
                    + "elseif tonumber(ARGV[4]) > 0 then "
                        + "redis.call('set', KEYS[6], ARGV[4]);"
                        + "local time = tonumber(ARGV[1]) + tonumber(ARGV[4]);"
                        + "redis.call('zadd', KEYS[3], time, 'ff:' .. ARGV[2]);"
                    + "end; "

                    + "if tonumber(ARGV[5]) > 0 then "
                        + "redis.call('zadd', KEYS[7], ARGV[5], ARGV[2]);"
                    + "end; "

                    + "redis.call('zadd', KEYS[3], ARGV[1], ARGV[2]);"
                    + "redis.call('hset', KEYS[5], ARGV[2], ARGV[3]);"
                    + "redis.call('del', KEYS[8]);"
                    + "redis.call('incr', KEYS[1]);"
                    + "local v = redis.call('zrange', KEYS[3], 0, 0); "
                    // 若新任务成为调度队列队首，则 publish 开始时间通知各 scheduler worker
                    + "if v[1] == ARGV[2] then "
                       + "redis.call('publish', KEYS[4], ARGV[1]); "
                    + "end "
                    + "return 1;"
                + "end;"
                + "return 0;";
        
        RFuture<Boolean> f = commandExecutor.evalWriteNoRetryAsync(tasksCounterName, LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN, script,
                Arrays.asList(tasksCounterName, statusName, schedulerQueueName,
                        schedulerChannelName, tasksName, tasksRetryIntervalName, tasksExpirationTimeName, taskName),
                params.getStartTime(), request.getId(), encode(request), tasksRetryInterval, expireTime);
        return f.toCompletableFuture();
    }
    
    /** 从调度队列、执行队列及任务哈希中移除指定 taskId。 */
    @Override
    protected CompletableFuture<Boolean> removeAsync(String requestQueueName, String taskId) {
        RFuture<Boolean> f = commandExecutor.evalWriteNoRetryAsync(requestQueueName, StringCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "local task = redis.call('hget', KEYS[6], ARGV[1]); "
                  + "redis.call('hdel', KEYS[6], ARGV[1]); "
                  
                  + "redis.call('zrem', KEYS[2], 'ff:' .. ARGV[1]); "
                  + "redis.call('zrem', KEYS[8], ARGV[1]); "

                  + "local removedScheduled = redis.call('zrem', KEYS[2], ARGV[1]); "
                  + "local removed = redis.call('lrem', KEYS[1], 1, ARGV[1]); "

                  // 从执行器队列中移除
                  + "if task ~= false and (removed > 0 or removedScheduled > 0) then "
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
              Arrays.asList(requestQueueName, schedulerQueueName, tasksCounterName, statusName,
                                terminationTopicName, tasksName, tasksRetryIntervalName, tasksExpirationTimeName),
                taskId, RedissonExecutorService.SHUTDOWN_STATE, RedissonExecutorService.TERMINATED_STATE);
        return f.toCompletableFuture();
    }
    
    /** 调度任务超时需加上距开始时间的等待时长。 */
    @Override
    protected long getTimeout(Long executionTimeoutInMillis, RemoteServiceRequest request) {
        if (request.getArgs()[0].getClass() == ScheduledParameters.class) {
            ScheduledParameters params = (ScheduledParameters) request.getArgs()[0];
            return executionTimeoutInMillis + params.getStartTime() - System.currentTimeMillis();
        }
        return executionTimeoutInMillis;
    }
    
    /** 若已设置 requestId 则直接使用，否则委托父类生成。 */
    @Override
    protected String generateRequestId(Object[] args) {
        if (requestId == null) {
            return super.generateRequestId(args);
        }
        return requestId;
    }

}
