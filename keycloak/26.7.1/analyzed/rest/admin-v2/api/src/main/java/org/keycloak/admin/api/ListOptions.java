package org.keycloak.admin.api;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

/**
 * 客户端 Admin API v2 列表查询的通用选项（字段筛选、排序、过滤与分页）。
 * <p>
 * 对应 GET 请求的 {@code fields}、{@code sort}、{@code q}、{@code limit}、{@code offset} 查询参数，
 * 支持链式构建与 sort 表达式的解析。
 */
public class ListOptions {

    /** 响应中需包含的顶层字段集合，省略或为空则返回全部字段。 */
    @Parameter(description = "Set of fields to include in the response. Must be top-level fields. If omitted or empty, all fields will be populated.", 
            explode = Explode.FALSE, schema = @Schema(type = SchemaType.ARRAY, uniqueItems = true, implementation = String.class))
    @QueryParam("fields")
    protected String fields;

    /** 排序表达式，逗号分隔字段，可选 {@code |asc}/{@code |desc} 方向，默认升序。 */
    @Parameter(description = "Sort expression. Comma-separated fields with optional direction per field using | (e.g. displayName|desc,clientId). Default direction is asc.",
            schema = @Schema(type = SchemaType.STRING, defaultValue = "clientId"))
    @QueryParam("sort")
    protected String sort;

    /** SCIM 风格过滤表达式，如 {@code clientId eq "my-app" and enabled eq true}。 */
    @Parameter(description = "Filter expression using SCIM-like syntax, e.g. clientId eq \"my-app\" and enabled eq true")
    @QueryParam("q")
    protected String query;

    /** 返回结果数量上限，默认 100。 */
    @Parameter(description = "Maximum number of results to return. Defaults to 100.")
    @QueryParam("limit")
    protected Integer limit;

    /** 分页偏移量（从 0 起计），默认 0。 */
    @Parameter(description = "Index of the first result to return, counted from 0. Defaults to 0.")
    @QueryParam("offset")
    protected Integer offset;
    
    /** 解析后的排序选项缓存，避免重复解析 sort 字符串。 */
    private transient List<SortOption> parsedSort;

    public ListOptions fields(Set<String> fields) {
        this.setFields(fields);
        return this;
    }

    public ListOptions query(String query) {
        this.setQuery(query);
        return this;
    }

    public ListOptions limit(int limit) {
        this.setLimit(limit);
        return this;
    }

    public ListOptions sort(List<SortOption> sort) {
        this.setSort(sort);
        return this;
    }

    public ListOptions offset(int offset) {
        this.setOffset(offset);
        return this;
    }

    public Set<String> getFields() {
        if (fields == null) {
            return null;
        }
        if (fields.isEmpty()) {
            return Set.of();
        }
        return new LinkedHashSet<>(List.of(fields.split(",")));
    }

    public void setFields(Set<String> fields) {
        this.fields = fields == null ? null : fields.stream().collect(Collectors.joining(","));
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    /**
     * 解析并返回排序选项列表；sort 为空字符串时返回空列表，null 时返回 null。
     * 解析结果会被缓存。
     */
    public List<SortOption> getSort() {
        if (sort == null) {
            return null;
        }
        if (sort.isEmpty()) {
            return List.of();
        }
        if (parsedSort != null) {
            return parsedSort;
        }
        parsedSort = Arrays.stream(sort.split(","))
                .map(String::trim)
                .filter(segment -> !segment.isEmpty())
                .map(ListOptions::parseSortSegment)
                .collect(Collectors.toUnmodifiableList());
        if (parsedSort.isEmpty()) {
            throw new IllegalArgumentException("sort must specify at least one field");
        }
        return parsedSort;
    }

    public void setSort(List<SortOption> sort) {
        parsedSort = List.copyOf(sort);
        if (sort == null) {
            this.sort = null;
        } else if (sort.isEmpty()) {
            this.sort = "";
        } else {
            this.sort = sort.stream().map(SortOption::toQuerySegment).collect(Collectors.joining(","));
        }
    }

    /** 解析单个 sort 片段，格式为 {@code fieldName} 或 {@code fieldName|asc|desc}。 */
    private static SortOption parseSortSegment(String segment) {
        String[] parts = segment.split("\\|", 2);
        String fieldName = parts[0].trim();
        if (fieldName.isEmpty()) {
            throw new IllegalArgumentException("sort must specify at least one field");
        }
        ClientField field = ClientField.fromApiName(fieldName).orElseThrow(() ->
                new IllegalArgumentException(String.format("%s is not a sortable field", fieldName)));
        SortOrder order = parts.length == 1 ? SortOrder.ASC : parseSortOrder(parts[1].trim());
        return SortOption.of(field, order);
    }

    /** 解析排序方向，无效值抛出异常。 */
    private static SortOrder parseSortOrder(String value) {
        if (value.isEmpty()) {
            return SortOrder.ASC;
        }
        for (SortOrder order : SortOrder.values()) {
            if (order.name().equalsIgnoreCase(value)) {
                return order;
            }
        }
        throw new IllegalArgumentException("sort direction must be asc or desc");
    }
}
