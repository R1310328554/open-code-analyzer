package org.keycloak.scim.resource.resourcetype;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SCIM ResourceType 资源，描述一种 SCIM 资源类型的元信息与关联 schema。
 * <p>Discovery 端点返回此类型列表，供客户端了解可用端点与 schema。</p>
 */
public class ResourceType extends org.keycloak.scim.resource.ResourceTypeRepresentation {

    /** ResourceType 核心 schema URN。 */
    public static final String SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";

    /** 资源类型名称（如 User、Group）。 */
    @JsonProperty("name")
    private String name;

    /** 资源类型描述。 */
    @JsonProperty("description")
    private String description;

    /** 该资源类型的 REST 端点相对路径。 */
    @JsonProperty("endpoint")
    private String endpoint;

    /** 主 schema URN。 */
    @JsonProperty("schema")
    private String schema;

    /** 可选 schema 扩展列表。 */
    @JsonProperty("schemaExtensions")
    private List<SchemaExtension> schemaExtensions;

    @Override
    public Set<String> getSchemas() {
        return Set.of(SCHEMA);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<SchemaExtension> getSchemaExtensions() {
        return schemaExtensions;
    }

    public void setSchemaExtensions(List<SchemaExtension> schemaExtensions) {
        this.schemaExtensions = schemaExtensions;
    }

    /**
     * 资源类型的 schema 扩展定义，标明扩展 URN 及是否必需。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SchemaExtension {
        @JsonProperty("schema")
        private String schema;

        @JsonProperty("required")
        private Boolean required = false;

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }
    }
}
