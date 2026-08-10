package org.keycloak.scim.protocol.request;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SCIM 搜索请求（POST {@code /.search} 端点，RFC 7644 第 3.4.3 节）。
 * <p>允许客户端提交可能超出 URL 长度限制的复杂搜索条件。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class SearchRequest {

    /** 消息 Schema 集合。 */
    @JsonProperty("schemas")
    private Set<String> schemas = Set.of("urn:ietf:params:scim:api:messages:2.0:SearchRequest");

    /** 要在响应中包含的属性名列表。 */
    @JsonProperty("attributes")
    private List<String> attributes;

    /** 要从响应中排除的属性名列表。 */
    @JsonProperty("excludedAttributes")
    private List<String> excludedAttributes;

    /** SCIM 过滤表达式。 */
    @JsonProperty("filter")
    private String filter;

    /** 排序字段名。 */
    @JsonProperty("sortBy")
    private String sortBy;

    /** 排序方向（{@code ascending} 或 {@code descending}）。 */
    @JsonProperty("sortOrder")
    private String sortOrder;

    /** 分页起始索引（1-based）。 */
    @JsonProperty("startIndex")
    private Integer startIndex;

    /** 每页返回的最大条目数。 */
    @JsonProperty("count")
    private Integer count;

    // Getters and setters

    /** 返回消息 Schema 集合。 */
    public Set<String> getSchemas() {
        return schemas;
    }

    /** 设置消息 Schema 集合。 */
    public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }

    /** 返回要包含的属性列表。 */
    public List<String> getAttributes() {
        return attributes;
    }

    /** 设置要包含的属性列表。 */
    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    /** 返回要排除的属性列表。 */
    public List<String> getExcludedAttributes() {
        return excludedAttributes;
    }

    /** 设置要排除的属性列表。 */
    public void setExcludedAttributes(List<String> excludedAttributes) {
        this.excludedAttributes = excludedAttributes;
    }

    /** 返回 SCIM 过滤表达式。 */
    public String getFilter() {
        return filter;
    }

    /** 设置 SCIM 过滤表达式。 */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /** 返回排序字段。 */
    public String getSortBy() {
        return sortBy;
    }

    /** 设置排序字段。 */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /** 返回排序方向。 */
    public String getSortOrder() {
        return sortOrder;
    }

    /** 设置排序方向。 */
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    /** 返回分页起始索引。 */
    public Integer getStartIndex() {
        return startIndex;
    }

    /** 设置分页起始索引。 */
    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    /** 返回每页条目数。 */
    public Integer getCount() {
        return count;
    }

    /** 设置每页条目数。 */
    public void setCount(Integer count) {
        this.count = count;
    }

    /** 创建 {@link Builder} 以流式构建搜索请求。 */
    public static Builder builder() {
        return new Builder();
    }

    /** 流式构建 {@link SearchRequest} 的建造者。 */
    public static class Builder {

        /** 正在组装的搜索请求实例。 */
        private final SearchRequest searchRequest;

        private Builder() {
            this.searchRequest = new SearchRequest();
        }

        /** 设置要包含的属性列表。 */
        public Builder withAttributes(List<String> attributes) {
            searchRequest.setAttributes(attributes);
            return this;
        }

        /** 设置要排除的属性列表。 */
        public Builder withExcludedAttributes(List<String> excludedAttributes) {
            searchRequest.setExcludedAttributes(excludedAttributes);
            return this;
        }

        /** 设置 SCIM 过滤表达式。 */
        public Builder withFilter(String filter) {
            searchRequest.setFilter(filter);
            return this;
        }

        /** 设置排序字段。 */
        public Builder withSortBy(String sortBy) {
            searchRequest.setSortBy(sortBy);
            return this;
        }

        /** 设置排序方向。 */
        public Builder withSortOrder(String sortOrder) {
            searchRequest.setSortOrder(sortOrder);
            return this;
        }

        /** 设置分页起始索引。 */
        public Builder withStartIndex(Integer startIndex) {
            searchRequest.setStartIndex(startIndex);
            return this;
        }

        /** 设置每页条目数。 */
        public Builder withCount(Integer count) {
            searchRequest.setCount(count);
            return this;
        }

        /** 构建最终的 {@link SearchRequest}。 */
        public SearchRequest build() {
            return searchRequest;
        }
    }
}
