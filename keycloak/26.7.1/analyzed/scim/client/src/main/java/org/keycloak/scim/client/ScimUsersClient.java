package org.keycloak.scim.client;

import java.util.List;

import org.keycloak.scim.protocol.response.ListResponse;
import org.keycloak.scim.resource.user.User;

import static java.util.Objects.requireNonNull;

import static org.keycloak.scim.client.ResourceFilter.filter;

/**
 * SCIM 用户资源客户端。
 * <p>提供对 SCIM 用户（User）资源的查询、过滤与搜索操作。</p>
 */
public class ScimUsersClient extends AbstractScimResourceClient<User> {

    /**
     * 构造用户资源客户端。
     *
     * @param client 父级 {@link ScimClient} 实例
     */
    public ScimUsersClient(ScimClient client) {
        super(client, User.class);
    }

    /**
     * 获取所有用户（无过滤条件）。
     *
     * @return 包含全部用户的列表响应
     */
    public ListResponse<User> getAll() {
        return doFilter(filter());
    }

    /**
     * 获取所有用户，并可指定返回或排除的属性。
     *
     * @param attributes 要在响应中包含的属性名列表（可为 {@code null}）
     * @param excludedAttributes 要从响应中排除的属性名列表（可为 {@code null}）
     * @return 包含全部用户且属性已过滤的列表响应
     */
    public ListResponse<User> getAll(List<String> attributes, List<String> excludedAttributes) {
        return doFilter(filter(), attributes, excludedAttributes);
    }

    /**
     * 获取符合指定过滤条件的所有用户。
     *
     * @param filterExpression SCIM 过滤表达式（例如 {@code userName eq "john"}）
     * @return 包含匹配用户的列表响应
     */
    public ListResponse<User> getAll(String filterExpression) {
        requireNonNull(filterExpression, "filterExpression must not be null");
        return doFilter(new ResourceFilter() {
            @Override
            public String build() {
                return filterExpression;
            }
        });
    }

    /**
     * 通过 POST {@code /.search} 端点搜索用户。
     * <p>适用于可能超出 URL 长度限制的复杂过滤条件。</p>
     *
     * @param filterExpression SCIM 过滤表达式（例如 {@code userName eq "john"}）
     * @return 包含匹配用户的列表响应
     */
    public ListResponse<User> search(String filterExpression) {
        return this.search(filterExpression, null, null);
    }

    /**
     * 通过 POST {@code /.search} 端点搜索用户。
     * <p>适用于可能超出 URL 长度限制的复杂过滤条件。</p>
     *
     * @param filterExpression SCIM 过滤表达式（例如 {@code userName eq "john"}）
     * @param startIndex      可选，返回结果的起始索引（用于分页）；
     *                        若为 {@code null}，服务器将使用默认值（通常为 1）
     * @param count           可选，返回结果的最大数量（用于分页）；
     *                        若为 {@code null}，服务器将使用默认值
     * @return 包含匹配用户的列表响应
     */
    @SuppressWarnings("unchecked")
    public ListResponse<User> search(String filterExpression, Integer startIndex, Integer count) {
        return doPost(filterExpression, startIndex, count);
    }

    /** 关闭客户端；用户客户端无需释放额外资源。 */
    @Override
    public void close() throws Exception {

    }
}
