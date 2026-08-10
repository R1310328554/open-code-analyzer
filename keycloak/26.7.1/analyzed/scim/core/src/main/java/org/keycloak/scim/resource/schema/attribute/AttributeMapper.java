package org.keycloak.scim.resource.schema.attribute;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.common.util.TriConsumer;
import org.keycloak.models.Model;
import org.keycloak.scim.resource.ResourceTypeRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * <p>属性映射器，定义如何在 {@link Model} 与 {@link ResourceTypeRepresentation} 之间读写属性值。</p>
 * <p>支持 set/add/remove 及多值、复合类型的 JSON 转换。</p>
 * @param <M> Keycloak 领域模型类型
 * @param <R> SCIM 资源表示类型
 * @see Attribute
 */
public class AttributeMapper<M extends Model, R extends ResourceTypeRepresentation> {

    /** 关联的 {@link Attribute} 元数据。 */
    private Attribute<M, R> attribute;
    /** 模型侧 setter 回调。 */
    private final TriConsumer<M, String, ?> modelSetter;
    private TriConsumer<M, String, ?> modelRemover;
    private TriConsumer<M, String, ?> modelAdder;
    private final TriConsumer<Attribute<M, R>, R, ?> representationSetter;

    AttributeMapper(TriConsumer<M, String, ?> modelSetter, TriConsumer<Attribute<M, R>, R, ?> representationSetter) {
        this(modelSetter, representationSetter, null, null);
    }

    AttributeMapper(TriConsumer<M, String, ?> modelSetter, TriConsumer<Attribute<M, R>, R, ?> representationSetter, TriConsumer<M, String, ?> modelRemover, TriConsumer<M, String, ?> modelAdder) {
        this.modelSetter = modelSetter;
        this.representationSetter = representationSetter;
        this.modelRemover = modelRemover;
        this.modelAdder = modelAdder;
    }

    /** 将值写入 SCIM 表示（模型 → 表示）。 */
    public void setValue(R representation, Object value) {
        if (representationSetter != null) {
            ((TriConsumer<Attribute<M, R>, R, Object>) representationSetter).accept(attribute, representation, value);
        }
    }

    /** 将 JSON 值写入模型（表示 → 模型，set 操作）。 */
    public void setValue(M model, JsonNode value) {
        setValue(model, value, (TriConsumer<M, String, Object>) modelSetter);
    }

    /** 向模型追加值；无 adder 时退化为 set。 */
    public void addValue(M model, JsonNode value) {
        if (modelAdder == null) {
            setValue(model, value);
        } else {
            setValue(model, value, (TriConsumer<M, String, Object>) modelAdder);
        }
    }

    /** 从模型移除值；无 remover 时写入 null。 */
    public void removeValue(M model, JsonNode value) {
        if  (modelRemover == null) {
            setValue(model, null);
        } else {
            setValue(model, value, (TriConsumer<M, String, Object>) modelRemover);
        }
    }

    private void setValue(M model, JsonNode value, TriConsumer<M, String, Object> modelSetter) {
        if (modelSetter == null) {
            return;
        }

        String name = attribute.getModelAttributeName();

        if (name == null) {
            return;
        }

        if (attribute != null && attribute.isMultivalued()) {
            Class<?> complexType = attribute.getComplexType();

            if (complexType == null) {
                Set<String> values;

                if (value.isArray()) {
                    values =  value.valueStream().map(JsonNode::asText).collect(Collectors.toSet());
                } else {
                    values = Set.of(value.asText());
                }

                modelSetter.accept(model, name, values);
            } else if (value != null) {
                Set<Object> values = new HashSet<>();

                if (value.isArray()) {
                    for (JsonNode v : value) {
                        if (v.isValueNode()) {
                            values.add(v.textValue());
                        } else {
                            try {
                                values.add(JsonSerialization.readValue(v.toString(), complexType));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                } else if (value.isTextual()) {
                    values.add(value.textValue());
                } else if (!value.isNull()) {
                    try {
                        values.add(JsonSerialization.readValue(value.toString(), complexType));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                modelSetter.accept(model, name, values);
            }
        } else if (value != null && !value.isNull()) {
            modelSetter.accept(model, name, value.asText());
        } else {
            modelSetter.accept(model, name, null);
        }
    }

    void setAttribute(Attribute<M, R> attribute) {
        this.attribute = attribute;
    }
}
