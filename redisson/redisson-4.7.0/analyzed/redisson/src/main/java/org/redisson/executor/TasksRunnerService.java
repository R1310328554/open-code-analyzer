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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.redisson.RedissonExecutorService;
import org.redisson.RedissonShutdownException;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.redisson.api.RemoteInvocationOptions;
import org.redisson.cache.LRUCacheMap;
import org.redisson.client.RedisException;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.codec.CustomObjectInputStream;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.executor.params.*;
import org.redisson.misc.Hash;
import org.redisson.misc.HashValue;
import org.redisson.misc.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 分布式执行器 Worker 端实现，运行 Callable 与 Runnable 任务。
 * <p>
 * 实现 {@link RemoteExecutorService}：反序列化任务、注入依赖、
 * 执行逻辑并在 Redis 中更新任务计数与调度状态。
 *
 * @author Nikita Koksharov
 *
 */
public class TasksRunnerService implements RemoteExecutorService {

    private static final Logger log = LoggerFactory.getLogger(TasksRunnerService.class);

    /** 任务类字节码哈希到专用 Codec 的 LRU 缓存，避免重复创建 ClassLoader。 */
    private static final Map<HashValue, Codec> CODECS = new LRUCacheMap<>(500, 0, 0);
    
    /** 默认编解码器。 */
    private final Codec codec;
    /** 执行器服务名称（Redis key 前缀）。 */
    private final String name;
    /** 异步 Redis 命令执行器。 */
    private final CommandAsyncExecutor commandExecutor;

    /** 注入到任务中的 Redisson 客户端引用。 */
    private final RedissonClient redisson;
    
    /** 活跃任务计数器 Redis key。 */
    private String tasksCounterName;
    /** 执行器状态（running/shutdown/terminated）Redis key。 */
    private String statusName;
    /** 终止通知 Pub/Sub 频道名。 */
    private String terminationTopicName;
    /** 任务参数哈希表 Redis key。 */
    private String tasksName;
    /** 任务 latch 前缀 key。 */
    private String tasksLatchName;
    /** 调度队列（ZSET）Redis key。 */
    private String schedulerQueueName;
    /** 调度变更通知频道。 */
    private String schedulerChannelName;
    /** 失败重试间隔配置 key。 */
    private String tasksRetryIntervalName;
    /** 任务过期时间 ZSET key。 */
    private String tasksExpirationTimeName;

    /** 可选的自定义任务依赖注入器。 */
    private TasksInjector tasksInjector;

    /** 调度任务迟到阈值（毫秒），超时则跳过本次执行。 */
    private long taskLateThreshold;

    /** 构造 Worker 端任务运行服务。 */
    public TasksRunnerService(CommandAsyncExecutor commandExecutor, RedissonClient redisson, Codec codec, String name) {
        this.commandExecutor = commandExecutor;
        this.name = name;
        this.redisson = redisson;
        this.codec = codec;
    }

    /** 设置任务依赖注入器（如 SpringTasksInjector）。 */
    public void setTasksInjector(TasksInjector tasksInjector) {
        this.tasksInjector = tasksInjector;
    }

    /** 设置调度任务允许的最大迟到毫秒数。 */
    public void setTaskLateThreshold(long taskLateThreshold) {
        this.taskLateThreshold = taskLateThreshold;
    }

    public void setTasksExpirationTimeName(String tasksExpirationTimeName) {
        this.tasksExpirationTimeName = tasksExpirationTimeName;
    }

    public void setTasksRetryIntervalName(String tasksRetryInterval) {
        this.tasksRetryIntervalName = tasksRetryInterval;
    }
    
    public void setSchedulerQueueName(String schedulerQueueName) {
        this.schedulerQueueName = schedulerQueueName;
    }
    
    public void setSchedulerChannelName(String schedulerChannelName) {
        this.schedulerChannelName = schedulerChannelName;
    }
    
    public void setTasksName(String tasksName) {
        this.tasksName = tasksName;
    }
    
    public void setTasksCounterName(String tasksCounterName) {
        this.tasksCounterName = tasksCounterName;
    }
    
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public void setTerminationTopicName(String terminationTopicName) {
        this.terminationTopicName = terminationTopicName;
    }

    public void setTasksLatchName(String tasksLatchName) {
        this.tasksLatchName = tasksLatchName;
    }

    /** 执行本轮 fixed-rate 任务并按周期计算下次开始时间后重调度。 */
    @Override
    public void scheduleAtFixedRate(ScheduledAtFixedRateParameters params) {
        long start = System.nanoTime();
        executeRunnable(params, false);
        if (!hasTask(params.getRequestId())) {
            return;
        }

        long spent = params.getSpentTime()
                                + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        long newStartTime = System.currentTimeMillis() + Math.max(params.getPeriod() - spent, 0);
        params.setStartTime(newStartTime);
        spent = Math.max(spent - params.getPeriod(), 0);
        params.setSpentTime(spent);
        asyncScheduledServiceAtFixed(params.getExecutorId(), params.getRequestId()).scheduleAtFixedRate(params);
    }

    /** 检查任务是否仍存在于 tasks 哈希表中（未被取消）。 */
    private boolean hasTask(String requestId) {
        RFuture<Boolean> f = commandExecutor.writeAsync(tasksName, LongCodec.INSTANCE,
                RedisCommands.HEXISTS, tasksName, requestId);
        return commandExecutor.get(f);
    }

    /** 按 Cron 表达式计算下次触发时间并执行/重调度。 */
    @Override
    public void schedule(ScheduledCronExpressionParameters params) {
        CronExpression expression = new CronExpression(params.getCronExpression());
        expression.setTimeZone(TimeZone.getTimeZone(params.getTimezone()));
        Date nextStartDate = expression.getNextValidTimeAfter(new Date());

        executeRunnable(params, nextStartDate == null);

        if (nextStartDate == null || !hasTask(params.getRequestId())) {
            return;
        }

        params.setStartTime(nextStartDate.getTime());
        asyncScheduledServiceAtFixed(params.getExecutorId(), params.getRequestId()).schedule(params);
    }

    /**
     * 创建用于周期重调度的 {@link RemoteExecutorServiceAsync} 代理。
     * <p>
     * 使用 {@link ScheduledTasksService} 并固定 {@code requestId}，
     * 使 recurring 任务始终复用同一标识。
     *
     * @return 无 ack/result 的异步远程代理
     */
    private RemoteExecutorServiceAsync asyncScheduledServiceAtFixed(String executorId, String requestId) {
        ScheduledTasksService scheduledRemoteService = new ScheduledTasksService(codec, name, commandExecutor, executorId);
        scheduledRemoteService.setTerminationTopicName(terminationTopicName);
        scheduledRemoteService.setTasksCounterName(tasksCounterName);
        scheduledRemoteService.setStatusName(statusName);
        scheduledRemoteService.setSchedulerQueueName(schedulerQueueName);
        scheduledRemoteService.setSchedulerChannelName(schedulerChannelName);
        scheduledRemoteService.setTasksName(tasksName);
        scheduledRemoteService.setTasksLatchName(tasksLatchName);
        scheduledRemoteService.setRequestId(requestId);
        scheduledRemoteService.setTasksExpirationTimeName(tasksExpirationTimeName);
        scheduledRemoteService.setTasksRetryIntervalName(tasksRetryIntervalName);
        RemoteExecutorServiceAsync asyncScheduledServiceAtFixed = scheduledRemoteService.get(RemoteExecutorServiceAsync.class, RemoteInvocationOptions.defaults().noAck().noResult());
        return asyncScheduledServiceAtFixed;
    }
    
    /** 执行本轮任务，完成后延迟 {@code delay} 毫秒再重调度。 */
    @Override
    public void scheduleWithFixedDelay(ScheduledWithFixedDelayParameters params) {
        executeRunnable(params, false);
        
        long newStartTime = System.currentTimeMillis() + params.getDelay();
        params.setStartTime(newStartTime);
        asyncScheduledServiceAtFixed(params.getExecutorId(), params.getRequestId()).scheduleWithFixedDelay(params);
    }
    
    /** 调度 Callable 等价于立即执行 {@link #executeCallable}。 */
    @Override
    public Object scheduleCallable(ScheduledParameters params) {
        return executeCallable(params);
    }
    
    /** 调度 Runnable 等价于 {@link #executeRunnable}。 */
    @Override
    public void scheduleRunnable(ScheduledParameters params) {
        executeRunnable(params);
    }
    
    /** 反序列化 Callable、执行并在 Redis 中标记任务完成。 */
    @Override
    public Object executeCallable(TaskParameters params) {
        Object res;
        try {
            RFuture<Long> future = renewRetryTime(params.getRequestId());
            future.toCompletableFuture().get();

            Callable<?> callable = decode(params);
            res = callable.call();
        } catch (RedissonShutdownException e) {
            throw e;
        } catch (RedisException e) {
            finish(params.getRequestId(), true);
            throw e;
        } catch (ExecutionException e) {
            finish(params.getRequestId(), true);
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new IllegalArgumentException(e.getCause());
            }
        } catch (Exception e) {
            finish(params.getRequestId(), true);
            throw new IllegalArgumentException(e);
        }
        finish(params.getRequestId(), true);
        return res;
    }

    /** 定时续期失败重试任务的调度时间（shutdown 期间仍保留在 ZSET）。 */
    protected void scheduleRetryTimeRenewal(String requestId, Long retryInterval) {
        if (retryInterval == null) {
            return;
        }

        commandExecutor.getServiceManager().newTimeout(timeout -> renewRetryTime(requestId),
                                                    Math.max(1000, retryInterval / 2), TimeUnit.MILLISECONDS);
    }

    /** 若执行器 shutdown 且任务仍存在，则延长其在 scheduler 队列中的 score。 */
    protected RFuture<Long> renewRetryTime(String requestId) {
        RFuture<Long> future = commandExecutor.evalWriteAsync(name, LongCodec.INSTANCE, RedisCommands.EVAL_LONG,
                // 检查执行器是否未处于 shutdown 状态
                  "local name = ARGV[2];"
                + "local scheduledName = ARGV[2];"
                + "if string.sub(scheduledName, 1, 3) ~= 'ff:' then "
                    + "scheduledName = 'ff:' .. scheduledName; "
                + "else "
                    + "name = string.sub(name, 4, string.len(name)); "
                + "end;"
                + "local retryInterval = redis.call('get', KEYS[4]);"
                
                + "if redis.call('exists', KEYS[1]) == 0 and retryInterval ~= false and redis.call('hexists', KEYS[5], name) == 1 then "
                    + "local startTime = tonumber(ARGV[1]) + tonumber(retryInterval);"
                    + "redis.call('zadd', KEYS[2], startTime, scheduledName);"
                    + "local v = redis.call('zrange', KEYS[2], 0, 0); "
                    // 若重试任务成为队首则 publish 开始时间通知 scheduler worker
                    + "if v[1] == scheduledName then "
                        + "redis.call('publish', KEYS[3], startTime); "
                    + "end;"
                    + "return retryInterval; "
                + "end;"
                + "return nil;", 
                Arrays.asList(statusName, schedulerQueueName, schedulerChannelName, tasksRetryIntervalName, tasksName),
                System.currentTimeMillis(), requestId);
        future.whenComplete((res, e) -> {
            if (e != null) {
                scheduleRetryTimeRenewal(requestId, 10000L);
                return;
            }
            
            if (res != null) {
                scheduleRetryTimeRenewal(requestId, res);
            }
        });
        return future;
    }

    /** 计算 classpath 中指定类文件的 MurmurHash128。 */
    private HashValue hash(ClassLoader classLoader, String className) throws IOException {
        String classAsPath = className.replace('.', '/') + ".class";
        InputStream classStream = classLoader.getResourceAsStream(classAsPath);
        if (classStream == null) {
            return HashValue.EMPTY;
        }

        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        out.writeBytes(classStream, classStream.available());
        HashValue hash = new HashValue(Hash.hash128(out));
        out.release();
        return hash;
    }
    
    /**
     * 从 {@link TaskParameters} 反序列化 Callable/Runnable 实例。
     * <p>
     * 支持 lambda 与普通序列化；注入 RedissonClient 与 requestId。
     */
    @SuppressWarnings("unchecked")
    private <T> T decode(TaskParameters params) {
        ByteBuf classBodyBuf = Unpooled.wrappedBuffer(params.getClassBody());
        ByteBuf stateBuf = Unpooled.wrappedBuffer(params.getState());
        try {
            HashValue hash = new HashValue(Hash.hash128(classBodyBuf));
            Codec classLoaderCodec = CODECS.get(hash);
            if (classLoaderCodec == null) {
                HashValue v = hash(codec.getClassLoader(), params.getClassName());
                if (v.equals(hash)) {
                    classLoaderCodec = codec;
                } else {
                    RedissonClassLoader cl = new RedissonClassLoader(codec.getClassLoader());
                    cl.loadClass(params.getClassName(), params.getClassBody());

                    classLoaderCodec = this.codec.getClass().getConstructor(ClassLoader.class).newInstance(cl);
                }
                CODECS.put(hash, classLoaderCodec);
            }
            
            T task;
            if (params.getLambdaBody() != null) {
                ByteArrayInputStream is = new ByteArrayInputStream(params.getLambdaBody());
                
                // 反序列化 lambda 时将线程上下文 ClassLoader 设为任务 ClassLoader，
                // 避免反射加载类时使用错误的 loader
                ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();                
                try {
                    Thread.currentThread().setContextClassLoader(classLoaderCodec.getClassLoader());
                    ObjectInput oo = new CustomObjectInputStream(classLoaderCodec.getClassLoader(), is);
                    task = (T) oo.readObject();
                    oo.close();
                } finally {
                    Thread.currentThread().setContextClassLoader(currentThreadClassLoader);
                }
            } else {
                task = (T) classLoaderCodec.getValueDecoder().decode(stateBuf, null);
            }

            Injector.inject(task, RedissonClient.class, redisson);
            Injector.inject(task, String.class, params.getRequestId());
            
            if (tasksInjector != null) {
                tasksInjector.inject(task);
            }
            
            return task;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize codec with ClassLoader parameter", e);
        } finally {
            classBodyBuf.release();
            stateBuf.release();
        }
    }

    /**
     * 根据 Worker 级迟到阈值判断调度任务是否应跳过本次执行。
     * <p>
     * 周期任务跳过后仍由调用方重调度，仅丢弃过期的一次执行。
     */
    private boolean skipAsLate(TaskParameters params) {
        if (taskLateThreshold <= 0 || !(params instanceof ScheduledParameters)) {
            return false;
        }
        long lateBy = System.currentTimeMillis() - ((ScheduledParameters) params).getStartTime();
        if (lateBy > taskLateThreshold) {
            log.debug("Task {} skipped: {} ms late, exceeds threshold of {} ms",
                    params.getRequestId(), lateBy, taskLateThreshold);
            return true;
        }
        return false;
    }

    /** 执行 Runnable；{@code removeTask} 控制 finish 时是否从 Redis 删除任务条目。 */
    public void executeRunnable(TaskParameters params, boolean removeTask) {
        if (skipAsLate(params)) {
            finish(params.getRequestId(), removeTask);
            return;
        }

        try {
            if (params.getRequestId() != null && !(params instanceof ScheduledParameters)) {
                RFuture<Long> future = renewRetryTime(params.getRequestId());
                try {
                    future.toCompletableFuture().get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            Runnable runnable = decode(params);
            runnable.run();
        } catch (RedissonShutdownException e) {
            throw e;
        } catch (RedisException e) {
            finish(params.getRequestId(), removeTask);
            throw e;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new IllegalArgumentException(e.getCause());
            }
        }

        finish(params.getRequestId(), removeTask);
    }
    
    /** 执行 Runnable 并在完成后移除任务。 */
    @Override
    public void executeRunnable(TaskParameters params) {
        executeRunnable(params, true);
    }

    /**
     * 任务结束时递减计数器；若归零且处于 shutdown 则转为 terminated 并发布通知。
     * <p>
     * {@code removeTask} 为 true 时同时从 tasks 哈希与过期 ZSET 删除条目。
     *
     * @param requestId 任务请求 ID
     */
    /** 执行 finish Lua：更新计数、清理调度项、必要时触发 terminated。 */
    void finish(String requestId, boolean removeTask) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        String script = "";
        if (removeTask) {
           script +=  "local scheduled = redis.call('zscore', KEYS[5], ARGV[3]);"
                    + "if scheduled == false then "
                        + "redis.call('hdel', KEYS[4], ARGV[3]); "
                        + "redis.call('zrem', KEYS[7], ARGV[3]); "
                    + "end;";
        }
        script += "redis.call('zrem', KEYS[5], 'ff:' .. ARGV[3]);" +
                  "if redis.call('decr', KEYS[1]) == 0 then "
                   + "redis.call('del', KEYS[1]);"
                    + "if redis.call('get', KEYS[2]) == ARGV[1] then "
                        + "redis.call('del', KEYS[6]);"
                        + "redis.call('set', KEYS[2], ARGV[2]);"
                        + "redis.call('publish', KEYS[3], ARGV[2]);"
                    + "end;"
                + "end;";  

        RFuture<Object> f = commandExecutor.evalWriteNoRetryAsync(tasksCounterName, StringCodec.INSTANCE, RedisCommands.EVAL_VOID,
                script,
                Arrays.asList(tasksCounterName, statusName, terminationTopicName, tasksName, schedulerQueueName, tasksRetryIntervalName, tasksExpirationTimeName),
                RedissonExecutorService.SHUTDOWN_STATE, RedissonExecutorService.TERMINATED_STATE, requestId);
        commandExecutor.get(f);
    }

}
