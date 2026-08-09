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
 * {@link NumericIndex} 的参数实现。
 *
 * @author Nikita Koksharov
 *
 */
public final class NumericIndexParams implements NumericIndex {

    private SortMode sortMode;
    private boolean noIndex;
    private final String fieldName;
    private String as;
    private boolean indexMissing;

    NumericIndexParams(String name) {
        this.fieldName = name;
    }

    @Override
    public NumericIndexParams as(String as) {
        this.as = as;
        return this;
    }

    @Override
    public NumericIndexParams sortMode(SortMode sortMode) {
        this.sortMode = sortMode;
        return this;
    }

    @Override
    public NumericIndexParams noIndex() {
        noIndex = true;
        return this;
    }

    @Override
    public NumericIndexParams indexMissing() {
        this.indexMissing = true;
        return this;
    }

    /** 返回排序模式。 */
    public SortMode getSortMode() {
        return sortMode;
    }

    /** 是否标记为不索引。 */
    public boolean isNoIndex() {
        return noIndex;
    }

    /** 返回字段名。 */
    public String getFieldName() {
        return fieldName;
    }

    /** 返回映射的属性别名。 */
    public String getAs() {
        return as;
    }

    /** 是否索引缺失该属性的文档。 */
    public boolean isIndexMissing() {
        return indexMissing;
    }
}
