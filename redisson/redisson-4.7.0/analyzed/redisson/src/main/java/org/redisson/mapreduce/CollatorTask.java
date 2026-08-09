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

import java.util.concurrent.Callable;

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;
import org.redisson.api.mapreduce.RCollator;
import org.redisson.client.codec.Codec;
import org.redisson.misc.Injector;

/**
 * MapReduce Collator 阶段任务：读取 Mapper 输出的 {@link RMap}，调用 {@link RCollator#collate} 合并为最终结果。
 * <p>
 * 执行完毕后删除临时 result Map。
 *
 * @author Nikita Koksharov
 *
 * @param <KOut> key type
 * @param <VOut> value type
 * @param <R> result type
 */
public class CollatorTask<KOut, VOut, R> implements Callable<R> {

    /** 运行时注入的 Redisson 客户端（无参构造路径）。 */
    @RInject
    private RedissonClient redisson;
    
    /** 用户实现的合并逻辑。 */
    private RCollator<KOut, VOut, R> collator;
    
    /** Mapper 阶段写入的中间结果 Map 名称。 */
    private String resultMapName;
    /** 结果 Map 使用的 Codec 类（可序列化字段）。 */
    private Class<?> codecClass;
    
    /** {@link #call()} 中实例化后的 Codec。 */
    private Codec codec;
    
    /** 无参构造，供 Executor 反序列化后注入依赖。 */
    public CollatorTask() {
    }
    
    /** 本地构造并指定 collator 与中间 Map 参数。 */
    public CollatorTask(RedissonClient redisson, RCollator<KOut, VOut, R> collator, String resultMapName, Class<?> codecClass) {
        super();
        this.redisson = redisson;
        this.collator = collator;
        this.resultMapName = resultMapName;
        this.codecClass = codecClass;
    }

    /**
     * 实例化 Codec、注入 collator 依赖，读取 resultMap 执行 collate 后删除临时 Map。
     */
    @Override
    public R call() throws Exception {
        this.codec = (Codec) codecClass.getConstructor().newInstance();
        
        Injector.inject(collator, redisson);
        
        RMap<KOut, VOut> resultMap = redisson.getMap(resultMapName, codec);
        R result = collator.collate(resultMap);
        resultMap.delete();
        return result;
    }

}
