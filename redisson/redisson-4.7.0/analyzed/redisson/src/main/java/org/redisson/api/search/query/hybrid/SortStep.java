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
package org.redisson.api.search.query.hybrid;

import org.redisson.api.SortOrder;

/**
 * 混合查询构建流程中的结果排序配置步骤。
 * <p>
 * 继承 {@link HybridQueryArgs}，完成排序后可继续配置分页、分组等参数。
 *
 * @author Nikita Koksharov
 */
public interface SortStep extends HybridQueryArgs {

    /**
     * 按指定字段对最终结果排序。
     *
     * @param fieldName 排序字段名
     * @param order 排序方向
     * @return 混合查询参数，可继续配置
     */
    HybridQueryArgs sortBy(String fieldName, SortOrder order);

    /**
     * 禁用结果排序。
     *
     * @return 混合查询参数，可继续配置
     */
    HybridQueryArgs noSort();

}
