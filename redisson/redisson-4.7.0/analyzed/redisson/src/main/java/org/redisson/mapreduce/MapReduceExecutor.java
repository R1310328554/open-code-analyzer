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
package org.redisson.mapreduce;

import org.redisson.api.*;
import org.redisson.api.mapreduce.RCollator;
import org.redisson.api.mapreduce.RMapReduceExecutor;
import org.redisson.api.mapreduce.RReducer;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.CompletableFutureWrapper;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Redisson MapReduce 执行器抽象基类，实现 {@link org.redisson.api.mapreduce.RMapReduceExecutor}。
 * <p>
 * 负责校验 Mapper/Reducer 可序列化、创建唯一 resultMap 名称、
 * 通过 {@link CoordinatorTask} 异步提交作业，并在 execute 时批量读取并删除结果 Map。
 *
 * @author Nikita Koksharov
 *
 * @param <M> mapper type
 * @param <VIn> input value type
 * @param <KOut> output key type
 * @param <VOut> output value type
 */
abstract class MapReduceExecutor<M, VIn, KOut, VOut> implements RMapReduceExecutor<VIn, KOut, VOut> {

    /** Redisson 客户端。 */
    private final RedissonClient redisson;
    /** MapReduce 专用远程执行器。 */
    private final RExecutorService executorService;
    /** 本次作业自动生成的结果 Map Redis 键名。 */
    final String resultMapName;
    
    /** 源 Redis 对象的编解码器。 */
    final Codec objectCodec;
    /** 源 Redis 对象名称。 */
    final String objectName;
    /** 源 Redis 对象运行时 Class。 */
    final Class<?> objectClass;
    /** 异步命令执行器，用于阻塞 get 与超时调度。 */
    final CommandAsyncExecutor commandExecutor;


    /** 用户配置的 Reducer。 */
    RReducer<KOut, VOut> reducer;
    /** 用户配置的 Mapper。 */
    M mapper;
    /** 作业超时（毫秒）。 */
    long timeout;
    
    /** 从源 RObject 提取名称/编解码器，并生成唯一 resultMap 后缀。 */
    MapReduceExecutor(RObject object, RedissonClient redisson, CommandAsyncExecutor commandExecutor) {
        this.objectName = object.getName();
        this.objectCodec = object.getCodec();
        this.objectClass = object.getClass();

        this.redisson = redisson;
        UUID id = UUID.randomUUID();
        this.resultMapName = object.getName() + ":result:" + id;
        this.executorService = redisson.getExecutorService(RExecutorService.MAPREDUCE_NAME);
        this.commandExecutor = commandExecutor;
    }

    /**
     * 校验任务类可远程序列化：非 null、非匿名类、
     * 内部类必须为 static。
     */
    protected void check(Object task) {
        if (task == null) {
            throw new NullPointerException("Task is not defined");
        }
        if (task.getClass().isAnonymousClass()) {
            throw new IllegalArgumentException("Task can't be created using anonymous class");
        }
        if (task.getClass().isMemberClass()
                && !Modifier.isStatic(task.getClass().getModifiers())) {
            throw new IllegalArgumentException("Task class is an inner class and it should be static");
        }
    }
    
    /** 同步执行 MapReduce 并返回结果 Map。 */
    @Override
    public Map<KOut, VOut> execute() {
        return commandExecutor.get(executeAsync());
    }
    
    /**
     * 异步执行：Mapper 完成后批量 readAllMap 并 delete resultMap；
     * 若配置 timeout 则注册超时回调。
     */
    @Override
    public RFuture<Map<KOut, VOut>> executeAsync() {
        AtomicReference<RFuture<BatchResult<?>>> batchRef = new AtomicReference<>();

        RFuture<Void> mapperFuture = executeMapperAsync(resultMapName, null);
        CompletableFuture<Map<KOut, VOut>> f = mapperFuture.thenCompose(res -> {
            RBatch batch = redisson.createBatch();
            RMapAsync<KOut, VOut> resultMap = batch.getMap(resultMapName, objectCodec);
            RFuture<Map<KOut, VOut>> future = resultMap.readAllMapAsync();
            resultMap.deleteAsync();
            RFuture<BatchResult<?>> batchFuture = batch.executeAsync();
            batchRef.set(batchFuture);
            return future;
        }).toCompletableFuture();

        f.whenComplete((r, e) -> {
            if (f.isCancelled()) {
                if (batchRef.get() != null) {
                    batchRef.get().cancel(true);
                }
                mapperFuture.cancel(true);
            }
        });

        if (timeout > 0) {
            commandExecutor.getServiceManager().newTimeout(task -> {
                f.completeExceptionally(new MapReduceTimeoutException());
            }, timeout, TimeUnit.MILLISECONDS);
        }

        return new CompletableFutureWrapper<>(f);
    }

    @Override
    public void execute(String resultMapName) {
        commandExecutor.get(executeAsync(resultMapName));
    }
    
    @Override
    public RFuture<Void> executeAsync(String resultMapName) {
        return executeMapperAsync(resultMapName, null);
    }


    /** 构造 CoordinatorTask 并提交到 MapReduce 执行器。 */
    private <R> RFuture<R> executeMapperAsync(String resultMapName, RCollator<KOut, VOut, R> collator) {
        if (mapper == null) {
            throw new NullPointerException("Mapper is not defined");
        }
        if (reducer == null) {
            throw new NullPointerException("Reducer is not defined");
        }
        
        Callable<Object> task = createTask(resultMapName, (RCollator<KOut, VOut, Object>) collator);
        return (RFuture<R>) executorService.submit(task);
    }

    /** 由子类创建具体的 CoordinatorTask（绑定 Mapper 类型）。 */
    protected abstract Callable<Object> createTask(String resultMapName, RCollator<KOut, VOut, Object> collator);
    
    @Override
    public <R> R execute(RCollator<KOut, VOut, R> collator) {
        return commandExecutor.get(executeAsync(collator));
    }
    
    @Override
    public <R> RFuture<R> executeAsync(RCollator<KOut, VOut, R> collator) {
        check(collator);
        
        return executeMapperAsync(resultMapName, collator);
    }
    
}
