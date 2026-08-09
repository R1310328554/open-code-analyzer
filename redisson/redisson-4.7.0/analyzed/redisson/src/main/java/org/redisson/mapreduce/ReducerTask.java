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

import org.redisson.api.RListMultimap;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;
import org.redisson.api.mapreduce.RReducer;
import org.redisson.client.codec.Codec;
import org.redisson.misc.Injector;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;

/**
 * MapReduce Reduce 阶段远程任务：读取单个分片 {@link org.redisson.api.RListMultimap}，
 * 对每个 key 调用 {@link org.redisson.api.mapreduce.RReducer} 聚合 value 列表，
 * 写入最终结果 {@link org.redisson.api.RMap}，完成后删除中间 multimap。
 *
 * @author Nikita Koksharov
 *
 * @param <KOut> key
 * @param <VOut> value
 */
public class ReducerTask<KOut, VOut> implements Runnable, Serializable {

    private static final long serialVersionUID = 3556632668150314703L;

    @RInject
    private RedissonClient redisson;
    
    /** 本分片 Collector multimap 的 Redis 键名。 */
    private String name;
    /** 聚合结果写入的目标 Map 名称。 */
    private String resultMapName;
    /** 用户 Reducer 逻辑。 */
    private RReducer<KOut, VOut> reducer;
    /** 编解码器 Class，Worker 端反射实例化。 */
    private Class<?> codecClass;
    private Codec codec;
    /** 结果 Map TTL（毫秒）。 */
    private long timeout;

    public ReducerTask() {
    }
    
    public ReducerTask(String name, RReducer<KOut, VOut> reducer, Class<?> codecClass, String resultMapName, long timeout) {
        this.name = name;
        this.reducer = reducer;
        this.resultMapName = resultMapName;
        this.codecClass = codecClass;
        this.timeout = timeout;
    }

    /** 遍历分片 multimap 各 key，reduce 后 put 到 resultMap。 */
    @Override
    public void run() {
        try {
            this.codec = (Codec) codecClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        
        Injector.inject(reducer, redisson);
        
        RMap<KOut, VOut> map = redisson.getMap(resultMapName);
        RListMultimap<KOut, VOut> multimap = redisson.getListMultimap(name, codec);
        // 对每个中间 key 聚合 value 列表
        for (KOut key : multimap.keySet()) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            List<VOut> values = multimap.get(key);
            VOut out = reducer.reduce(key, values.iterator());
            map.put(key, out);
        }
        // 为结果 Map 设置 TTL 并清理中间数据
        if (timeout > 0) {
            map.expire(Duration.ofMillis(timeout));
        }
        multimap.delete();
    }

}
