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
 * 数值（NUMERIC）字段索引配置接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface NumericIndex extends FieldIndex {

    /**
     * 指定字段映射到的文档属性名。
     *
     * @param as 关联属性名
     * @return 当前数值索引选项
     */
    NumericIndex as(String as);

    /**
     * 设置该属性值的排序模式。
     *
     * @param sortMode 排序模式
     * @return 当前数值索引选项
     */
    NumericIndex sortMode(SortMode sortMode);

    /**
     * 标记该属性不参与索引。
     *
     * @return 当前数值索引选项
     */
    NumericIndex noIndex();

    /**
     * 索引缺少该属性的文档（视为缺失值参与检索）。
     *
     * @return 当前数值索引选项
     */
    NumericIndex indexMissing();

}
