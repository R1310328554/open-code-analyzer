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
package org.redisson.api.search.aggregate;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RediSearch 聚合查询的基础选项配置。
 * <p>
 * 支持 LOAD、GROUPBY、SORTBY、APPLY、LIMIT、FILTER 等 FT.AGGREGATE 子句，
 * 由 {@link AggregationOptions} 继承并扩展游标分页能力。
 *
 * @author seakider
 *
 */
public class AggregationBaseOptions<T> {
    private boolean verbatim;
    private List<String> load = Collections.emptyList();
    private Long timeout;
    private boolean loadAll;
    private List<GroupParams> groupByParams = Collections.emptyList();
    private List<SortedField> sortedByFields = Collections.emptyList();
    private Integer sortedByMax;
    private boolean sortedByWithCount;
    private List<Expression> expressions = Collections.emptyList();
    private Integer offset;
    private Integer count;
    private String filter;
    protected boolean withCursor;
    protected Integer cursorCount;
    protected Duration cursorMaxIdle;
    private Map<String, Object> params = Collections.emptyMap();
    private Integer dialect;

    protected AggregationBaseOptions() {
    }

    /**
     * 设置是否以 verbatim 模式执行聚合（禁用查询扩展）。
     *
     * @param verbatim 是否 verbatim
     * @return 当前选项对象
     */
    public T verbatim(boolean verbatim) {
        this.verbatim = verbatim;
        return (T) this;
    }

    /**
     * 指定需要从文档中加载的属性字段。
     *
     * @param attributes 属性名列表
     * @return 当前选项对象
     */
    public T load(String... attributes) {
        this.load = Arrays.asList(attributes);
        return (T) this;
    }

    /**
     * 设置聚合查询超时时间（毫秒）。
     *
     * @param timeout 超时毫秒数
     * @return 当前选项对象
     */
    public T timeout(Long timeout) {
        this.timeout = timeout;
        return (T) this;
    }

    /**
     * 加载文档的全部属性字段。
     *
     * @return 当前选项对象
     */
    public T loadAll() {
        this.loadAll = true;
        return (T) this;
    }

    /**
     * 设置 GROUPBY 分组配置。
     *
     * @param groups 分组对象
     * @return 当前选项对象
     */
    public T groupBy(GroupBy... groups) {
        groupByParams = Arrays.stream(groups).map(g -> (GroupParams) g).collect(Collectors.toList());
        return (T) this;
    }

    /**
     * 设置 SORTBY 排序字段。
     *
     * @param fields 排序字段
     * @return 当前选项对象
     */
    public T sortBy(SortedField... fields) {
        sortedByFields = Arrays.asList(fields);
        return (T) this;
    }

    /**
     * 设置 SORTBY 排序字段及最大返回数。
     *
     * @param max 最大返回数
     * @param fields 排序字段
     * @return 当前选项对象
     */
    public T sortBy(int max, SortedField... fields) {
        sortedByMax = max;
        sortedByFields = Arrays.asList(fields);
        return (T) this;
    }

    /**
     * 设置 SORTBY 排序字段，并可附带分组计数。
     *
     * @param withCount 是否附带计数
     * @param fields 排序字段
     * @return 当前选项对象
     */
    public T sortBy(boolean withCount, SortedField... fields) {
        sortedByWithCount = withCount;
        sortedByFields = Arrays.asList(fields);
        return (T) this;
    }

    /**
     * 设置 SORTBY 排序字段、最大返回数及是否附带计数。
     *
     * @param max 最大返回数
     * @param withCount 是否附带计数
     * @param fields 排序字段
     * @return 当前选项对象
     */
    public T sortBy(int max, boolean withCount, SortedField... fields) {
        sortedByMax = max;
        sortedByWithCount = withCount;
        sortedByFields = Arrays.asList(fields);
        return (T) this;
    }

    /**
     * 设置 APPLY 表达式，对字段进行计算变换。
     *
     * @param expressions 表达式列表
     * @return 当前选项对象
     */
    public T apply(Expression... expressions) {
        this.expressions = Arrays.asList(expressions);
        return (T) this;
    }

    /**
     * 设置 LIMIT 分页参数。
     *
     * @param offset 起始偏移量
     * @param count 返回记录数
     * @return 当前选项对象
     */
    public T limit(int offset, int count) {
        this.offset = offset;
        this.count = count;
        return (T) this;
    }

    /**
     * 设置 FILTER 后置过滤表达式。
     *
     * @param filter 过滤表达式
     * @return 当前选项对象
     */
    public T filter(String filter) {
        this.filter = filter;
        return (T) this;
    }

    protected T cursorCount(int count) {
        cursorCount = count;
        return (T) this;
    }

    protected T withCursor() {
        withCursor = true;
        return (T) this;
    }

    protected T cursorMaxIdle(Duration duration) {
        cursorMaxIdle = duration;
        return (T) this;
    }

    /**
     * 设置查询参数字典，用于参数化聚合表达式。
     *
     * @param params 参数字典
     * @return 当前选项对象
     */
    public T params(Map<String, Object> params) {
        this.params = params;
        return (T) this;
    }

    /**
     * 设置查询方言版本号。
     *
     * @param dialect 方言版本
     * @return 当前选项对象
     */
    public T dialect(Integer dialect) {
        this.dialect = dialect;
        return (T) this;
    }

    /** 返回是否启用 verbatim 模式 */
    public boolean isVerbatim() {
        return verbatim;
    }

    /** 返回需要加载的属性字段列表 */
    public List<String> getLoad() {
        return load;
    }

    /** 返回聚合查询超时时间（毫秒） */
    public Long getTimeout() {
        return timeout;
    }

    /** 返回是否加载全部属性 */
    public boolean isLoadAll() {
        return loadAll;
    }

    /** 返回 GROUPBY 分组参数列表 */
    public List<GroupParams> getGroupByParams() {
        return groupByParams;
    }

    /** 返回 SORTBY 排序字段列表 */
    public List<SortedField> getSortedByFields() {
        return sortedByFields;
    }

    /** 返回 SORTBY 最大返回数 */
    public Integer getSortedByMax() {
        return sortedByMax;
    }

    /** 返回 SORTBY 是否附带分组计数 */
    public boolean isSortedByWithCount() {
        return sortedByWithCount;
    }

    /** 返回 APPLY 表达式列表 */
    public List<Expression> getExpressions() {
        return expressions;
    }

    /** 返回 LIMIT 起始偏移量 */
    public Integer getOffset() {
        return offset;
    }

    /** 返回 LIMIT 返回记录数 */
    public Integer getCount() {
        return count;
    }

    /** 返回 FILTER 过滤表达式 */
    public String getFilter() {
        return filter;
    }

    /** 返回是否启用游标模式 */
    public boolean isWithCursor() {
        return withCursor;
    }

    /** 返回游标每批记录数 */
    public Integer getCursorCount() {
        return cursorCount;
    }

    /** 返回游标最大空闲时间 */
    public Duration getCursorMaxIdle() {
        return cursorMaxIdle;
    }

    /** 返回查询参数字典 */
    public Map<String, Object> getParams() {
        return params;
    }

    /** 返回查询方言版本号 */
    public Integer getDialect() {
        return dialect;
    }
}
