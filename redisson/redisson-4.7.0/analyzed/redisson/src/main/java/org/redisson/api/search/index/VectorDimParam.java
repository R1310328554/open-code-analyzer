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
 * 向量索引配置链中设置向量维度的阶段接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface VectorDimParam<T> {

    /**
     * 设置向量维度。
     *
     * @param value 向量维度
     * @return 距离度量配置阶段
     */
    VectorDistParam<T> dim(int value);

}
