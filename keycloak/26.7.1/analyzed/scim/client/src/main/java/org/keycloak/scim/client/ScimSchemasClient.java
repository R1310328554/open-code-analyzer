package org.keycloak.scim.client;

import org.keycloak.scim.protocol.response.ListResponse;
import org.keycloak.scim.resource.schema.Schema;

/**
 * SCIM Schema 端点客户端。
 * <p>Schema 为只读资源，描述 SCIM 资源的结构定义。</p>
 */
public class ScimSchemasClient extends AbstractScimResourceClient<Schema> {

    /** 底层 SCIM 客户端引用。 */
    private final ScimClient scimClient;

    /**
     * 构造 Schema 客户端。
     *
     * @param scimClient 父级 {@link ScimClient} 实例
     */
    public ScimSchemasClient(ScimClient scimClient) {
        super(scimClient, Schema.class);
        this.scimClient = scimClient;
    }

    /** 获取泛型 {@link ListResponse}{@code <Schema>} 类型，供反序列化使用。 */
    @SuppressWarnings("unchecked")
    private Class<ListResponse<Schema>> getListResponseType() {
        return (Class<ListResponse<Schema>>) (Class<?>) ListResponse.class;
    }

    /**
     * 检索所有受支持的 SCIM Schema。
     *
     * @return 包含全部 Schema 的列表响应
     * @throws ScimClientException 请求失败时抛出
     */
    public ListResponse<Schema> getAll() {
        return scimClient.execute(doGet(""), getListResponseType());
    }

    /**
     * Schema 为只读资源，不支持创建。
     *
     * @throws UnsupportedOperationException 始终抛出
     */
    @Override
    public Schema create(Schema resource) {
        throw new UnsupportedOperationException("Schemas are read-only and cannot be created");
    }

    /**
     * Schema 为只读资源，不支持更新。
     *
     * @throws UnsupportedOperationException 始终抛出
     */
    @Override
    public Schema update(Schema resource) {
        throw new UnsupportedOperationException("Schemas are read-only and cannot be updated");
    }

    /**
     * Schema 为只读资源，不支持删除。
     *
     * @throws UnsupportedOperationException 始终抛出
     */
    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Schemas are read-only and cannot be deleted");
    }
}
