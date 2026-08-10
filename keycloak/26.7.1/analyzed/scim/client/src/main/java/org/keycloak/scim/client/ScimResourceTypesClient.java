package org.keycloak.scim.client;

import org.keycloak.scim.protocol.response.ListResponse;
import org.keycloak.scim.resource.resourcetype.ResourceType;

/**
 * SCIM 资源类型（ResourceType）端点客户端。
 * <p>用于检索服务器支持的 SCIM 资源类型定义。</p>
 */
public class ScimResourceTypesClient {

    /** 父级 SCIM 客户端。 */
    private final ScimClient client;

    /**
     * 构造资源类型客户端。
     *
     * @param client 父级 {@link ScimClient} 实例
     */
    public ScimResourceTypesClient(ScimClient client) {
        this.client = client;
    }

    /**
     * 获取所有 SCIM 资源类型。
     *
     * @return 包含全部资源类型的列表响应
     */
    @SuppressWarnings("unchecked")
    public ListResponse<ResourceType> getAll() {
        return client.execute(client.doGet(ResourceType.class), ListResponse.class);
    }
}
