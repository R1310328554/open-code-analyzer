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

import org.redisson.codec.JsonCodec;

/**
 * {@link org.redisson.api.RVectorSet#add} 方法的参数对象。
 * <p>
 * 支持配置向量数据、量化、探索因子及属性等选项。
 *
 * @author Nikita Koksharov
 *
 */
public interface VectorAddArgs {

    /**
     * 指定待添加的元素名称。
     *
     * @param name 元素名称
     * @return 参数对象
     */
    static ElementStep element(String name) {
        return new VectorAddParams(name);
    }

    /**
     * 向量数据配置步骤接口。
     */
    interface ElementStep {

        /**
         * 以字节数组形式定义向量（32 位浮点值 blob）。
         *
         * @param vector 向量字节数组
         * @return 参数对象
         */
        VectorAddArgs vector(byte[] vector);

        /**
         * 以双精度浮点数组形式定义向量。
         *
         * @param vector 向量浮点数组
         * @return 参数对象
         */
        VectorAddArgs vector(Double... vector);
    }

    /**
     * 设置随机投影值以降低向量维度。
     *
     * @param reduce 降维目标值
     * @return 参数对象
     */
    VectorAddArgs reduce(int reduce);

    /**
     * 启用 check-and-set 风格的添加执行方式。
     *
     * @return 参数对象
     */
    VectorAddArgs useCheckAndSet();

    /**
     * 指定向量量化类型。
     *
     * @param type 量化类型
     * @return 参数对象
     */
    VectorAddArgs quantization(QuantizationType type);

    /**
     * 设置探索因子（EF）。
     *
     * @param value 探索因子值
     * @return 参数对象
     */
    VectorAddArgs explorationFactor(int value);

    /**
     * 定义属性对象（以 JavaScript 对象形式序列化）。
     *
     * @param attributes 待序列化的属性对象
     * @return 参数对象
     */
    VectorAddArgs attributes(Object attributes, JsonCodec jsonCodec);

    /**
     * 设置每个节点与其他节点的最大连接数。
     *
     * @param maxConnections 最大连接数
     * @return 参数对象
     */
    VectorAddArgs maxConnections(int maxConnections);
}
