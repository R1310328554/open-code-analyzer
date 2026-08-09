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
package org.redisson.api.search.profile;

import org.redisson.api.search.aggregate.AggregationResult;

import java.util.Map;

/**
 * {@link org.redisson.api.RSearch#profileAggregate(String, String, org.redisson.api.search.aggregate.AggregationOptions)}
 * 及其重载方法返回的结果对象。封装底层 {@code FT.AGGREGATE} 调用产生的 {@link AggregationResult}，
 * 以及 {@code FT.PROFILE} 命令收集的性能分析信息。
 *
 * @author Nikita Koksharov
 *
 */
public final class AggregateProfileResult {

    private final AggregationResult result;

    private final Map<String, Object> info;

    public AggregateProfileResult(AggregationResult result, Map<String, Object> info) {
        this.result = result;
        this.info = info;
    }

    /**
     * 返回底层 {@code FT.AGGREGATE} 调用的聚合结果。
     *
     * @return 聚合结果
     */
    public AggregationResult getResult() {
        return result;
    }

    /**
     * 返回 {@code FT.PROFILE} 命令收集的性能分析信息。
     * <p>
     * 映射的键为服务端返回的顶层分区名称。Redis Stack 8 及更高版本通常为
     * {@code "Shards"}（各分片分析数据列表）与 {@code "Coordinator"}（协调节点数据，如有）。
     * 各分片条目包含耗时（如 {@code "Total profile time"}、{@code "Parsing time"}、
     * {@code "Pipeline creation time"}）、{@code "Iterators profile"} 下的迭代器树，
     * 以及 {@code "Result processors profile"} 下各阶段结果处理器数据。
     * <p>
     * 值为原始列表或基本类型；嵌套结构与服务端返回的数组布局一致。具体键名与值形态
     * 取决于 Redis Stack 版本，可能随版本演进而变化。
     *
     * @return 性能分析信息
     */
    public Map<String, Object> getInfo() {
        return info;
    }

}
