package org.keycloak.scim.protocol.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.keycloak.scim.resource.ResourceTypeRepresentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * SCIM 列表响应体（RFC 7644 第 3.4.2 节）。
 * <p>封装分页查询或搜索操作返回的资源集合及元数据。</p>
 *
 * @param <T> 资源类型，须继承 {@link ResourceTypeRepresentation}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListResponse<T extends ResourceTypeRepresentation> {

    /** SCIM ListResponse 消息 Schema URN。 */
    public static final String SCHEMA = "urn:ietf:params:scim:api:messages:2.0:ListResponse";

    /** 消息 Schema 集合。 */
    @JsonProperty("schemas")
    private Set<String> schemas = Set.of(SCHEMA);

    /** 匹配条件的资源总数。 */
    @JsonProperty("totalResults")
    private Integer totalResults;

    /** 当前页的资源列表。 */
    @JsonProperty("Resources")
    @JsonDeserialize(using = ListResponseDeserializer.class)
    private List<T> resources;

    /** 分页起始索引（1-based）。 */
    @JsonProperty("startIndex")
    private Integer startIndex;

    /** 当前页返回的条目数。 */
    @JsonProperty("itemsPerPage")
    private Integer itemsPerPage;

    /** 返回消息 Schema 集合。 */
    public Set<String> getSchemas() {
        return schemas;
    }

    /** 设置消息 Schema 集合。 */
    public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }

    /** 返回匹配资源总数。 */
    public Integer getTotalResults() {
        return totalResults;
    }

    /** 设置匹配资源总数。 */
    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    /** 返回当前页资源列表。 */
    public List<T> getResources() {
        return resources;
    }

    /** 设置当前页资源列表。 */
    public void setResources(List<T> resources) {
        this.resources = resources;
    }

    /** 返回分页起始索引。 */
    public Integer getStartIndex() {
        return startIndex;
    }

    /** 设置分页起始索引。 */
    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    /** 返回当前页条目数。 */
    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    /** 设置当前页条目数。 */
    public void setItemsPerPage(Integer itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /** 向资源列表追加一条资源（必要时初始化列表）。 */
    public void addResource(T resource) {
        if (resources == null) {
            resources = new ArrayList<>();
        }
        resources.add(resource);
    }
}
