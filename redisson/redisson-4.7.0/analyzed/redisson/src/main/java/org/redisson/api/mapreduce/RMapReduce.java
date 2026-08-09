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
package org.redisson.api.mapreduce;

import java.util.concurrent.TimeUnit;

/**
 * 对 Redis Map 中存储的大量数据执行 MapReduce 处理。
 * <p>
 * Mapper、Reducer 与 Collator 任务会在 Redisson 集群各节点上并行调度。
 * <p>
 * 使用示例：
 * 
 * <pre>
 *   public class WordMapper implements RMapper&lt;String, String, String, Integer&gt; {
 *
 *       public void map(String key, String value, RCollector&lt;String, Integer&gt; collector) {
 *           String[] words = value.split(&quot;[^a-zA-Z]&quot;);
 *           for (String word : words) {
 *               collector.emit(word, 1);
 *           }
 *       }
 *       
 *   }
 *   
 *   public class WordReducer implements RReducer&lt;String, Integer&gt; {
 *
 *       public Integer reduce(String reducedKey, Iterator&lt;Integer&gt; iter) {
 *           int sum = 0;
 *           while (iter.hasNext()) {
 *              Integer i = (Integer) iter.next();
 *              sum += i;
 *           }
 *           return sum;
 *       }
 *       
 *   }
 *
 *   public class WordCollator implements RCollator&lt;String, Integer, Integer&gt; {
 *
 *       public Integer collate(Map&lt;String, Integer&gt; resultMap) {
 *           int result = 0;
 *           for (Integer count : resultMap.values()) {
 *               result += count;
 *           }
 *           return result;
 *       }
 *       
 *   }
 *   
 *   RMap&lt;String, String&gt; map = redisson.getMap(&quot;myWords&quot;);
 *   
 *   Map&lt;String, Integer&gt; wordsCount = map.&lt;String, Integer&gt;mapReduce()
 *     .mapper(new WordMapper())
 *     .reducer(new WordReducer())
 *     .execute();
 *
 *   Integer totalCount = map.&lt;String, Integer&gt;mapReduce()
 *     .mapper(new WordMapper())
 *     .reducer(new WordReducer())
 *     .execute(new WordCollator());
 *
 * </pre>
 *
 * @author Nikita Koksharov
 *
 * @param <KIn> 输入键类型
 * @param <VIn> 输入值类型
 * @param <KOut> 输出键类型
 * @param <VOut> 输出值类型
 */
public interface RMapReduce<KIn, VIn, KOut, VOut> extends RMapReduceExecutor<VIn, KOut, VOut> {

    /**
     * 设置 MapReduce 流程的超时时间。
     * <code>0</code> 表示永不超时。
     * 
     * @param timeout 超时时长
     * @param unit 时间单位
     * @return 当前构建器实例
     */
    RMapReduce<KIn, VIn, KOut, VOut> timeout(long timeout, TimeUnit unit);

    /**
     * 配置 Map 阶段的 {@link RMapper}。
     * 
     * @param mapper Map 阶段使用的映射器
     * @return 当前构建器实例
     */
    RMapReduce<KIn, VIn, KOut, VOut> mapper(RMapper<KIn, VIn, KOut, VOut> mapper);
    
    /**
     * 配置 Reduce 阶段的 {@link RReducer}。
     * 
     * @param reducer Reduce 阶段使用的归约器
     * @return 当前构建器实例
     */
    RMapReduce<KIn, VIn, KOut, VOut> reducer(RReducer<KOut, VOut> reducer);
    
}
