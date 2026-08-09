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
 * 使用 SVS-VAMANA 索引算法的向量字段配置接口。
 *
 * @author seakider
 *
 */
public interface SVSVamanaVectorIndex extends VectorTypeParam<SVSVamanaVectorOptionalArgs> {

    /**
     * 指定字段映射到的文档属性名。
     *
     * @param as 关联属性名
     * @return 当前向量索引选项
     */
    SVSVamanaVectorIndex as(String as);
}
