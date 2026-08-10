package org.keycloak.scim.model.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.Model;
import org.keycloak.models.ModelException;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.scim.model.config.ServiceProviderConfigResourceTypeProvider;
import org.keycloak.scim.model.resourcetype.ResourceTypeProviderFactory;
import org.keycloak.scim.protocol.ForbiddenException;
import org.keycloak.scim.protocol.request.SearchRequest;
import org.keycloak.scim.resource.Scim;
import org.keycloak.scim.resource.schema.ModelSchema;
import org.keycloak.scim.resource.schema.Schema;
import org.keycloak.scim.resource.schema.Schema.Attribute;
import org.keycloak.scim.resource.spi.ScimResourceTypeProvider;

import static org.keycloak.scim.resource.Scim.hasDiscoveryEndpointPermission;

/**
 * SCIM Schema 资源提供者，通过 /Schemas 端点向客户端暴露支持的 schema。
 * <p>
 * Schema 为只读资源，描述 SCIM 资源的属性结构。本实现支持：
 * - 内置核心 schema（User、Group）
 * - 内置扩展 schema（EnterpriseUser）
 * - 基于用户配置文件的自定义扩展 schema（规划中）
 */
public class SchemaResourceTypeProvider implements ScimResourceTypeProvider<Schema> {

    /** schema URN 到 {@link Schema} 表示的缓存。 */
    private final Map<String, Schema> schemas = new HashMap<>();
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    public SchemaResourceTypeProvider(KeycloakSession session) {
        this.session = session;
        initializeSchemas();
    }

    /** 启动时从各资源类型 provider 收集 ModelSchema 并构建 Schema 表示。 */
    private void initializeSchemas() {
        Stream<ProviderFactory> schemas = session.getKeycloakSessionFactory().getProviderFactoriesStream(ScimResourceTypeProvider.class);

        schemas.filter(providerFactory -> !(providerFactory instanceof SchemaResourceTypeProviderFactory
                        || providerFactory instanceof ResourceTypeProviderFactory
                        || providerFactory instanceof ServiceProviderConfigResourceTypeProvider)
                ).flatMap((Function<ProviderFactory, Stream<ModelSchema>>) factory -> {
                    ScimResourceTypeProvider provider = session.getProvider(ScimResourceTypeProvider.class, factory.getId());
                    return provider.getSchemas().stream();
                }).forEach(this::buildSchema);
    }

    /** 将 {@link ModelSchema} 转换为 Discovery {@link Schema}，嵌套子属性。 */
    private void buildSchema(ModelSchema<?, ?> modelSchema) {
        Schema rep = new Schema();
        rep.setId(modelSchema.getId());
        rep.setName(modelSchema.getName());
        rep.setDescription(modelSchema.getDescription());

        // 收集顶层属性，将子属性嵌套于父属性下
        Map<String, Attribute> topLevelAttributes = new HashMap<>();

        for (org.keycloak.scim.resource.schema.attribute.Attribute<?, ?> attribute : modelSchema.getAttributes().values()) {
            String name = attribute.getName();

            if (name.startsWith("meta.")) {
                continue;
            }

            String parentName = attribute.getParentName();

            if (!modelSchema.isCore()) {
                // 扩展 schema 属性以 schema 名称为顶层属性名
                parentName = attribute.getSchema();
            }

            if (parentName != null && !parentName.equals(name)) {
                // 子属性：去掉父前缀得到相对路径
                String relativeName = name.substring(parentName.length() + 1);

                if (relativeName.indexOf('.') != -1) {
                    // 嵌套复合子属性（如 manager.value → 顶层 manager，子 value）
                    String topName = relativeName.substring(0, relativeName.indexOf('.'));
                    String subName = relativeName.substring(relativeName.indexOf('.') + 1);

                    Attribute parent = topLevelAttributes.computeIfAbsent(topName, k -> {
                        Attribute p = new Attribute();
                        p.setName(k);
                        p.setType("complex");
                        p.setMultiValued(false);
                        p.setMutability("readWrite");
                        p.setCaseExact(false);
                        p.setRequired(false);
                        p.setUniqueness("none");
                        return p;
                    });

                    Attribute subAttr = new Attribute();
                    subAttr.setName(subName);
                    subAttr.setType(attribute.getType());
                    subAttr.setMultiValued(false);
                    subAttr.setReturned(attribute.getReturned());
                    subAttr.setMutability(attribute.isImmutable() ? "immutable" : "readWrite");
                    subAttr.setUniqueness(attribute.getUniqueness());

                    List<Attribute> subAttributes = parent.getSubAttributes();
                    if (subAttributes == null) {
                        subAttributes = new ArrayList<>();
                        parent.setSubAttributes(subAttributes);
                    }
                    subAttributes.add(subAttr);
                } else if (modelSchema.isCore()) {
                    // 核心 schema 子属性（如 name.givenName）
                    Attribute parent = topLevelAttributes.computeIfAbsent(parentName, k -> {
                        Attribute p = new Attribute();
                        p.setName(k);
                        p.setType("complex");
                        p.setMultiValued(attribute.isMultivalued());
                        p.setMutability(attribute.isImmutable() ? "immutable" : "readWrite");
                        p.setRequired(attribute.isRequired());
                        p.setCaseExact(attribute.isCaseExact());
                        p.setUniqueness(attribute.getUniqueness());
                        return p;
                    });

                    Attribute subAttr = new Attribute();
                    subAttr.setName(relativeName);
                    subAttr.setType(attribute.getType());
                    subAttr.setMultiValued(false);
                    subAttr.setReturned(attribute.getReturned());
                    subAttr.setMutability(attribute.isImmutable() ? "immutable" : "readWrite");
                    subAttr.setRequired(attribute.isRequired());
                    subAttr.setCaseExact(attribute.isCaseExact());
                    subAttr.setUniqueness(attribute.getUniqueness());

                    List<Attribute> subAttributes = parent.getSubAttributes();
                    if (subAttributes == null) {
                        subAttributes = new ArrayList<>();
                        parent.setSubAttributes(subAttributes);
                    }
                    subAttributes.add(subAttr);
                } else {
                    // 扩展 schema 简单子属性
                    topLevelAttributes.computeIfAbsent(relativeName, createExtensionAttribute(modelSchema, parentName, attribute));
                }
            } else {
                // 顶层属性：若尚未作为父节点创建则添加
                topLevelAttributes.computeIfAbsent(name, k -> createTopLevelAttribute(attribute, k));
            }
        }

        rep.setAttributes(List.copyOf(topLevelAttributes.values()));

        List<Attribute> attributes = rep.getAttributes();

        if (!modelSchema.isInternal() && !attributes.isEmpty()) {
            schemas.put(modelSchema.getId(), rep);
        }
    }

    private Function<String, Attribute> createExtensionAttribute(ModelSchema<?, ?> modelSchema, String schemaName, org.keycloak.scim.resource.schema.attribute.Attribute<?, ?> attribute) {
        return k -> {
            Attribute attr = createTopLevelAttribute(attribute, k);

            if (modelSchema.isCore()) {
                return attr;
            }

            schemas.computeIfAbsent(schemaName, n -> {
                Schema schema = new Schema();

                schema.setName(n);
                schema.setId(n);
                schema.setAttributes(new ArrayList<>());

                return schema;
            }).getAttributes().add(attr);

            return attr;
        };
    }

    private Attribute createTopLevelAttribute(org.keycloak.scim.resource.schema.attribute.Attribute<?, ?> attribute, String name) {
        Attribute attr = new Attribute();

        attr.setName(name);
        attr.setType(attribute.getType());
        attr.setMultiValued(attribute.isMultivalued());
        attr.setReturned(attribute.getReturned());
        attr.setMutability(attribute.isImmutable() ? "immutable" : "readWrite");
        attr.setRequired(attribute.isRequired());
        attr.setCaseExact(attribute.isCaseExact());
        attr.setUniqueness(attribute.getUniqueness());

        return attr;
    }


    @Override
    public Schema get(String id) {
        if (!hasDiscoveryEndpointPermission(session)) {
            throw new ForbiddenException();
        }
        return schemas.get(id);
    }

    /** 返回全部 schema（Discovery 端点，不支持过滤/分页）。 */
    @Override
    public Stream<Schema> getAll(SearchRequest searchRequest) {
        if (hasDiscoveryEndpointPermission(session)) {
            // 按 RFC 7644 §4，/Schemas 为 Discovery 端点，须返回全部 schema，忽略过滤/排序/分页
            return schemas.values().stream();
        }

        throw new ForbiddenException();
    }

    @Override
    public Long count(SearchRequest searchRequest) {
        return getAll(null).count();
    }

    @Override
    public Schema create(Schema resource) {
        throw new ModelException("Schemas are read-only and cannot be created");
    }

    @Override
    public Schema update(Schema resource) {
        throw new ModelException("Schemas are read-only and cannot be updated");
    }

    @Override
    public boolean delete(String id) {
        throw new ModelException("Schemas are read-only and cannot be deleted");
    }

    @Override
    public String getSchema() {
        return Scim.SCHEMA_CORE_SCHEMA;
    }

    @Override
    public <M extends Model> List<ModelSchema<M, Schema>> getSchemas() {
        return List.of();
    }

    @Override
    public Class<Schema> getResourceType() {
        return Schema.class;
    }

    @Override
    public void close() {
        // 无需要关闭的资源
    }
}
