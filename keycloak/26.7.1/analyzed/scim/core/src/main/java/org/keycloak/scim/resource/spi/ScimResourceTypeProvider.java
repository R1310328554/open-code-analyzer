package org.keycloak.scim.resource.spi;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.Model;
import org.keycloak.provider.Provider;
import org.keycloak.scim.protocol.request.PatchRequest.PatchOperation;
import org.keycloak.scim.protocol.request.SearchRequest;
import org.keycloak.scim.resource.ResourceTypeRepresentation;
import org.keycloak.scim.resource.schema.ModelSchema;

/**
 * SCIM 资源类型提供者接口。
 *
 * <p>负责资源类型的完整生命周期，包括校验、创建、更新、检索与删除。
 * 注册后，该提供者将自动暴露于 SCIM API。</p>
 *
 * <p>{@link ScimResourceTypeProvider} 主要负责在 SCIM 资源表示与底层模型之间映射值，
 * 并在管理资源实例时强制执行资源类型及其对应模型的规则。</p>
 */
public interface ScimResourceTypeProvider<R extends ResourceTypeRepresentation> extends Provider {

    /** 默认最大返回结果数。 */
    public static final int DEFAULT_MAX_RESULTS = 100;

    /**
     * 返回此提供者管理的资源类型名称。
     *
     * @return 资源类型名称
     */
    default String getName() {
        return getResourceType().getSimpleName();
    }

    /**
     * 返回此提供者管理的资源类型的人类可读描述。
     *
     * @return 资源类型描述
     */
    default String getDescription() {
        return getName();
    }

    /**
     * 返回此提供者管理的资源类型的 Schema URI。
     *
     * @return 资源类型的 Schema URI
     */
    String getSchema();

    /** 返回所有 {@link ModelSchema} 定义（含扩展）。 */
    <M extends Model> List<ModelSchema<M, R>> getSchemas();

    /**
     * 返回此提供者管理的资源类型的 Schema 扩展 URI 列表。
     *
     * @return Schema 扩展 URI 列表
     */
    default List<String> getSchemaExtensions() {
        return List.of();
    }

    /**
     * 返回此提供者管理的 {@link ResourceTypeRepresentation} 类型。
     *
     * @return 资源类型表示类
     */
    Class<R> getResourceType();

    /**
     * 创建此类型的新资源。校验通过后调用，应持久化资源并返回包含生成标识符的实例。
     *
     * @param resource 待创建的资源
     * @return 已创建的资源
     */
    R create(R resource);

    /**
     * 更新此类型的现有资源。校验通过后调用，应持久化更新并返回最新实例。
     *
     * @param resource 待更新的资源
     * @return 已更新的资源
     */
    R update(R resource);

    /**
     * 按标识符检索此类型的资源。资源不存在时返回 {@code null}。
     *
     * @param id 资源标识符
     * @return 对应资源，或 {@code null}
     */
    R get(String id);

    /**
     * 按标识符检索资源，并根据 {@code attributes} 与 {@code excludedAttributes} 过滤返回属性。
     *
     * @param id 资源标识符
     * @param attributes 需包含的属性列表（{@code null} 表示不过滤）
     * @param excludedAttributes 需排除的属性列表（{@code null} 表示不排除）
     * @return 对应资源，或 {@code null}
     */
    default R get(String id, List<String> attributes, List<String> excludedAttributes) {
        return get(id);
    }

    /**
     * 检索匹配搜索条件的所有资源。
     *
     * @param searchRequest 包含过滤条件等参数的搜索请求
     * @return 匹配资源的流
     */
    Stream<R> getAll(SearchRequest searchRequest);

    /**
     * 统计匹配搜索条件的资源总数。
     *
     * @param searchRequest 包含过滤条件等参数的搜索请求
     * @return 匹配资源总数
     */
    Long count(SearchRequest searchRequest);

    /**
     * 按标识符删除此类型的资源。
     *
     * @param id 资源标识符
     * @return 删除成功返回 {@code true}，未找到或无法删除返回 {@code false}
     */
    boolean delete(String id);

    /** 对资源执行 Patch 操作（默认不支持，子类可覆盖）。 */
    default void patch(R existing, List<PatchOperation> operations) {
        throw new UnsupportedOperationException("Add operation is not supported for resource type " + getName());
    }

    /**
     * 返回此 SCIM 资源类型对应的管理事件 {@link ResourceType}。
     * <p>默认将 {@link #getName()} 转为大写并匹配已知枚举值；子类可覆盖以映射不同值。</p>
     *
     * @return 管理事件资源类型
     */
    default ResourceType getAdminEventResourceType() {
        String name = getName().toUpperCase();
        return Arrays.stream(ResourceType.values())
                .filter(rt -> rt.name().equals(name))
                .findFirst()
                .orElse(ResourceType.CUSTOM);
    }
}
