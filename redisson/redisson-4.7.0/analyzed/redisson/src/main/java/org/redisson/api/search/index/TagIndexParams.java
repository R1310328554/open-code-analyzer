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
 * {@link TagIndex} 的参数实现。
 *
 * @author Nikita Koksharov
 *
 */
public final class TagIndexParams implements TagIndex {

    private final String fieldName;
    private String as;
    private SortMode sortMode;
    private boolean noIndex;
    private boolean caseSensitive;
    private boolean withSuffixTrie;
    private String separator;
    private boolean indexEmpty;
    private boolean indexMissing;

    TagIndexParams(String name) {
        this.fieldName = name;
    }

    @Override
    public TagIndexParams as(String as) {
        this.as = as;
        return this;
    }

    @Override
    public TagIndexParams separator(String separator) {
        if (separator.length() != 1) {
            throw new IllegalArgumentException("Separator should be a single character");
        }
        this.separator = separator;
        return this;
    }

    @Override
    public TagIndexParams sortMode(SortMode sortMode) {
        this.sortMode = sortMode;
        return this;
    }

    @Override
    public TagIndexParams caseSensitive() {
        caseSensitive = true;
        return this;
    }

    @Override
    public TagIndexParams noIndex() {
        noIndex = true;
        return this;
    }

    @Override
    public TagIndexParams withSuffixTrie() {
        withSuffixTrie = true;
        return this;
    }

    @Override
    public TagIndexParams indexEmpty() {
        this.indexEmpty = true;
        return this;
    }

    @Override
    public TagIndexParams indexMissing() {
        this.indexMissing = true;
        return this;
    }

    /** 返回字段名。 */
    public String getFieldName() {
        return fieldName;
    }

    /** 返回映射的属性别名。 */
    public String getAs() {
        return as;
    }

    /** 返回排序模式。 */
    public SortMode getSortMode() {
        return sortMode;
    }

    /** 是否区分标签大小写。 */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /** 是否标记为不索引。 */
    public boolean isNoIndex() {
        return noIndex;
    }

    /** 是否启用后缀 trie。 */
    public boolean isWithSuffixTrie() {
        return withSuffixTrie;
    }

    /** 返回标签分隔符。 */
    public String getSeparator() {
        return separator;
    }

    /** 是否索引空标签值。 */
    public boolean isIndexEmpty() {
        return indexEmpty;
    }

    /** 是否索引缺失该属性的文档。 */
    public boolean isIndexMissing() {
        return indexMissing;
    }

}

