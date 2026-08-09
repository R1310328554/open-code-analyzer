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
import org.redisson.api.mapreduce.RCollectionMapReduce;
import org.redisson.api.mapreduce.RCollectionMapper;
import org.redisson.api.mapreduce.RReducer;
import org.redisson.command.CommandAsyncExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 集合/列表的 MapReduce 流式 API 实现，
 * 实现 {@link org.redisson.api.mapreduce.RCollectionMapReduce}。
 * <p>
 * 链式配置 timeout、{@link org.redisson.api.mapreduce.RCollectionMapper}、
 * {@link org.redisson.api.mapreduce.RReducer} 后调用 execute 触发
 * {@link CollectionMapperTask} + {@link CoordinatorTask} 流水线。
 *
 * @author Nikita Koksharov
 *
 * @param <VIn> input value type
 * @param <KOut> output key type
 * @param <VOut> output value type
 */
public class RedissonCollectionMapReduce<VIn, KOut, VOut> extends MapReduceExecutor<RCollectionMapper<VIn, KOut, VOut>, VIn, KOut, VOut> 
                                                            implements RCollectionMapReduce<VIn, KOut, VOut> {

    /** 绑定源集合 RObject 与命令执行器。 */
    public RedissonCollectionMapReduce(RObject object, RedissonClient redisson, CommandAsyncExecutor commandExecutor) {
        super(object, redisson, commandExecutor);
    }
    
    /** 设置作业超时，支持链式调用。 */
    @Override
    public RCollectionMapReduce<VIn, KOut, VOut> timeout(long timeout, TimeUnit unit) {
        this.timeout = unit.toMillis(timeout);
        return this;
    }
    
    /** 配置集合 Mapper 并校验可序列化。 */
    @Override
    public RCollectionMapReduce<VIn, KOut, VOut> mapper(RCollectionMapper<VIn, KOut, VOut> mapper) {
        check(mapper);
        this.mapper = mapper;
        return this;
    }

    /** 配置 Reducer 并校验可序列化。 */
    @Override
    public RCollectionMapReduce<VIn, KOut, VOut> reducer(RReducer<KOut, VOut> reducer) {
        check(reducer);
        this.reducer = reducer;
        return this;
    }

    /** 组装 CollectionMapperTask 与 CoordinatorTask  Callable。 */
    @Override
    protected Callable<Object> createTask(String resultMapName, RCollator<KOut, VOut, Object> collator) {
        CollectionMapperTask<VIn, KOut, VOut> mapperTask = new CollectionMapperTask<VIn, KOut, VOut>(mapper, objectClass, objectCodec.getClass());
        return new CoordinatorTask<KOut, VOut>(mapperTask, reducer, objectName, resultMapName, objectCodec.getClass(), objectClass, collator, timeout, System.currentTimeMillis());
    }

}
