package org.keycloak.scim.resource.schema;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.models.Model;
import org.keycloak.models.ModelValidationException;
import org.keycloak.scim.resource.ResourceTypeRepresentation;
import org.keycloak.scim.resource.schema.attribute.Attribute;

import com.fasterxml.jackson.databind.JsonNode;

import static java.util.Optional.ofNullable;

/**
 * <p>表示 SCIM 资源类型 schema 的接口。</p>
 *
 * <p>Schema 是一组描述资源属性的元数据，用于校验 {@link ResourceTypeRepresentation} 并在 REST 层对象与 {@link Model} 之间双向映射属性。</p>
 * @param <M> Keycloak 领域模型类型
 * @param <R> SCIM 资源表示类型
 */
public interface ModelSchema<M extends Model, R extends ResourceTypeRepresentation> {

    /**
     * Schema 的 URN 标识，用于关联资源类型。
     *
     * @return schema URN
     */
    String getId();

    /** 返回 schema 的可读名称。 */
    String getName();

    /** 返回 schema 描述。 */
    String getDescription();

    /**
     * 返回本 schema 定义的属性映射，键为属性名，值为 {@link Attribute} 描述。
     *
     * @return 属性映射
     */
    Map<String, Attribute<M, R>> getAttributes();

    /**
     * 从 SCIM 表示填充 Keycloak 模型。
     *
     * @param model 待填充模型
     * @param representation 源 SCIM 表示
     */
    void populate(M model, R representation);

    /**
     * 从 Keycloak 模型填充 SCIM 表示。
     *
     * @param model 源模型
     * @param representation 待填充的 SCIM 表示
     */
    void populate(R representation, M model);

    /**
     * 按 attributes/excludedAttributes 过滤后，从模型填充 SCIM 表示。
     *
     * @param representation 待填充表示
     * @param model 源模型
     * @param attributes 包含属性列表（null 表示不过滤）
     * @param excludedAttributes 排除属性列表（null 表示不排除）
     */
    default void populate(R representation, M model, List<String> attributes, List<String> excludedAttributes) {
        populate(representation, model);
    }

    /**
     * 校验 SCIM 表示是否符合 schema，无效时抛出异常。
     *
     * @param representation 待校验表示
     * @throws ModelValidationException 校验失败
     */
    void validate(R representation) throws ModelValidationException;

    /**
     * 对模型执行 PATCH add 操作。
     *
     * @param model 目标模型
     * @param path 属性路径（可为 null 表示整资源）
     * @param value 追加值
     */
    default void add(M model, String path, JsonNode value) {
        throw new UnsupportedOperationException("Add operation is not supported for this schema");
    }

    /**
     * 对模型执行 PATCH remove 操作。
     *
     * @param resource SCIM 资源表示
     * @param model 目标模型
     * @param path 属性路径
     */
    default void remove(R resource, M model, String path) {
        throw new UnsupportedOperationException("Add operation is not supported for this schema");
    }

    /**
     * 对模型执行 PATCH replace 操作（先 remove 再 add）。
     *
     * @param resource SCIM 资源表示
     * @param model 目标模型
     * @param path 属性路径
     */
    default void replace(R resource, M model, String path, JsonNode value) {
        if (path != null) {
            remove(resource, model, path);
        }
        add(model, path, value);
    }

    /**
     * 是否为核心 schema（默认 true）。
     *
     * @return 核心 schema 返回 true
     */
    default boolean isCore() {
        return true;
    }

    /**
     * 按路径返回 {@link Attribute}，路径可为 null 或点分隔子属性（如 name.familyName）。
     *
     * @param path SCIM 属性路径
     * @return 匹配的 Attribute，无匹配返回 null
     */
    Attribute<M, R> getAttributeByPath(String path);

    /**
     * 是否为内部 schema（不通过 /Schemas 端点暴露）。
     *
     * @return 内部 schema 返回 true
     */
    default boolean isInternal() {
        return false;
    }

    /**
     * 判断请求的 schema 集合是否包含本 schema（核心 schema 始终支持）。
     *
     * @param schemas 请求的 schema URN 集合
     * @return 支持返回 true
     */
    default boolean supports(Set<String> schemas) {
        return isCore() || ofNullable(schemas).orElse(Set.of()).contains(getId());
    }
}
