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
 * 向量索引配置链中设置向量元素类型的阶段接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface VectorTypeParam<T> {

    /** 向量元素浮点精度类型。 */
    enum Type {FLOAT32, FLOAT64}

    /**
     * 设置向量元素类型。
     *
     * @param type 向量类型（32 位或 64 位浮点）
     * @return 向量维度配置阶段
     */
    VectorDimParam<T> type(Type type);

}
