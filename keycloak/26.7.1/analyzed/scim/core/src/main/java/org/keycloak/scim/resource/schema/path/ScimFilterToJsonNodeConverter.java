package org.keycloak.scim.resource.schema.path;

import org.keycloak.models.ModelValidationException;
import org.keycloak.scim.filter.FilterUtils;
import org.keycloak.scim.filter.ScimFilterParser;
import org.keycloak.scim.filter.ScimFilterParserBaseVisitor;
import org.keycloak.scim.resource.schema.attribute.Attribute;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 将 SCIM 过滤表达式转换为 {@link JsonNode} 的访问者实现。
 * <p>仅支持 {@code eq} 比较运算符，用于在复杂属性路径上构造 JSON 值。</p>
 */
class ScimFilterToJsonNodeConverter extends ScimFilterParserBaseVisitor<JsonNode> {

    /** 当前正在解析的 SCIM 属性。 */
    private final Attribute<?, ?> attribute;

    /**
     * 构造转换器。
     *
     * @param attribute 目标 SCIM 属性
     */
    public ScimFilterToJsonNodeConverter(Attribute<?, ?> attribute) {
        this.attribute = attribute;
    }

    @Override
    public JsonNode visitFilter(ScimFilterParser.FilterContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public JsonNode visitExpression(ScimFilterParser.ExpressionContext ctx) {
        if (ctx.OR() != null) {
            JsonNode left = visit(ctx.expression());
            JsonNode right = visit(ctx.andExpression());
            ArrayNode array = JsonNodeFactory.instance.arrayNode();
            flattenIntoArray(array, left);
            flattenIntoArray(array, right);
            return array;
        }
        return visit(ctx.andExpression());
    }

    @Override
    public JsonNode visitAndExpression(ScimFilterParser.AndExpressionContext ctx) {
        if (ctx.AND() != null) {
            JsonNode left = visit(ctx.andExpression());
            JsonNode right = visit(ctx.notExpression());
            ObjectNode merged = JsonNodeFactory.instance.objectNode();
            mergeFields(merged, left);
            mergeFields(merged, right);
            return merged;
        }
        return visit(ctx.notExpression());
    }

    @Override
    public JsonNode visitNotExpression(ScimFilterParser.NotExpressionContext ctx) {
        if (ctx.NOT() != null) {
            throw new IllegalArgumentException("NOT operator is not supported when converting a SCIM filter to a JSON value");
        }
        return visit(ctx.atom());
    }

    @Override
    public JsonNode visitAtom(ScimFilterParser.AtomContext ctx) {
        if (ctx.valuePath() != null) {
            return visit(ctx.valuePath());
        }
        if (ctx.attributeExpression() != null) {
            return visit(ctx.attributeExpression());
        }
        return visit(ctx.expression());
    }

    @Override
    public JsonNode visitValuePath(ScimFilterParser.ValuePathContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public JsonNode visitPresentExpression(ScimFilterParser.PresentExpressionContext ctx) {
        throw new IllegalArgumentException("Present (pr) operator is not supported when converting a SCIM filter to a JSON value");
    }

    @Override
    public JsonNode visitComparisonExpression(ScimFilterParser.ComparisonExpressionContext ctx) {
        String operator = ctx.compareOp().getText().toLowerCase();

        if (!"eq".equals(operator)) {
            throw new IllegalArgumentException("Only 'eq' operator is supported when converting a SCIM filter to a JSON value, got: " + operator);
        }

        Class<?> complexType = attribute.getComplexType();

        if (complexType == null) {
            return null;
        }

        String attrName = ctx.ATTRPATH().getText();

        if (!isComplexTypeAttribute(complexType, attrName)) {
            throw new ModelValidationException("Unknown attribute " + attrName);
        }

        String value = extractValue(ctx.compValue());
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        if (value == null) {
            node.putNull(attrName);
        } else {
            node.put(attrName, value);
        }

        return node;
    }

    /** 判断 {@code attrName} 是否为 {@code complexType} 的 Jackson 可序列化属性。 */
    private static boolean isComplexTypeAttribute(Class<?> complexType, String attrName) {
        JavaType javaType = JsonSerialization.mapper.getTypeFactory().constructType(complexType);
        SerializationConfig serializationConfig = JsonSerialization.mapper.getSerializationConfig();
        return serializationConfig.introspect(javaType).findProperties().stream().anyMatch(p -> p.getName().equals(attrName));
    }

    /** 从比较值上下文提取字符串值。 */
    private String extractValue(ScimFilterParser.CompValueContext ctx) {
        return FilterUtils.extractCompValue(ctx);
    }

    /** 将 {@code node} 展平追加到数组（数组节点则逐个添加，否则整体添加）。 */
    private void flattenIntoArray(ArrayNode array, JsonNode node) {
        if (node.isArray()) {
            node.forEach(array::add);
        } else {
            array.add(node);
        }
    }

    /** 将源对象节点的字段合并到目标对象节点。 */
    private void mergeFields(ObjectNode target, JsonNode source) {
        if (source.isObject()) {
            source.properties().forEach(e -> target.set(e.getKey(), e.getValue()));
        }
    }
}
