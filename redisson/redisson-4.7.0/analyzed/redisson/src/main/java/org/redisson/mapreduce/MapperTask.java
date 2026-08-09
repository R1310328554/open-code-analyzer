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

import java.util.Map.Entry;

import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.mapreduce.RCollector;
import org.redisson.api.mapreduce.RMapper;
import org.redisson.client.codec.Codec;
import org.redisson.misc.Injector;

/**
 * Map 型 MapReduce 的 Mapper 远程任务：遍历 Redis Map/MapCache 条目，
 * 调用 {@link org.redisson.api.mapreduce.RMapper} 将 (key, value) 映射到
 * {@link Collector} 分区中间结果。
 *
 * @author Nikita Koksharov
 *
 * @param <KIn> input key type
 * @param <VIn> input key type
 * @param <KOut> output key type
 * @param <VOut> output key type
 */
public class MapperTask<KIn, VIn, KOut, VOut> extends BaseMapperTask<KOut, VOut> {

    private static final long serialVersionUID = 2441161019495880394L;
    
    /** 用户定义的 Map Mapper。 */
    protected RMapper<KIn, VIn, KOut, VOut> mapper;
    
    public MapperTask() {
    }
    
    public MapperTask(RMapper<KIn, VIn, KOut, VOut> mapper, Class<?> objectClass, Class<?> objectCodecClass) {
        super(objectClass, objectCodecClass);
        this.mapper = mapper;
    }

    /** 打开源 Map，逐 entry 调用 mapper.map(key, value, collector)。 */
    @Override
    public void run() {
        Codec codec;
        try {
            codec = (Codec) objectCodecClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        
        Injector.inject(mapper, redisson);
        // 创建按 Worker 数分区的中间结果收集器
        RCollector<KOut, VOut> collector = new Collector<KOut, VOut>(codec, redisson, collectorMapName, workersAmount, timeout);

        for (String objectName : objectNames) {
            RMap<KIn, VIn> map = null;
            // MapCache 带 TTL 语义，普通 Map 无
            if (RMapCache.class.isAssignableFrom(objectClass)) {
                map = redisson.getMapCache(objectName, codec);
            } else {
                map = redisson.getMap(objectName, codec);
            }
            
            for (Entry<KIn, VIn> entry : map.entrySet()) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                
                mapper.map(entry.getKey(), entry.getValue(), collector);
            }
        }
    }

}
