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

import java.util.Map;

/**
 * 混合查询构建流程中的参数替换步骤。
 * <p>
 * 用于定义查询表达式中 $parameter_name 形式占位符的实际取值。
 *
 * @author Nikita Koksharov
 */
public interface ParamsStep {

    /**
     * 定义查询参数替换映射。
     * 参数可在搜索表达式中通过 $parameter_name 引用。
     *
     * @param params 参数名到值的映射
     * @return 排序配置步骤
     */
    SortStep params(Map<String, Object> params);

}
