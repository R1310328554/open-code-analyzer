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
 * 全文（TEXT）字段索引配置接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface TextIndex extends FieldIndex {

    /**
     * 指定字段映射到的文档属性名。
     *
     * @param as 关联属性名
     * @return 当前文本索引选项
     */
    TextIndex as(String as);

    /**
     * 设置该属性值的排序模式。
     *
     * @param sortMode 排序模式
     * @return 当前文本索引选项
     */
    TextIndex sortMode(SortMode sortMode);

    /**
     * 索引时禁用词干提取（stemming）。
     *
     * @return 当前文本索引选项
     */
    TextIndex noStem();

    /**
     * 标记该属性不参与索引。
     *
     * @return 当前文本索引选项
     */
    TextIndex noIndex();

    /**
     * 保留后缀字典树，以支持后缀匹配检索。
     *
     * @return 当前文本索引选项
     */
    TextIndex withSuffixTrie();

    /**
     * 配置语音匹配算法及语言，用于搜索结果匹配。
     *
     * @param matcher 语音匹配器（算法与语言）
     * @return 当前文本索引选项
     */
    TextIndex phonetic(PhoneticMatcher matcher);

    /**
     * 设置相关性评分权重，用于计算结果准确度时体现该字段的重要程度。
     *
     * @param weight 权重乘数
     * @return 当前文本索引选项
     */
    TextIndex weight(Double weight);

    /**
     * 索引空字符串值。
     *
     * @return 当前文本索引选项
     */
    TextIndex indexEmpty();

    /**
     * 索引缺少该属性的文档（视为缺失值参与检索）。
     *
     * @return 当前文本索引选项
     */
    TextIndex indexMissing();

}
