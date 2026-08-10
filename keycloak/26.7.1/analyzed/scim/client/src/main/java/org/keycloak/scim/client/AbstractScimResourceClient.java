package org.keycloak.scim.client;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.http.simple.SimpleHttpRequest;
import org.keycloak.scim.protocol.request.PatchRequest;
import org.keycloak.scim.protocol.request.SearchRequest;
import org.keycloak.scim.protocol.response.ErrorResponse;
import org.keycloak.scim.protocol.response.ListResponse;
import org.keycloak.scim.resource.ResourceTypeRepresentation;

import org.apache.http.HttpStatus;

import static java.util.Objects.requireNonNull;


/**
 * SCIM 资源类型客户端抽象基类，封装 CRUD 与过滤操作的通用 HTTP 逻辑。
 * @param <R> SCIM 资源类型表示类
 */
public abstract class AbstractScimResourceClient<R extends ResourceTypeRepresentation> implements AutoCloseable {

    /** 底层 SCIM 客户端。 */
    protected final ScimClient client;
    /** 资源类型的 Java 类。 */
    private final Class<R> resourceTypeClass;

    /** 构造资源客户端。 @param client SCIM 客户端 @param resourceType 资源类型 Class */
    public AbstractScimResourceClient(ScimClient client, Class<R> resourceType) {
        this.client = client;
        this.resourceTypeClass = resourceType;
    }

    /** 创建 SCIM 资源（POST）。 */
    public R create(R resource) {
        requireNonNull(resource, "SCIM resource must not be null");
        return client.execute(client.doPost(resourceTypeClass).json(resource), resourceTypeClass);
    }

    public R update(R resource) {
        return update(resource.getId(), resource);
    }

    /** 按 ID 全量更新 SCIM 资源（PUT）。 */
    public R update(String id, R resource) {
        requireNonNull(resource, "SCIM resource must not be null");
        return client.execute(client.doPut(resourceTypeClass, id)
                .json(resource), resourceTypeClass);
    }

    /** 按 ID 删除 SCIM 资源。 */
    public void delete(String id) {
        requireNonNull(id, "SCIM resource ID must not be null");
        client.execute(client.doDelete(resourceTypeClass, id));
    }

    public R get(String id) {
        return get(id, null, null);
    }

    public R get(String id, List<String> attributes) {
        return get(id, attributes, null);
    }

    /**
     * 按 ID 获取资源，可选 attributes/excludedAttributes 过滤返回字段。
     * 404 时返回 null。
     */
    public R get(String id, List<String> attributes, List<String> excludedAttributes) {
        requireNonNull(id, "SCIM resource ID must not be null");

        try {
            SimpleHttpRequest request = doGet("/" + id);

            Map<String, String> params = new HashMap<>();
            if (attributes != null && !attributes.isEmpty()) {
                params.put("attributes", String.join(",", attributes));
            }
            if (excludedAttributes != null && !excludedAttributes.isEmpty()) {
                params.put("excludedAttributes", String.join(",", excludedAttributes));
            }
            if (!params.isEmpty()) {
                request = request.params(params);
            }

            return client.execute(request, resourceTypeClass);
        } catch (ScimClientException scime) {
            ErrorResponse error = scime.getError();

            if (error != null) {
                if (HttpStatus.SC_NOT_FOUND == error.getStatusInt()) {
                    return null;
                }
            }

            throw scime;
        }
    }

    /** 对资源执行 PATCH 部分更新。 */
    public void patch(String id, PatchRequest request) {
        requireNonNull(request, "request must not be null");
        client.execute(client.doPatch(resourceTypeClass, id).json(request));
    }

    @SuppressWarnings("unchecked")
    protected ListResponse<R> doFilter(ResourceFilter filter) {
        return doFilter(filter, null, null);
    }

    @SuppressWarnings("unchecked")
    /** 使用 GET + filter 查询参数检索资源列表。 */
    protected ListResponse<R> doFilter(ResourceFilter filter, List<String> attributes, List<String> excludedAttributes) {
        SimpleHttpRequest request = doGet("");

        Map<String, String> params = new HashMap<>();
        String query = filter.build();
        if (!query.isEmpty()) {
            params.put("filter", query);
        }
        if (attributes != null && !attributes.isEmpty()) {
            params.put("attributes", String.join(",", attributes));
        }
        if (excludedAttributes != null && !excludedAttributes.isEmpty()) {
            params.put("excludedAttributes", String.join(",", excludedAttributes));
        }
        if (!params.isEmpty()) {
            request = request.params(params);
        }

        return client.execute(request, ListResponse.class);
    }

    protected SimpleHttpRequest doGet(String path) {
        return client.doGet(resourceTypeClass, path);
    }

    /**
     * 通过 POST /.search 端点搜索资源，适用于超出 URL 长度限制的复杂过滤器。
     *
     * @param filterExpression SCIM 过滤表达式（如 {@code userName eq "john"}）
     * @param startIndex 分页起始索引，null 时使用服务器默认值
     * @param count 返回条数上限，null 时使用服务器默认值
     * @return 匹配资源的列表响应
     */
    @SuppressWarnings("unchecked")
    public ListResponse<R> doPost(String filterExpression, Integer startIndex, Integer count) {
        SearchRequest searchRequest = SearchRequest.builder()
                .withFilter(filterExpression)
                .withStartIndex(startIndex)
                .withCount(count).build();
        return client.execute(client.doPost(resourceTypeClass, "/.search").json(searchRequest), ListResponse.class);
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
