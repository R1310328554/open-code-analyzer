package org.keycloak.client.admin.cli.v2;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static java.util.function.Predicate.not;

/**
 * v2 CLI 命令的紧凑描述符。
 * <p>
 * 构建期由 OpenAPI 规范生成，运行期按服务器缓存；使用 Jackson 反序列化，读取路径无需 SmallRye。
 */
public class KcAdmV2CommandDescriptor {

    /** OpenAPI 规范版本号。 */
    private String version;
    /** 按资源名分组的命令列表。 */
    private List<ResourceDescriptor> resources;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<ResourceDescriptor> getResources() {
        return resources;
    }

    public void setResources(List<ResourceDescriptor> resources) {
        this.resources = resources;
    }

    /** 单个 REST 资源及其下属 CLI 命令。 */
    public static class ResourceDescriptor {
        /** 资源名（CLI 子命令组名，如 {@code client}）。 */
        private String name;
        /** 该资源下的 get/create/patch 等命令描述。 */
        private List<CommandDescriptor> commands;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<CommandDescriptor> getCommands() {
            return commands;
        }

        public void setCommands(List<CommandDescriptor> commands) {
            this.commands = commands;
        }
    }

    /** 单条 Admin REST 操作对应的 CLI 子命令元数据。 */
    public static class CommandDescriptor {
        /** CLI 子命令名（如 get、create、list）。 */
        private String name;
        /** 所属资源名。 */
        private String resourceName;
        /** HTTP 方法（GET、POST 等）。 */
        private String httpMethod;
        /** Admin API 路径模板（含 {@code {realmName}}、{@code {id}} 占位符）。 */
        private String path;
        /** 帮助文本摘要。 */
        private String description;
        /** 是否需要在路径中提供资源 ID。 */
        private boolean requiresId;
        /** 响应是否含 JSON 正文（204 时为 false）。 */
        private boolean hasResponseBody = true;
        /** 请求体/查询参数字段映射的 CLI 选项。 */
        private List<OptionDescriptor> options;
        /** OpenAPI 鉴别器衍生的协议变体子命令。 */
        private List<VariantDescriptor> variants;

        /** 转换时填充但不序列化——仅用于文档示例生成。 */
        @JsonIgnore
        private String operationId;

        public String getOperationId() {
            return operationId;
        }

        public void setOperationId(String operationId) {
            this.operationId = operationId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getResourceName() {
            return resourceName;
        }

        public void setResourceName(String resourceName) {
            this.resourceName = resourceName;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isRequiresId() {
            return requiresId;
        }

        public void setRequiresId(boolean requiresId) {
            this.requiresId = requiresId;
        }

        /** 是否存在非查询参数的请求体字段或变体子命令。 */
        public boolean hasRequestBody() {
            return (options != null && options.stream().anyMatch(not(OptionDescriptor::isQueryParam))) || hasVariants();
        }

        public boolean isHasResponseBody() {
            return hasResponseBody;
        }

        public void setHasResponseBody(boolean hasResponseBody) {
            this.hasResponseBody = hasResponseBody;
        }

        public List<OptionDescriptor> getOptions() {
            return options;
        }

        public void setOptions(List<OptionDescriptor> options) {
            this.options = options;
        }

        public List<VariantDescriptor> getVariants() {
            return variants;
        }

        public boolean hasVariants() {
            return variants != null && !variants.isEmpty();
        }

        public void setVariants(List<VariantDescriptor> variants) {
            this.variants = variants;
        }
    }

    /**
     * 命令的协议专属变体（如 client create 的 {@code oidc} 与 {@code saml}）。
     * <p>
     * 源自 OpenAPI schema 鉴别器；每个变体成为独立子命令，选项为基类字段与协议字段的并集。
     */
    public static class VariantDescriptor {
        /** 变体子命令名（如 oidc）。 */
        private String name;
        /** JSON 鉴别器字段名。 */
        private String discriminatorField;
        /** 鉴别器字段取值。 */
        private String discriminatorValue;
        /** 该变体可用的 CLI 选项列表。 */
        private List<OptionDescriptor> options;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDiscriminatorField() {
            return discriminatorField;
        }

        public void setDiscriminatorField(String discriminatorField) {
            this.discriminatorField = discriminatorField;
        }

        public String getDiscriminatorValue() {
            return discriminatorValue;
        }

        public void setDiscriminatorValue(String discriminatorValue) {
            this.discriminatorValue = discriminatorValue;
        }

        public List<OptionDescriptor> getOptions() {
            return options;
        }

        public void setOptions(List<OptionDescriptor> options) {
            this.options = options;
        }
    }

    /**
     * 将 OpenAPI schema 属性映射为 CLI 选项。
     * <p>
     * {@code fieldName} 为 JSON 属性名（如 {@code clientId}），用于构造请求体；
     * {@code name} 为 kebab-case CLI 标志名（如 {@code client-id}）。
     */
    public static class OptionDescriptor {
        /** 布尔类型常量。 */
        public static final String TYPE_BOOLEAN = "boolean";
        /** 字符串类型常量。 */
        public static final String TYPE_STRING = "string";

        /** CLI 标志名（不含 {@code --} 前缀）。 */
        private String name;
        /** JSON 请求体中的字段名。 */
        private String fieldName;
        /** 字段类型（string、boolean 等）。 */
        private String type;
        /** 选项说明（来自 OpenAPI description）。 */
        private String description;
        /** 是否为数组/多值选项。 */
        private boolean array;
        /** 枚举合法取值列表。 */
        private List<String> enumValues;
        /** 嵌套对象时的父字段名。 */
        private String parentFieldName;
        /** 是否为 URL 查询参数而非请求体字段。 */
        private boolean queryParam;
        /** 数组查询参数是否 explode 展开。 */
        private boolean explode = true;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isArray() {
            return array;
        }

        public void setArray(boolean array) {
            this.array = array;
        }

        public List<String> getEnumValues() {
            return enumValues;
        }

        public void setEnumValues(List<String> enumValues) {
            this.enumValues = enumValues;
        }

        public String getParentFieldName() {
            return parentFieldName;
        }

        public void setParentFieldName(String parentFieldName) {
            this.parentFieldName = parentFieldName;
        }

        public boolean isQueryParam() {
            return queryParam;
        }

        public void setQueryParam(boolean queryParam) {
            this.queryParam = queryParam;
        }

        public boolean isExplode() {
            return explode;
        }

        public void setExplode(boolean explode) {
            this.explode = explode;
        }
    }
}
