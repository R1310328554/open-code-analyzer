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
package org.redisson.api.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link org.redisson.api.RSearch#spellcheck(String, String, SpellcheckOptions)} 方法的拼写检查选项。
 * <p>
 * 配置 Levenshtein 编辑距离、查询方言及自定义词典的包含/排除词条。
 *
 * @author Nikita Koksharov
 *
 */
public final class SpellcheckOptions {

    private Integer distance;
    private Integer dialect;
    private List<String> includedTerms = Collections.emptyList();
    private List<String> excludedTerms = Collections.emptyList();
    private String excludedDictionary;
    private String includedDictionary;

    private SpellcheckOptions() {
    }

    /**
     * 返回默认拼写检查选项。
     *
     * @return 默认选项实例
     */
    public static SpellcheckOptions defaults() {
        return new SpellcheckOptions();
    }

    /**
     * 设置拼写建议的最大 Levenshtein 编辑距离，允许值为 1 至 4。
     * <p>
     * 默认值为 <code>1</code>
     *
     * @param distance 最大编辑距离
     * @return 当前选项对象
     */
    public SpellcheckOptions distance(Integer distance) {
        this.distance = distance;
        return this;
    }

    /**
     * 设置查询执行所使用的方言版本。
     *
     * @param dialect 方言版本号
     * @return 当前选项对象
     */
    public SpellcheckOptions dialect(Integer dialect) {
        this.dialect = dialect;
        return this;
    }

    /**
     * 指定自定义 <code>dictionary</code> 中需要包含的 <code>includedTerms</code> 词条。
     *
     * @param dictionary 自定义词典名
     * @param includedTerms 包含的词条
     * @return 当前选项对象
     */
    public SpellcheckOptions includedTerms(String dictionary, String... includedTerms) {
        this.includedDictionary = dictionary;
        this.includedTerms = Arrays.asList(includedTerms);
        return this;
    }

    /**
     * 指定自定义 <code>dictionary</code> 中需要排除的 <code>excludedTerms</code> 词条。
     *
     * @param dictionary 自定义词典名
     * @param excludedTerms 排除的词条
     * @return 当前选项对象
     */
    public SpellcheckOptions excludedTerms(String dictionary, String... excludedTerms) {
        this.excludedDictionary = dictionary;
        this.excludedTerms = Arrays.asList(excludedTerms);
        return this;
    }

    /**
     * 返回最大 Levenshtein 编辑距离。
     *
     * @return 编辑距离
     */
    public Integer getDistance() {
        return distance;
    }

    /**
     * 返回查询方言版本号。
     *
     * @return 方言版本
     */
    public Integer getDialect() {
        return dialect;
    }

    /**
     * 返回包含的词条列表。
     *
     * @return 包含词条
     */
    public List<String> getIncludedTerms() {
        return includedTerms;
    }

    /**
     * 返回排除的词条列表。
     *
     * @return 排除词条
     */
    public List<String> getExcludedTerms() {
        return excludedTerms;
    }

    /**
     * 返回排除词条所属的自定义词典名。
     *
     * @return 词典名
     */
    public String getExcludedDictionary() {
        return excludedDictionary;
    }

    /**
     * 返回包含词条所属的自定义词典名。
     *
     * @return 词典名
     */
    public String getIncludedDictionary() {
        return includedDictionary;
    }
}
