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
package org.redisson.api.search.query;

import org.redisson.api.SortOrder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link org.redisson.api.RSearch#search(String, String, QueryOptions)} 的搜索查询选项。
 * <p>
 * 通过链式调用配置分页、排序、过滤器、高亮与摘要等行为。
 *
 * @author Nikita Koksharov
 *
 */
public class QueryOptions {

    private boolean noContent;
    private boolean verbatim;
    private boolean noStopwords;
    private boolean withScores;
    private boolean withSortKeys;
    private Integer slop;
    private Long timeout;
    private boolean inOrder;
    private String language;
    private String expander;
    private String scorer;
    private boolean explainScore;
    private String sortBy;
    private SortOrder sortOrder;
    private boolean withCount;
    private Integer offset;
    private Integer count;
    private Map<String, Object> params = Collections.emptyMap();
    private Integer dialect;
    private List<QueryFilter> filters = Collections.emptyList();
    private SummarizeOptions summarize;
    private HighlightOptions highlight;
    private List<String> inKeys = Collections.emptyList();
    private List<String> inFields = Collections.emptyList();
    private List<ReturnAttribute> returnAttributes = Collections.emptyList();

    protected QueryOptions() {
    }

    /** 创建默认查询选项。 */
    public static QueryOptions defaults() {
        return new QueryOptions();
    }

    /**
     * 设置查询过滤器列表。
     *
     * @param filters 过滤器数组
     * @return 当前选项
     */
    public QueryOptions filters(QueryFilter... filters) {
        this.filters = Arrays.asList(filters);
        return this;
    }

    /** 是否仅返回文档 ID 而不返回字段内容。 */
    public QueryOptions noContent(boolean noContent) {
        this.noContent = noContent;
        return this;
    }

    /** 是否禁用查询扩展，按字面语义匹配。 */
    public QueryOptions verbatim(boolean verbatim) {
        this.verbatim = verbatim;
        return this;
    }

    /** 是否禁用停用词过滤。 */
    public QueryOptions noStopwords(boolean noStopwords) {
        this.noStopwords = noStopwords;
        return this;
    }

    /** 是否在结果中包含相关性得分。 */
    public QueryOptions withScores(boolean withScores) {
        this.withScores = withScores;
        return this;
    }

    /** 是否在结果中包含排序键。 */
    public QueryOptions withSortKeys(boolean withSortKeys) {
        this.withSortKeys = withSortKeys;
        return this;
    }

    /** 设置词项间距容差（slop）。 */
    public QueryOptions slop(Integer slop) {
        this.slop = slop;
        return this;
    }

    /** 设置查询超时时间（毫秒）。 */
    public QueryOptions timeout(Long timeout) {
        this.timeout = timeout;
        return this;
    }

    /** 是否要求查询词按顺序出现。 */
    public QueryOptions inOrder(boolean inOrder) {
        this.inOrder = inOrder;
        return this;
    }

    /** 设置查询语言。 */
    public QueryOptions language(String language) {
        this.language = language;
        return this;
    }

    /** 设置查询扩展器名称。 */
    public QueryOptions expander(String expander) {
        this.expander = expander;
        return this;
    }

    /** 设置打分器名称。 */
    public QueryOptions scorer(String scorer) {
        this.scorer = scorer;
        return this;
    }

    /** 是否返回得分计算说明。 */
    public QueryOptions explainScore(boolean explainScore) {
        this.explainScore = explainScore;
        return this;
    }

    /** 设置排序字段。 */
    public QueryOptions sortBy(String sortBy) {
        this.sortBy = sortBy;
        return this;
    }

    /** 设置排序方向。 */
    public QueryOptions sortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    /** 是否在结果中包含匹配总数。 */
    public QueryOptions withCount(boolean withCount) {
        this.withCount = withCount;
        return this;
    }

    /**
     * 设置分页偏移与返回条数。
     *
     * @param offset 起始偏移
     * @param count 返回条数
     * @return 当前选项
     */
    public QueryOptions limit(int offset, int count) {
        this.offset = offset;
        this.count = count;
        return this;
    }

    /** 设置查询参数字典。 */
    public QueryOptions params(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    /** 设置查询方言版本。 */
    public QueryOptions dialect(Integer dialect) {
        this.dialect = dialect;
        return this;
    }

    /** 设置结果摘要选项。 */
    public QueryOptions summarize(SummarizeOptions summarize) {
        this.summarize = summarize;
        return this;
    }

    /** 设置结果高亮选项。 */
    public QueryOptions highlight(HighlightOptions highlight) {
        this.highlight = highlight;
        return this;
    }

    /** 限制搜索的文档键集合。 */
    public QueryOptions inKeys(List<String> inKeys) {
        this.inKeys = inKeys;
        return this;
    }

    /** 限制搜索的字段集合。 */
    public QueryOptions inFields(List<String> inFields) {
        this.inFields = inFields;
        return this;
    }

    /** 指定需返回的字段属性。 */
    public QueryOptions returnAttributes(ReturnAttribute... returnAttributes) {
        return returnAttributes(Arrays.asList(returnAttributes));
    }

    public QueryOptions returnAttributes(List<ReturnAttribute> returnAttributes) {
        this.returnAttributes = returnAttributes;
        return this;
    }

    public boolean isNoContent() {
        return noContent;
    }

    public boolean isVerbatim() {
        return verbatim;
    }

    public boolean isNoStopwords() {
        return noStopwords;
    }

    public boolean isWithScores() {
        return withScores;
    }

    public boolean isWithSortKeys() {
        return withSortKeys;
    }

    public Integer getSlop() {
        return slop;
    }

    public Long getTimeout() {
        return timeout;
    }

    public boolean isInOrder() {
        return inOrder;
    }

    public String getLanguage() {
        return language;
    }

    public String getExpander() {
        return expander;
    }

    public String getScorer() {
        return scorer;
    }

    public boolean isExplainScore() {
        return explainScore;
    }

    public String getSortBy() {
        return sortBy;
    }

    public boolean isWithCount() {
        return withCount;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getCount() {
        return count;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Integer getDialect() {
        return dialect;
    }

    public List<QueryFilter> getFilters() {
        return filters;
    }

    public SummarizeOptions getSummarize() {
        return summarize;
    }

    public HighlightOptions getHighlight() {
        return highlight;
    }

    public List<String> getInKeys() {
        return inKeys;
    }

    public List<String> getInFields() {
        return inFields;
    }

    public List<ReturnAttribute> getReturnAttributes() {
        return returnAttributes;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }
}
