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

import org.redisson.api.RObject;
import org.redisson.api.RedissonClient;
import org.redisson.api.mapreduce.RCollator;
import org.redisson.api.mapreduce.RMapReduce;
import org.redisson.api.mapreduce.RMapper;
import org.redisson.api.mapreduce.RReducer;
import org.redisson.command.CommandAsyncExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis Map 的 MapReduce 流式 API 实现，
 * 实现 {@link org.redisson.api.mapreduce.RMapReduce}。
 * <p>
 * 链式配置 timeout、{@link org.redisson.api.mapreduce.RMapper}、
 * {@link org.redisson.api.mapreduce.RReducer}，execute 时提交
 * {@link MapperTask} 经 {@link CoordinatorTask} 完成分布式计算。
 *
 * @author Nikita Koksharov
 *
 * @param <KIn> input key type
 * @param <VIn> input value type
 * @param <KOut> output key type
 * @param <VOut> output value type
 */
public class RedissonMapReduce<KIn, VIn, KOut, VOut> extends MapReduceExecutor<RMapper<KIn, VIn, KOut, VOut>, VIn, KOut, VOut> 
                                                        implements RMapReduce<KIn, VIn, KOut, VOut> {

    /** 绑定源 Map RObject。 */
    public RedissonMapReduce(RObject object, RedissonClient redisson, CommandAsyncExecutor commandExecutor) {
        super(object, redisson, commandExecutor);
    }

    /** 设置作业超时。 */
    @Override
    public RMapReduce<KIn, VIn, KOut, VOut> timeout(long timeout, TimeUnit unit) {
        this.timeout = unit.toMillis(timeout);
        return this;
    }

    /** 配置 Map Mapper。 */
    @Override
    public RMapReduce<KIn, VIn, KOut, VOut> mapper(RMapper<KIn, VIn, KOut, VOut> mapper) {
        check(mapper);
        this.mapper = mapper;
        return this;
    }

    /** 配置 Reducer。 */
    @Override
    public RMapReduce<KIn, VIn, KOut, VOut> reducer(RReducer<KOut, VOut> reducer) {
        check(reducer);
        this.reducer = reducer;
        return this;
    }

    /** 创建 MapperTask + CoordinatorTask  Callable。 */
    @Override
    protected Callable<Object> createTask(String resultMapName, RCollator<KOut, VOut, Object> collator) {
        MapperTask<KIn, VIn, KOut, VOut> mapperTask = new MapperTask<KIn, VIn, KOut, VOut>(mapper, objectClass, objectCodec.getClass());
        return new CoordinatorTask<KOut, VOut>(mapperTask, reducer, objectName, resultMapName, objectCodec.getClass(), objectClass, collator, timeout, System.currentTimeMillis());
    }

}
