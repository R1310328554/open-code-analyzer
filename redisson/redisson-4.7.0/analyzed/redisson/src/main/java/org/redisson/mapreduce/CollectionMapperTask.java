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

import org.redisson.api.RLexSortedSet;
import org.redisson.api.RList;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RSetCache;
import org.redisson.api.RSortedSet;
import org.redisson.api.mapreduce.RCollectionMapper;
import org.redisson.api.mapreduce.RCollector;
import org.redisson.client.codec.Codec;
import org.redisson.misc.Injector;

/**
 * 集合型 MapReduce 的 Mapper 远程任务：遍历 Redis 集合/列表元素，
 * 调用 {@link org.redisson.api.mapreduce.RCollectionMapper} 将每条记录映射到
 * {@link Collector} 分区中间结果。
 * <p>
 * 支持 {@link org.redisson.api.RSet}、{@link org.redisson.api.RList}、
 * 各类 SortedSet 等；由 {@link CoordinatorTask} 异步提交到 MapReduce 执行器。
 *
 * @author Nikita Koksharov
 *
 * @param <VIn> input value type
 * @param <KOut> output key type
 * @param <VOut> output value type
 */
public class CollectionMapperTask<VIn, KOut, VOut> extends BaseMapperTask<KOut, VOut> {

    private static final long serialVersionUID = -2634049426877164580L;
    
    /** 用户定义的集合 Mapper，通过 {@link Injector} 注入 RedissonClient。 */
    RCollectionMapper<VIn, KOut, VOut> mapper;
    
    public CollectionMapperTask() {
    }
    
    /** 构造集合 Mapper 任务，绑定 Mapper 实例与源集合类型/编解码器。 */
    public CollectionMapperTask(RCollectionMapper<VIn, KOut, VOut> mapper, Class<?> objectClass, Class<?> objectCodecClass) {
        super(objectClass, objectCodecClass);
        this.mapper = mapper;
    }

    /**
     * 执行 Map 阶段：按 objectClass 打开对应 Redis 集合，逐元素调用 mapper.map。
     * 线程中断时提前退出；中间结果写入 collectorMapName 对应的分区 Collector。
     */
    @Override
    public void run()  {
        // 反射实例化编解码器（任务序列化后需在 Worker 端重建）
        Codec codec;
        try {
            codec = (Codec) objectCodecClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        
        // 向 Mapper 注入 RedissonClient（@RInject）
        Injector.inject(mapper, redisson);

        for (String objectName : objectNames) {
            // 按集合具体类型获取可迭代视图
            Iterable<VIn> collection = null;
            if (RSetCache.class.isAssignableFrom(objectClass)) {
                collection = redisson.getSetCache(objectName, codec);
            } else if (RSet.class.isAssignableFrom(objectClass)) {
                collection = redisson.getSet(objectName, codec);
            } else if (RSortedSet.class.isAssignableFrom(objectClass)) {
                collection = redisson.getSortedSet(objectName, codec);
            } else if (RScoredSortedSet.class.isAssignableFrom(objectClass)) {
                collection = redisson.getScoredSortedSet(objectName, codec);
            } else if (RLexSortedSet.class.isAssignableFrom(objectClass)) {
                collection = (Iterable<VIn>) redisson.getLexSortedSet(objectName);
            } else if (RList.class.isAssignableFrom(objectClass)) {
                collection = redisson.getList(objectName, codec);
            } else {
                throw new IllegalStateException("Unable to work with " + objectClass);
            }
            
            // 为本 Worker 创建分区 Collector，按 hash 将 emit 写入不同分片
            RCollector<KOut, VOut> collector = new Collector<KOut, VOut>(codec, redisson, collectorMapName, workersAmount, timeout);
            
            for (VIn value : collection) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                
                mapper.map(value, collector);
            }
        }
    }

}
