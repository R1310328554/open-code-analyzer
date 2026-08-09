package com.taobao.arthas.mcp.server.tool.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;
import com.taobao.arthas.mcp.server.util.Assert;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 轻量级 JSON Schema 生成器：根据方法参数上的 {@link ToolParam} 注解构建 MCP 入参 Schema。
 * <p>
 * 支持常见 Java 类型到 JSON Schema type 的映射，并合并 {@code @JsonProperty} 等 Jackson 注解。
 */
public final class JsonSchemaGenerator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /** 未显式标注时，参数默认为必填。 */
    private static final boolean PROPERTY_REQUIRED_BY_DEFAULT = true;

    private JsonSchemaGenerator() {
    }

    /**
     * 为带 {@code @ToolParam} 的方法参数生成 object 型 JSON Schema。
     *
     * @param method 目标工具方法
     * @return 含 properties、required 与 additionalProperties=false 的 Schema
     */
    public static McpSchema.JsonSchema generateForMethodInput(Method method) {
        Assert.notNull(method, "method cannot be null");

        ObjectNode schema = OBJECT_MAPPER.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = schema.putObject("properties");
        List<String> required = new ArrayList<>();

        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
            // 无 @ToolParam 的参数不参与 Schema（例如 ToolContext 等框架注入参数）
            if (toolParam == null) {
                continue;
            }
            
            String paramName = getParameterName(parameter);
            Class<?> paramType = parameter.getType();

            boolean isRequired = isParameterRequired(parameter);
            if (isRequired) {
                required.add(paramName);
            }

            ObjectNode paramProperties = generateParameterProperties(paramType);

            String description = getParameterDescription(parameter);
            if (description != null) {
                paramProperties.put("description", description);
            }

            properties.set(paramName, paramProperties);
        }

        if (!required.isEmpty()) {
            ArrayNode requiredArray = schema.putArray("required");
            for (String req : required) {
                requiredArray.add(req);
            }
        }

        // MCP 要求禁止未声明的额外字段
        schema.put("additionalProperties", false);

        return new McpSchema.JsonSchema("object", convertToMap(properties), required, false);
    }

    /** 优先使用 {@code @JsonProperty} 值作为 JSON 字段名，否则使用编译参数名。 */
    private static String getParameterName(Parameter parameter) {
        JsonProperty jsonProperty = parameter.getAnnotation(JsonProperty.class);
        if (jsonProperty != null && !jsonProperty.value().isEmpty()) {
            return jsonProperty.value();
        }
        return parameter.getName();
    }

    /** 将 Java 类型映射为 JSON Schema 的 type/items 结构。 */
    private static ObjectNode generateParameterProperties(Class<?> paramType) {
        ObjectNode properties = OBJECT_MAPPER.createObjectNode();

        if (paramType == String.class) {
            properties.put("type", "string");
        } else if (paramType == int.class || paramType == Integer.class
                || paramType == long.class || paramType == Long.class) {
            properties.put("type", "integer");
        } else if (paramType == double.class || paramType == Double.class
                || paramType == float.class || paramType == Float.class) {
            properties.put("type", "number");
        } else if (paramType == boolean.class || paramType == Boolean.class) {
            properties.put("type", "boolean");
        } else if (paramType.isArray()) {
            properties.put("type", "array");
            ObjectNode items = properties.putObject("items");
            Class<?> componentType = paramType.getComponentType();
            
            if (componentType == String.class) {
                items.put("type", "string");
            } else if (componentType == int.class || componentType == Integer.class
                    || componentType == long.class || componentType == Long.class) {
                items.put("type", "integer");
            } else if (componentType == double.class || componentType == Double.class
                    || componentType == float.class || componentType == Float.class) {
                items.put("type", "number");
            } else if (componentType == boolean.class || componentType == Boolean.class) {
                items.put("type", "boolean");
            } else {
                items.put("type", "object");
            }
        } else {
            properties.put("type", "object");
        }

        return properties;
    }

    /** 综合 {@code @ToolParam.required} 与 {@code @JsonProperty.required} 判定是否必填。 */
    private static boolean isParameterRequired(Parameter parameter) {
        ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
        if (toolParam != null) {
            return toolParam.required();
        }

        JsonProperty jsonProperty = parameter.getAnnotation(JsonProperty.class);
        if (jsonProperty != null) {
            return jsonProperty.required();
        }

        return PROPERTY_REQUIRED_BY_DEFAULT;
    }

    /** 从 {@code @ToolParam.description} 或 {@code @JsonPropertyDescription} 读取参数说明。 */
    private static String getParameterDescription(Parameter parameter) {
        ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
        if (toolParam != null && toolParam.description() != null && !toolParam.description().isEmpty()) {
            return toolParam.description();
        }

        JsonPropertyDescription jsonPropertyDescription = parameter.getAnnotation(JsonPropertyDescription.class);
        if (jsonPropertyDescription != null && !jsonPropertyDescription.value().isEmpty()) {
            return jsonPropertyDescription.value();
        }

        return null;
    }

    /** 将 Jackson ObjectNode 递归转换为 MCP Schema 使用的 Map 结构。 */
    private static Map<String, Object> convertToMap(ObjectNode node) {
        Map<String, Object> result = new HashMap<>();
        node.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            if (value.isObject()) {
                result.put(entry.getKey(), convertToMap((ObjectNode) value));
            } else if (value.isArray()) {
                List<Object> array = new ArrayList<>();
                value.elements().forEachRemaining(element -> {
                    if (element.isObject()) {
                        array.add(convertToMap((ObjectNode) element));
                    } else {
                        array.add(element.asText());
                    }
                });
                result.put(entry.getKey(), array);
            } else {
                result.put(entry.getKey(), value.asText());
            }
        });
        return result;
    }
}
