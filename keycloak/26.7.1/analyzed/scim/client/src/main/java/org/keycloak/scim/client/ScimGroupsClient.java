package org.keycloak.scim.client;

import org.keycloak.scim.protocol.response.ListResponse;
import org.keycloak.scim.resource.group.Group;

import static java.util.Objects.requireNonNull;

/**
 * SCIM 组资源客户端。
 * <p>提供对 SCIM 组（Group）资源的查询与搜索操作。</p>
 */
public class ScimGroupsClient extends AbstractScimResourceClient<Group> {

    /**
     * 构造组资源客户端。
     *
     * @param client 父级 {@link ScimClient} 实例
     */
    public ScimGroupsClient(ScimClient client) {
        super(client, Group.class);
    }

    /**
     * 获取符合指定过滤条件的所有组。
     *
     * @param filterExpression SCIM 过滤表达式（例如 {@code displayName eq "mygroup"}）
     * @return 包含匹配组的列表响应
     */
    public ListResponse<Group> getAll(String filterExpression) {
        requireNonNull(filterExpression, "filterExpression must not be null");
        return doFilter(new ResourceFilter() {
            @Override
            public String build() {
                return filterExpression;
            }
        });
    }

    /**
     * 通过 POST {@code /.search} 端点搜索组。
     * <p>适用于可能超出 URL 长度限制的复杂过滤条件。</p>
     *
     * @param filterExpression SCIM 过滤表达式（例如 {@code displayName eq "Engineering"}）
     * @param startIndex      可选，返回结果的起始索引（用于分页）；
     *                        若为 {@code null}，服务器将使用默认值（通常为 1）
     * @param count           可选，返回结果的最大数量（用于分页）；
     *                        若为 {@code null}，服务器将使用默认值
     * @return 包含匹配组的列表响应
     */
    @SuppressWarnings("unchecked")
    public ListResponse<Group> search(String filterExpression, Integer startIndex, Integer count) {
        return doPost(filterExpression, startIndex, count);
    }


    /** 关闭客户端；组客户端无需释放额外资源。 */
    @Override
    public void close() {
    }
}
