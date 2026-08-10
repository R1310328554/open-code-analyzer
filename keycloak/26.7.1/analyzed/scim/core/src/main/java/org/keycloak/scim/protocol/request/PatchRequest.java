package org.keycloak.scim.protocol.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * SCIM PATCH 请求体（RFC 7644 第 3.5.2 节）。
 * <p>封装一组对 SCIM 资源的部分更新操作。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchRequest {

    /** SCIM PatchOp 消息 Schema URN。 */
    public static final String SCHEMA = "urn:ietf:params:scim:api:messages:2.0:PatchOp";

    /** 消息 Schema 集合。 */
    @JsonProperty("schemas")
    private Set<String> schemas = Set.of(SCHEMA);

    /** PATCH 操作列表。 */
    @JsonProperty("Operations")
    private List<PatchOperation> operations;

    /** 供 Jackson 反射使用的无参构造器。 */
    public PatchRequest() {
        // reflection
    }

    /**
     * 构造包含指定操作列表的 PATCH 请求。
     *
     * @param operations PATCH 操作列表
     */
    public PatchRequest(List<PatchOperation> operations) {
        this.operations = operations;
    }

    /** 创建 {@link Builder} 以流式构建 PATCH 请求。 */
    public static Builder create() {
        return new Builder();
    }

    /** 返回消息 Schema 集合。 */
    public Set<String> getSchemas() {
        return schemas;
    }

    /** 设置消息 Schema 集合。 */
    public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }

    /** 返回 PATCH 操作列表。 */
    public List<PatchOperation> getOperations() {
        return operations;
    }

    /** 设置 PATCH 操作列表。 */
    public void setOperations(List<PatchOperation> operations) {
        this.operations = operations;
    }

    /**
     * 表示单条 PATCH 操作（add / remove / replace）。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PatchOperation {
        /** 操作类型：{@code add}、{@code remove} 或 {@code replace}。 */
        @JsonProperty("op")
        private String op; // "add", "remove", "replace"

        /** 目标属性路径（可为 {@code null} 表示根级操作）。 */
        @JsonProperty("path")
        private String path;

        /** 操作值（JSON 节点）。 */
        @JsonProperty("value")
        private JsonNode value;

        /** 供 Jackson 反射使用的无参构造器。 */
        public PatchOperation() {
            // reflection
        }

        /**
         * 构造单条 PATCH 操作。
         *
         * @param op    操作类型
         * @param path  属性路径
         * @param value 字符串形式的值（可为 {@code null}）
         */
        public PatchOperation(String op, String path, String value) {
            this.op = op;
            this.path = path;
            if (value == null) {
                this.value = null;
            } else {
                try {
                    if (value.startsWith("{") || value.startsWith("[")) {
                        this.value = JsonSerialization.readValue(value, JsonNode.class);
                    } else {
                        this.value = new TextNode(value);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /** 返回操作类型。 */
        public String getOp() {
            return op;
        }

        /** 设置操作类型。 */
        public void setOp(String op) {
            this.op = op;
        }

        /** 返回属性路径。 */
        public String getPath() {
            return path;
        }

        /** 设置属性路径。 */
        public void setPath(String path) {
            this.path = path;
        }

        /** 返回操作值。 */
        public JsonNode getValue() {
            return value;
        }

        /** 设置操作值。 */
        public void setValue(JsonNode value) {
            this.value = value;
        }
    }

    /** 流式构建 {@link PatchRequest} 的建造者。 */
    public static class Builder {

        /** 累积的 PATCH 操作列表。 */
        private final List<PatchOperation> operations = new ArrayList<>();

        /** 添加带路径的 add 操作。 */
        public Builder add(String path, String value) {
            operation("add", path, value);
            return this;
        }

        /** 添加根级 add 操作（无路径）。 */
        public Builder add(String value) {
            operation("add", null, value);
            return this;
        }

        /** 添加带路径的 replace 操作。 */
        public Builder replace(String path, String value) {
            operation("replace", path, value);
            return this;
        }

        /** 添加根级 replace 操作（无路径）。 */
        public Builder replace(String value) {
            replace(null, value);
            return this;
        }

        /** 添加 remove 操作。 */
        public Builder remove(String path) {
            operation("remove", path, null);
            return this;
        }

        /** 追加一条 PATCH 操作。 */
        private void operation(String operation, String path, String value) {
            operations.add(new PatchOperation(operation, path, value));
        }

        /** 构建最终的 {@link PatchRequest}。 */
        public PatchRequest build() {
            return new PatchRequest(operations);
        }
    }
}
