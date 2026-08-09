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
package org.redisson.api.search.index;

/**
 * HNSW 向量索引的可选参数阶段接口。
 * <p>
 * 在设定向量类型、维度与距离度量后，可进一步配置图结构、搜索窗口与范围查询边界。
 *
 * @author Nikita Koksharov
 *
 */
public interface HNSWVectorOptionalArgs extends FieldIndex {

    /**
     * 设置向量索引的初始容量。
     *
     * @param value 初始向量容量
     * @return 当前向量选项
     */
    HNSWVectorOptionalArgs initialCapacity(int value);

    /**
     * 设置每个节点的最大出边数（HNSW 的 M 参数）。
     *
     * @param value 最大出边数
     * @return 当前向量选项
     */
    HNSWVectorOptionalArgs m(int value);

    /**
     * 设置建图时每个节点候选出边的最大数量（efConstruction）。
     *
     * @param value 候选出边数量上限
     * @return 当前向量选项
     */
    HNSWVectorOptionalArgs efConstruction(int value);

    /**
     * 设置 KNN 搜索过程中保留的候选集大小上限（efRuntime）。
     *
     * @param value 候选集大小上限
     * @return 当前向量选项
     */
    HNSWVectorOptionalArgs efRuntime(int value);

    /**
     * 设置范围查询的相对边界因子（epsilon）。
     *
     * @param value 相对边界因子
     * @return 当前向量选项
     */
    HNSWVectorOptionalArgs epsilon(double value);

}
