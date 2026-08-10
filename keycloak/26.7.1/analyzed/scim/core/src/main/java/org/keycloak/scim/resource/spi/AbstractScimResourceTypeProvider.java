package org.keycloak.scim.resource.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.Model;
import org.keycloak.models.ModelValidationException;
import org.keycloak.scim.protocol.ForbiddenException;
import org.keycloak.scim.protocol.request.PatchRequest.PatchOperation;
import org.keycloak.scim.protocol.request.SearchRequest;
import org.keycloak.scim.resource.ResourceTypeRepresentation;
import org.keycloak.scim.resource.schema.ModelSchema;

import com.fasterxml.jackson.databind.JsonNode;

import static java.util.function.Predicate.not;

import static org.keycloak.utils.StringUtil.isBlank;

/**
 * {@link ScimResourceTypeProvider} 的抽象基类，封装 CRUD、Patch 与权限校验的通用逻辑。
 *
 * @param <M> 底层 Keycloak 领域模型类型
 * @param <R> SCIM 资源类型表示类
 */
public abstract class AbstractScimResourceTypeProvider<M extends Model, R extends ResourceTypeRepresentation> implements ScimResourceTypeProvider<R> {

    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;
    /** 主 Schema 定义。 */
    private final ModelSchema<M, R> schema;
    /** Schema 扩展列表。 */
    private final List<ModelSchema<M, R>> schemaExtensions;
    /** 主 Schema 与扩展的合并列表。 */
    private final List<ModelSchema<M, R>> schemas;

    /**
     * 构造提供者，包含主 Schema 与扩展。
     *
     * @param session Keycloak 会话
     * @param schema 主 Schema
     * @param schemaExtensions Schema 扩展列表
     */
    public AbstractScimResourceTypeProvider(KeycloakSession session, ModelSchema<M, R> schema, List<ModelSchema<M, R>> schemaExtensions) {
        this.session = session;
        this.schema = schema;
        this.schemaExtensions = schemaExtensions;
        this.schemas = new ArrayList<>();
        this.schemas.add(schema);
        this.schemas.addAll(schemaExtensions);
    }

    /**
     * 构造提供者，无 Schema 扩展。
     *
     * @param session Keycloak 会话
     * @param schema 主 Schema
     */
    public AbstractScimResourceTypeProvider(KeycloakSession session, ModelSchema<M, R> schema) {
        this(session, schema, List.of());
    }

    @Override
    public R create(R resource) {
        if (!hasPermission(getRealmResourceType(), AdminPermissionsSchema.MANAGE)) {
            throw new ForbiddenException();
        }

        return onCreate(resource);
    }

    @Override
    public R update(R resource) {
        M model = getModel(resource.getId());

        if (!hasPermission(model, getRealmResourceType(), AdminPermissionsSchema.MANAGE)) {
            throw new ForbiddenException();
        }

        populate(model, resource);

        return onUpdate(model, resource);
    }

    @Override
    public R get(String id) {
        return get(id, null, null);
    }

    /**
     * 按 ID 获取资源，支持属性包含/排除过滤。
     *
     * @param id 资源标识符
     * @param attributes 需包含的属性列表（可为 {@code null}）
     * @param excludedAttributes 需排除的属性列表（可为 {@code null}）
     * @return 资源实例，不存在时返回 {@code null}
     */
    public R get(String id, List<String> attributes, List<String> excludedAttributes) {
        M model = getModel(id);

        if (model == null) {
            return null;
        }

        if (!hasPermission(model, getRealmResourceType(), AdminPermissionsSchema.VIEW)) {
            throw new ForbiddenException();
        }

        return createResourceTypeInstance(model, attributes, excludedAttributes);
    }

    @Override
    public Stream<R> getAll(SearchRequest searchRequest) {
        if (!canQuery()) {
            throw new ForbiddenException();
        }

        return getModels(searchRequest).map(m -> {
            try {
                return get(m.getId(), searchRequest.getAttributes(), searchRequest.getExcludedAttributes());
            } catch (ForbiddenException fe) {
                return null;
            }
        }).filter(Objects::nonNull);
    }

    @Override
    public boolean delete(String id) {
        M model = getModel(id);

        if (!hasPermission(model, getRealmResourceType(), AdminPermissionsSchema.MANAGE)) {
            throw new ForbiddenException();
        }

        return onDelete(id);
    }

    @Override
    public void patch(R existing, List<PatchOperation> operations) {
        Objects.requireNonNull(existing, "existing cannot be null");
        Objects.requireNonNull(operations, "operations cannot be null");
        M model = getModel(existing.getId());

        if (!hasPermission(model, getRealmResourceType(), AdminPermissionsSchema.MANAGE)) {
            throw new ForbiddenException();
        }

        for (PatchOperation operation : operations) {
            String op = operation.getOp();

            if (isBlank(op)) {
                throw new ModelValidationException("Missing operation for patch operation");
            }

            String path = operation.getPath();
            JsonNode value = operation.getValue();

            for (ModelSchema<M, R> schema : schemas) {
                switch (op.toLowerCase()) {
                    case "add" -> schema.add(model, path, value);
                    case "replace" -> schema.replace(existing, model, path, value);
                    case "remove" -> schema.remove(existing, model, path);
                    default -> throw new ModelValidationException("Unsupported patch operation " + op);
                }
            }
        }
    }

    @Override
    public String getSchema() {
        return schema.getId();
    }

    @Override
    public List<ModelSchema<M, R>> getSchemas() {
        return schemas;
    }

    @Override
    public List<String> getSchemaExtensions() {
        return schemaExtensions.stream().filter(not(ModelSchema::isInternal)).map(ModelSchema::getId).toList();
    }

    /** 创建资源的子类钩子方法。 */
    protected abstract R onCreate(R resource);

    /** 更新资源的子类钩子方法。 */
    protected abstract R onUpdate(M model, R resource);

    /** 删除资源的子类钩子方法。 */
    protected abstract boolean onDelete(String id);

    /** 按搜索条件获取模型流。 */
    protected abstract Stream<M> getModels(SearchRequest searchRequest);

    /** 按 ID 获取底层模型。 */
    protected abstract M getModel(String id);

    /** 返回 Realm 资源类型标识，用于细粒度权限校验。 */
    protected abstract String getRealmResourceType();

    /** 将 SCIM 资源表示的值写入底层模型。 */
    protected void populate(M model, R resource) {
        for (ModelSchema<M, R> schema : schemas) {
            if (schema.supports(resource.getSchemas())) {
                schema.populate(model, resource);
            }
        }
    }

    /** 从底层模型构造 SCIM 资源表示实例。 */
    protected R createResourceTypeInstance(M model, List<String> attributes, List<String> excludedAttributes) {
        try {
            R resource = getResourceType().getDeclaredConstructor().newInstance();

            for (ModelSchema<M, R> schema : schemas) {
                schema.populate(resource, model, attributes, excludedAttributes);
            }

            return resource;
        } catch (Exception e) {
            throw new RuntimeException("Could not create instance of resource type " + getResourceType(), e);
        }
    }

    /** 判断当前会话是否具备查询权限。 */
    private boolean canQuery() {
        return session.getContext().getPermissions().hasPermission(getRealmResourceType(), AdminPermissionsSchema.QUERY)
                || session.getContext().getPermissions().hasPermission(getRealmResourceType(), AdminPermissionsSchema.VIEW);
    }

    /** 判断对 Realm 资源类型是否拥有指定权限范围。 */
    private boolean hasPermission(String realmResourceType, String scope) {
        return session.getContext().getPermissions().hasPermission(realmResourceType, scope);
    }

    /**
     * 判断对特定模型实例是否拥有指定权限范围。
     *
     * @param model 领域模型实例
     * @param realmResourceType Realm 资源类型
     * @param scope 权限范围（如 VIEW、MANAGE）
     * @return 是否具备权限
     */
    protected boolean hasPermission(M model, String realmResourceType, String scope) {
        if (AdminPermissionsSchema.VIEW.equals(scope)) {
            return session.getContext().getPermissions().hasPermission(model, realmResourceType, scope);
        }

        return session.getContext().getPermissions().hasPermission(model, realmResourceType, scope) && isManageable(model);
    }

    /** 判断模型实例是否可管理（子类可覆盖以限制不可变资源）。 */
    protected boolean isManageable(M model) {
        return true;
    }
}
