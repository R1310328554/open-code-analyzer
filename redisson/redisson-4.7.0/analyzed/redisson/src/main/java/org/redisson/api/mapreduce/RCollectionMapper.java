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

import java.io.Serializable;

/**
 * MapReduce Map 阶段任务，在 Redisson 集群各节点上并行执行。
 * <p>
 * 每个任务将输入元素映射为零个或多个键值对，写入共享的 {@link RCollector}；
 * 全部 Mapper 完成后由 {@link RReducer} 消费收集结果。
 * 
 * @author Nikita Koksharov
 *
 * @param <VIn> 输入元素类型
 * @param <KOut> 输出键类型
 * @param <VOut> 输出值类型
 */
public interface RCollectionMapper<VIn, KOut, VOut> extends Serializable {

    /**
     * 对集合中的每个源元素调用一次。
     * 
     * @param value 输入元素
     * @param collector 跨所有 Mapper 任务共享的收集器
     */
    void map(VIn value, RCollector<KOut, VOut> collector);
    
}
