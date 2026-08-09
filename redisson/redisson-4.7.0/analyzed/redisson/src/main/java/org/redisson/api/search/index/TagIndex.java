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
 * 标签（TAG）字段索引配置接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface TagIndex extends FieldIndex {

    /**
     * 指定字段映射到的文档属性名。
     *
     * @param as 关联属性名
     * @return 当前标签索引选项
     */
    TagIndex as(String as);

    /**
     * 设置拆分标签值的分隔符，必须为单个字符。
     * <p>
     * 默认为 <code>,</code>。
     *
     * @param separator 分隔符
     * @return 当前标签索引选项
     */
    TagIndex separator(String separator);

    /**
     * 设置该属性值的排序模式。
     *
     * @param sortMode 排序模式
     * @return 当前标签索引选项
     */
    TagIndex sortMode(SortMode sortMode);

    /**
     * 保留标签的原始大小写；未设置时标签会转为小写。
     *
     * @return 当前标签索引选项
     */
    TagIndex caseSensitive();

    /**
     * 标记该属性不参与索引。
     *
     * @return 当前标签索引选项
     */
    TagIndex noIndex();

    /**
     * 为匹配后缀的词条保留后缀 trie 结构。
     *
     * @return 当前标签索引选项
     */
    TagIndex withSuffixTrie();

    /**
     * 是否索引空字符串标签值。
     *
     * @return 当前标签索引选项
     */
    TagIndex indexEmpty();

    /**
     * 索引缺少该属性的文档（视为缺失值参与检索）。
     *
     * @return 当前标签索引选项
     */
    TagIndex indexMissing();

}

