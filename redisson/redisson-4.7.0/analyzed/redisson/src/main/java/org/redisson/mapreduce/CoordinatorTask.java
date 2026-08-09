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

import org.redisson.Redisson;
import org.redisson.api.RExecutorService;
import org.redisson.api.RFuture;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;
import org.redisson.api.mapreduce.RCollator;
import org.redisson.api.mapreduce.RReducer;
import org.redisson.client.RedisException;
import org.redisson.client.codec.Codec;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * MapReduce 协调任务：在 MapReduce 执行器上编排 Map → Reduce → Collator 三阶段。
 * <p>
 * 1. 提交 {@link BaseMapperTask} 完成 Map；<br>
 * 2. 为每个 Worker 提交 {@link ReducerTask} 聚合分片中间结果；<br>
 * 3. 可选执行 {@link CollatorTask} 合并最终结果。<br>
 * 全程 respect timeout，超时抛出 {@link MapReduceTimeoutException}。
 *
 * @author Nikita Koksharov
 *
 * @param <KOut> output key
 * @param <VOut> output value
 */
public class CoordinatorTask<KOut, VOut> implements Callable<Object>, Serializable {

    private static final long serialVersionUID = 7559371478909848610L;

    /** 注入的 Redisson 客户端。 */
    @RInject
    protected RedissonClient redisson;
    
    /** Map 阶段任务（MapperTask 或 CollectionMapperTask）。 */
    private BaseMapperTask<KOut, VOut> mapperTask;
    /** 可选的最终 Collator，为 null 时 Reduce 结果即为输出 Map。 */
    private RCollator<KOut, VOut, Object> collator;
    /** Reduce 阶段用户逻辑。 */
    private RReducer<KOut, VOut> reducer;
    /** 源 Redis 对象名称（Map 或 Collection）。 */
    protected String objectName;
    /** 源对象运行时类型。 */
    protected Class<?> objectClass;
    /** 源对象编解码器 Class，Worker 端反射实例化。 */
    private Class<?> objectCodecClass;
    /** 最终结果写入的 Redis Map 名称。 */
    private String resultMapName;
    /** 整体超时（毫秒），0 表示无限等待。 */
    private long timeout;
    /** 任务开始时间戳，用于计算剩余超时。 */
    private long startTime;

    protected Codec codec;
    
    public CoordinatorTask() {
    }
    
    public CoordinatorTask(BaseMapperTask<KOut, VOut> mapperTask, RReducer<KOut, VOut> reducer, 
            String mapName, String resultMapName, Class<?> mapCodecClass, Class<?> objectClass,
            RCollator<KOut, VOut, Object> collator, long timeout, long startTime) {
        super();
        this.mapperTask = mapperTask;
        this.reducer = reducer;
        this.objectName = mapName;
        this.objectCodecClass = mapCodecClass;
        this.objectClass = objectClass;
        this.resultMapName = resultMapName;
        this.collator = collator;
        this.timeout = timeout;
        this.startTime = startTime;
    }

    /** 协调 MapReduce 全流程，返回 Collator 结果或 null。 */
    @Override
    public Object call() throws Exception {
        long timeSpent = System.currentTimeMillis() - startTime;
        if (isTimeoutExpired(timeSpent)) {
            throw new MapReduceTimeoutException();
        }
        
        this.codec = (Codec) objectCodecClass.getConstructor().newInstance();
        
        // 获取 MapReduce 专用执行器及活跃 Worker 数
        RScheduledExecutorService executor = redisson.getExecutorService(RExecutorService.MAPREDUCE_NAME);
        int workersAmount = executor.countActiveWorkers();
        
        // 为本次作业生成唯一 Collector 前缀
        UUID id = UUID.randomUUID();
        String collectorMapName = objectName + ":collector:" + id;

        mapperTask.setCollectorMapName(collectorMapName);
        mapperTask.setWorkersAmount(workersAmount);
        timeSpent = System.currentTimeMillis() - startTime;
        if (isTimeoutExpired(timeSpent)) {
            throw new MapReduceTimeoutException();
        }
        if (timeout > 0) {
            mapperTask.setTimeout(timeout - timeSpent);
        }

        mapperTask.addObjectName(objectName);
        // 异步提交 Map 任务
        RFuture<?> mapperFuture = executor.submitAsync(mapperTask);
        try {
            if (timeout > 0) {
                try {
                    mapperFuture.toCompletableFuture().get(timeout - timeSpent, TimeUnit.MILLISECONDS);
                } catch (ExecutionException e) {
                    throw ((Redisson) redisson).getCommandExecutor().convertException(e);
                } catch (TimeoutException e) {
                    mapperFuture.cancel(true);
                    throw new MapReduceTimeoutException();
                }
            }
            if (timeout == 0) {
                try {
                    mapperFuture.toCompletableFuture().join();
                } catch (CompletionException e) {
                    throw new RedisException(e.getCause());
                } catch (CancellationException e) {
                    // skip
                }
            }
        } catch (InterruptedException e) {
            mapperFuture.cancel(true);
            return null;
        }

        // 为每个分片提交 Reduce 子任务
        SubTasksExecutor reduceExecutor = new SubTasksExecutor(executor, startTime, timeout);
        for (int i = 0; i < workersAmount; i++) {
            String name = collectorMapName + ":" + i;
            Runnable runnable = new ReducerTask<>(name, reducer, objectCodecClass, resultMapName, timeout - timeSpent);
            reduceExecutor.submit(runnable);
        }

        if (!reduceExecutor.await()) {
            return null;
        }
        
        return executeCollator();
    }

    /** 若配置了 Collator，在本地或线程池中执行并最终合并 resultMap。 */
    private Object executeCollator() throws Exception {
        if (collator == null) {
            if (timeout > 0) {
                redisson.getMap(resultMapName).clearExpire();
            }
            return null;
        }
        
        Callable<Object> collatorTask = new CollatorTask<>(redisson, collator, resultMapName, objectCodecClass);
        long timeSpent = System.currentTimeMillis() - startTime;
        if (isTimeoutExpired(timeSpent)) {
            throw new MapReduceTimeoutException();
        }

        if (timeout > 0) {
            ExecutorService executor = ((Redisson) redisson).getServiceManager().getExecutor();
            java.util.concurrent.Future<?> collatorFuture = executor.submit(collatorTask);
            try {
                return collatorFuture.get(timeout - timeSpent, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                return null;
            } catch (TimeoutException e) {
                collatorFuture.cancel(true);
                throw new MapReduceTimeoutException();
            }
        } else {
            return collatorTask.call();
        }
    }

    /** 判断已耗时是否超过配置的超时阈值。 */
    private boolean isTimeoutExpired(long timeSpent) {
        return timeSpent > timeout && timeout > 0;
    }

}
