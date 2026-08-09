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
package org.redisson.api.vector;

/**
 * {@link org.redisson.api.RVectorSet#getSimilar} 方法的参数对象。
 * <p>
 * 用于配置相似向量检索的查询向量、返回数量及过滤条件。
 *
 * @author Nikita Koksharov
 *
 */
public interface VectorSimilarArgs {

    /**
     * 指定待检索的元素名称。
     *
     * @param element 元素名称
     * @return 参数对象
     */
    static VectorSimilarArgs element(String element) {
        return new VectorSimilarParams(element);
    }

    /**
     * 以字节数组形式指定查询向量（32 位浮点值序列）。
     *
     * @param vector 向量字节数组
     * @return 参数对象
     */
    static VectorSimilarArgs vector(byte[] vector) {
        return new VectorSimilarParams(vector);
    }

    /**
     * 以浮点数数组形式指定查询向量。
     *
     * @param vector 双精度浮点数组
     * @return 参数对象
     */
    static VectorSimilarArgs vector(Double... vector) {
        return new VectorSimilarParams(vector);
    }

    /**
     * 指定返回的相似结果数量。
     *
     * @param count 返回条数
     * @return 参数对象
     */
    VectorSimilarArgs count(int count);

    /**
     * 指定距离阈值，仅返回距离不超过该值的结果。
     *
     * @param value 0 到 1 之间的浮点数
     * @return 参数对象
     */
    VectorSimilarArgs epsilon(double value);
    /**
     * 指定探索因子（EF）。
     *
     * @param value 探索因子值
     * @return 参数对象
     */
    VectorSimilarArgs explorationFactor(int value);

    /**
     * 指定过滤表达式以限制匹配元素。
     *
     * @param expression 过滤表达式
     * @return 参数对象
     */
    VectorSimilarArgs filter(String expression);

    /**
     * 指定过滤表达式的最大尝试次数。
     *
     * @param filterEffort 过滤尝试次数上限
     * @return 参数对象
     */
    VectorSimilarArgs filterEffort(int filterEffort);

    /**
     * 启用线性扫描以获取精确结果。
     *
     * @return 参数对象
     */
    VectorSimilarArgs useLinearScan();

    /**
     * 指定在主线程而非后台线程执行检索。
     *
     * @return 参数对象
     */
    VectorSimilarArgs useMainThread();
}
