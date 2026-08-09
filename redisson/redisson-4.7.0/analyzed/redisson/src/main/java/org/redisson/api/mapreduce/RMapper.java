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
 * MapReduce 流程 Map 阶段在各 Redisson 节点上执行的映射任务。
 * 每个任务将输入键值对转换后的结果写入 {@link RCollector} 实例；
 * 全部 Mapper 任务完成后，由 {@link RReducer} 对收集结果进行归约。
 * 
 * @author Nikita Koksharov
 *
 * @param <KIn> 输入键类型
 * @param <VIn> 输入值类型
 * @param <KOut> 输出键类型
 * @param <VOut> 输出值类型
 */
public interface RMapper<KIn, VIn, KOut, VOut> extends Serializable {

    /**
     * 对 Map 数据源中的每条条目执行映射逻辑。
     * 
     * @param key 输入键
     * @param value 输入值
     * @param collector 在所有 Mapper 任务间共享的收集器
     */
    void map(KIn key, VIn value, RCollector<KOut, VOut> collector);
    
}
