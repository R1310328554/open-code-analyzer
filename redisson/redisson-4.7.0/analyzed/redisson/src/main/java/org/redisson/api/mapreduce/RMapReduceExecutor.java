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

import java.util.Map;

import org.redisson.api.RFuture;

/**
 * MapReduce 流程的执行入口，提供同步与异步多种执行方式。
 * 
 * @author Nikita Koksharov
 *
 * @param <VIn> 输入值类型
 * @param <KOut> 输出键类型
 * @param <VOut> 输出值类型
 */
public interface RMapReduceExecutor<VIn, KOut, VOut> {

    /**
     * 在 Redisson 集群各节点上同步执行 MapReduce。
     * 
     * @return 归约后的键值映射
     */
    Map<KOut, VOut> execute();
    
    /**
     * 在 Redisson 集群各节点上异步执行 MapReduce。
     * 
     * @return 包含归约结果的 {@link RFuture}
     */
    RFuture<Map<KOut, VOut>> executeAsync();
    
    /**
     * 同步执行 MapReduce，并将结果写入指定名称的 Redis Map。
     * 
     * @param resultMapName 目标 Map 名称
     */
    void execute(String resultMapName);
    
    /**
     * 异步执行 MapReduce，并将结果写入指定名称的 Redis Map。
     * 
     * @param resultMapName 目标 Map 名称
     * @return 完成时无返回值的 {@link RFuture}
     */
    RFuture<Void> executeAsync(String resultMapName);
    
    /**
     * 同步执行 MapReduce，并通过 {@link RCollator} 将归约结果合并为单一对象。
     * 
     * @param <R> 归并结果类型
     * @param collator 应用于 Reduce 输出的归并器
     * @return 归并后的最终结果
     */
    <R> R execute(RCollator<KOut, VOut, R> collator);
    
    /**
     * 异步执行 MapReduce，并通过 {@link RCollator} 将归约结果合并为单一对象。
     * 
     * @param <R> 归并结果类型
     * @param collator 应用于 Reduce 输出的归并器
     * @return 包含归并结果的 {@link RFuture}
     */
    <R> RFuture<R> executeAsync(RCollator<KOut, VOut, R> collator);
    
}
