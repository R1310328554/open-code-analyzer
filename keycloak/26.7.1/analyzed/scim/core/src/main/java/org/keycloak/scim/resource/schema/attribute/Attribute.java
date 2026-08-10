package org.keycloak.scim.resource.schema.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.keycloak.common.util.TriConsumer;
import org.keycloak.models.Model;
import org.keycloak.scim.resource.ResourceTypeRepresentation;
import org.keycloak.scim.resource.schema.ModelSchema;

import com.fasterxml.jackson.databind.JsonNode;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * {@link ModelSchema} 中的 SCIM 属性定义，包含元数据及双向映射器。
 * <p>通过 {@link AttributeMapper} 在 {@link ResourceTypeRepresentation} 与 {@link Model} 之间转换值。</p>
 * @param <M> Keycloak 领域模型类型
 * @param <R> SCIM 资源表示类型
 * @see ModelSchema
 */
public class Attribute<M extends Model, R extends ResourceTypeRepresentation> {

    /** returned 特性：始终返回。 */
    public static final String RETURNED_ALWAYS = "always";
    /** returned 特性：默认返回。 */
    public static final String RETURNED_DEFAULT = "default";
    /** returned 特性：仅当请求 attributes 时返回。 */
    public static final String RETURNED_REQUEST = "request";
    /** returned 特性：永不返回。 */
    public static final String RETURNED_NEVER = "never";

    /** 从带 URN 前缀的属性名提取 schema URN。 */
    public static String getSchema(String name) {
        requireNonNull(name, "name is required");
        int schemaSeparator = name.lastIndexOf(':');

        if (schemaSeparator == -1) {
            return null;
        }

        return name.substring(0, schemaSeparator);
    }

    /** 从属性 URN 解析资源类型段（如 User）。 */
    public static String getResourceType(String name) {
        requireNonNull(name, "name is required");
        String schema = getSchema(name);

        if  (schema == null) {
            return null;
        }

        int resourceTypeSeparator = schema.lastIndexOf(':');

        if (resourceTypeSeparator == -1) {
            return null;
        }

        return schema.substring(resourceTypeSeparator + 1);
    }

    /** 返回去掉 schema 前缀后的简单属性名。 */
    public static String getSimpleName(String name) {
        String schema = getSchema(name);

        if (schema == null) {
            return name;
        }

        return name.substring(schema.length() + 1);
    }

    /**
     * 创建简单（非复合）属性的构建器。
     *
     * @param name {@link R} 表示中的属性名
     * @return 属性构建器
     */
    public static <M extends Model, R extends ResourceTypeRepresentation> Builder<M, R> simple(String name) {
        return (Builder<M, R>) new Builder<>(name, null).string();
    }

    /**
     * 创建复合属性构建器，{@code complexType} 决定表示层 setter 类型。
     *
     * @param name 复合属性名
     * @param complexType 复合 Java 类型
     * @return 属性构建器
     */
    public static <M extends Model, R extends ResourceTypeRepresentation> Builder<M, R> complex(String name, Class<?> complexType) {
        Builder<M, R> builder = new Builder<>(name, complexType);
        builder.type = "complex";
        return builder;
    }

    private final String name;
    private final AttributeMapper<M, R> mapper;
    private final String parentName;
    private final String alias;
    private Function<Attribute<M, R>, String> modelAttributeResolver;
    private String type;
    private String mutability;
    private String returned = RETURNED_DEFAULT;
    private boolean multivalued;
    private Class<?> complexType;
    private boolean required;
    private boolean caseExact;
    private String uniqueness;

    private Attribute(String name, AttributeMapper<M, R> mapper, String parentName, String alias) {
        this.name = name;
        this.mapper = mapper;
        this.parentName = parentName;
        this.alias = alias;
        this.mapper.setAttribute(this);
    }

    /**
     * {@link R} 表示中的 SCIM 属性全名。
     *
     * @return 属性名
     */
    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    /**
     * 子属性时返回父属性名，否则为 null。
     *
     * @return 父属性名或 null
     */
    public String getParentName() {
        return parentName;
    }

    /**
     * 关联的 {@link Model} 侧属性名。
     *
     * @return 模型属性名，无映射时 null
     */
    public String getModelAttributeName() {
        if (modelAttributeResolver != null) {
            return modelAttributeResolver.apply(this);
        }
        return null;
    }

    private void setModelAttributeResolver(Function<Attribute<M, R>, String> resolver) {
        this.modelAttributeResolver = resolver;
    }

    public boolean isTimestamp() {
        return Objects.equals(type, "timestamp");
    }

    public boolean isBoolean() {
        return Objects.equals(type, "boolean");
    }

    private void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private void setMutability(String mutability) {
        this.mutability = mutability;
    }

    public boolean isImmutable() {
        return Objects.equals(mutability, "immutable");
    }

    public String getReturned() {
        return returned;
    }

    private void setReturned(String returned) {
        this.returned = returned;
    }

    private void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }

    public boolean isMultivalued() {
        return multivalued;
    }

    private void setComplexType(Class<?> complexType) {
        this.complexType = complexType;
    }

    public Class<?> getComplexType() {
        return complexType;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    public void setCaseExact(boolean caseExact) {
        this.caseExact = caseExact;
    }

    public boolean isCaseExact() {
        return caseExact;
    }

    public void setUniqueness(String uniqueness) {
        this.uniqueness = uniqueness;
    }

    public String getUniqueness() {
        return uniqueness;
    }

    public String getSchema() {
        if (!isExtension()) {
            return null;
        }
        return getSchema(getName());
    }

    public String getResourceType() {
        if (!isExtension()) {
            return null;
        }
        return getResourceType(getName());
    }

    public String getSimpleName() {
        if (!isExtension()) {
            return null;
        }
        return getSimpleName(getName());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Attribute<?, ?> attribute)) return false;
        return Objects.equals(name, attribute.name) && Objects.equals(parentName, attribute.parentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parentName);
    }

    /** 将 JSON 值写入模型（PATCH set）。 */
    public void set(M model, JsonNode value) {
        mapper.setValue(model, value);
    }

    /** 将模型值写入 SCIM 表示（读取/序列化）。 */
    public void set(R resource, Object value) {
        mapper.setValue(resource, value);
    }

    /** 向模型追加多值属性项（PATCH add）。 */
    public void add(M model, JsonNode value) {
        mapper.addValue(model, value);
    }

    /** 从模型移除多值属性项（PATCH remove）。 */
    public void remove(M model, JsonNode value) {
        mapper.removeValue(model, value);
    }

    /**
     * 根据 returned 特性及 attributes/excludedAttributes 过滤，判断是否跳过该属性。
     */
    public boolean isExcluded(ModelSchema<M, R> schema, List<String> requestedAttributes, List<String> excludedAttributes) {
        String returned = getReturned();

        // returned: always - never skip
        if (Attribute.RETURNED_ALWAYS.equals(returned)) {
            return false;
        }

        // returned: never - always skip
        if (Attribute.RETURNED_NEVER.equals(returned)) {
            return true;
        }

        // If attributes parameter is specified (inclusion filter)
        if (requestedAttributes != null && !requestedAttributes.isEmpty()) {
            if (!isPresent(schema, requestedAttributes)) {
                return true;
            }
        }

        if (Attribute.RETURNED_REQUEST.equals(returned)) {
            // No attributes parameter specified - returned: request attributes are not returned by default
            return !isPresent(schema, requestedAttributes);
        }

        return isPresent(schema, excludedAttributes);
    }

    private boolean isPresent(ModelSchema<M, R> schema, List<String> names) {
        return ofNullable(names).orElse(List.of()).stream()
                .map(path -> {
                    String parentName = getParentName();

                    // fallback to check if the attribute is a child of a requested attribute
                    if (path.equalsIgnoreCase(parentName)) {
                        return this;
                    }

                    // fallback to check if the path is the scheme itself
                    if (path.equalsIgnoreCase(schema.getId())) {
                        return this;
                    }

                    return schema.getAttributeByPath(path);
                }).anyMatch(this::equals);
    }

    public boolean isExtension() {
        return getName().contains(":");
    }

    /** 流式构建 {@link Attribute} 及其子属性的构建器。 */
    public static class Builder<M extends Model, R extends ResourceTypeRepresentation> {

        private final Class<?> complexType;
        private final String name;
        private TriConsumer<M, String, ?> modelSetter;
        private TriConsumer<Attribute<M, R>, R, ?> representationSetter;
        List<Attribute<M, R>> attributes = new ArrayList<>();
        // 默认将模型属性名解析为与 SCIM 属性名相同
        private Function<Attribute<M, R>, String> modelAttributeResolver = Attribute::getName;
        private String type;
        private String mutability;
        private String returned;
        private boolean multivalued;
        private TriConsumer<M, String, Set<?>> modelRemover;
        private TriConsumer<M, String, Set<?>> modelAdder;
        private boolean required;
        private boolean caseExact = true;
        private String uniqueness = "none";

        private Builder(String name, Class<?> complexType) {
            requireNonNull(name, "name cannot be null");
            this.complexType = complexType;
            this.name = name;
        }

        public <V, C extends BiConsumer<R, V>> Builder<M, R> withModelSetter(TriConsumer<M, String, ?> modelSetter, C repSetter) {
            this.modelSetter = modelSetter;
            this.representationSetter = (TriConsumer<Attribute<M, R>, R, Object>) (attribute, r, o) -> repSetter.accept(r, (V) o);
            return this;
        }

        public <V, C extends TriConsumer<Attribute<M, R>, R, V>> Builder<M, R> withSetters(TriConsumer<M, String, ?> modelSetter, C repSetter) {
            this.modelSetter = modelSetter;
            this.representationSetter = repSetter;
            return this;
        }

        public Builder<M, R> withModelSetter(TriConsumer<M, String, String> modelSetter) {
            this.modelSetter = modelSetter;
            this.representationSetter = new ComplexAttributeSetter<>(name, complexType);
            return this;
        }

        public Builder<M, R> withModelSetter(BiConsumer<M, String> modelSetter) {
            this.modelSetter = (model, name, value) -> modelSetter.accept(model, (String) value);
            this.representationSetter = new ComplexAttributeSetter<>(name, complexType);
            return this;
        }

        public Builder<M, R> withAttribute(String name, TriConsumer<M, String, String> modelSetter) {
            return withAttribute(name, null, modelSetter);
        }

        public Builder<M, R> withAttribute(String name, String alias, TriConsumer<M, String, String> modelSetter) {
            String subName = this.name + "." + name;
            Attribute<M, R> attribute = assembleAttribute(subName, this.name, alias,
                    new AttributeMapper<>(modelSetter, new ComplexAttributeSetter<>(this.name, name, complexType)),
                    modelAttributeResolver, "string", null, returned, false, false, true, null, null);
            attributes.add(attribute);
            return this;
        }

        public Builder<M, R> modelAttributeResolver(Function<Attribute<M, R>, String> resolver) {
            this.modelAttributeResolver = resolver;
            return this;
        }

        public Builder<M, R> string() {
            this.type = "string";
            return this;
        }

        public Builder<M, R> timestamp() {
            this.type = "timestamp";
            return this;
        }

        public Builder<M, R> bool() {
            this.type = "boolean";
            return this;
        }

        public Builder<M, R> immutable() {
            this.mutability = "immutable";
            return this;
        }

        public Builder<M, R> returned(String returned) {
            this.returned = returned;
            return this;
        }

        public List<Attribute<M, R>> build() {
            Attribute<M, R> attribute = assembleAttribute(name, null, null,
                    new AttributeMapper<>(modelSetter, representationSetter, modelRemover, modelAdder),
                    modelAttributeResolver, type, mutability, returned, multivalued, required, caseExact, uniqueness, complexType);
            if (attributes.isEmpty()) {
                // do not add the root attribute if there are subattributes
                attributes.add(attribute);
            }
            return attributes;
        }

        private Attribute<M, R> assembleAttribute(String name, String parentName, String alias,
                                                   AttributeMapper<M, R> mapper,
                                                   Function<Attribute<M, R>, String> modelAttributeResolver,
                                                   String type, String mutability, String returned,
                                                   boolean multivalued,
                                                   boolean required,
                                                   boolean caseExact,
                                                   String uniqueness,
                                                   Class<?> complexType) {
            Attribute<M, R> attribute = new Attribute<>(name, mapper, parentName, alias);
            attribute.setModelAttributeResolver(modelAttributeResolver);
            attribute.setType(type);
            attribute.setMutability(mutability);
            if (returned != null) {
                attribute.setReturned(returned);
            }
            attribute.setMultivalued(multivalued);
            attribute.setComplexType(complexType);
            attribute.setRequired(required);
            attribute.setCaseExact(caseExact);
            attribute.setUniqueness(uniqueness == null ? "none" : uniqueness);
            return attribute;
        }

        public Builder<M, R> multivalued() {
            this.multivalued = true;
            return this;
        }

        public <C> Builder<M, R> withModelRemover(TriConsumer<M, String, Set<C>> remover) {
            this.modelRemover = (m, s, objects) -> remover.accept(m, s, (Set<C>) objects);
            return this;
        }

        public <C> Builder<M, R> withModelAdder(TriConsumer<M, String, Set<C>> adder) {
            this.modelAdder = (m, s, objects) -> adder.accept(m, s, (Set<C>) objects);
            return this;
        }

        public Builder<M, R> required() {
            this.required = true;
            return this;
        }

        public Builder<M, R> notCaseExact() {
            this.caseExact = false;
            return this;
        }

        public Builder<M, R> serverUnique() {
            this.uniqueness = "server";
            return this;
        }

        public Builder<M, R> globalUnique() {
            this.uniqueness = "global";
            return this;
        }
    }
}
