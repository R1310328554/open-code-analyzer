package org.keycloak.scim.resource.schema.path;

import org.keycloak.scim.filter.FilterUtils;
import org.keycloak.scim.filter.ScimFilterParser;
import org.keycloak.scim.resource.ResourceTypeRepresentation;
import org.keycloak.scim.resource.schema.ModelSchema;
import org.keycloak.scim.resource.schema.attribute.Attribute;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * SCIM PATCH/过滤路径解析器，支持属性路径及方括号内过滤器表达式。
 * <p>例如 {@code emails[type eq "work"].value} 会分离路径与 filter。</p>
 */
public final class Path {

    /** 解析后的属性路径（不含 filter）。 */
    private final String path;
    /** 方括号内的 SCIM 过滤表达式，无 filter 时为 null。 */
    private final String filter;

    /**
     * 从原始路径字符串构造 Path。
     * @param schema 用于 null 路径时默认 schema id
     * @param rawPath 原始 SCIM 路径
     */
    public <R extends ResourceTypeRepresentation> Path(ModelSchema<?, ?> schema, String rawPath) {
        if (rawPath == null) {
            this.path = schema.getId();
            this.filter = null;
        } else {
            int filterStartIdx = rawPath.indexOf("[");

            if (filterStartIdx > 0) {
                int filterEndIdx = rawPath.lastIndexOf("]");

                if (filterEndIdx == -1) {
                    throw new RuntimeException("Invalid path: " + rawPath);
                }

                // filter 表达式位于方括号内
                String path = rawPath.substring(0, filterStartIdx);

                if (rawPath.indexOf('.', filterEndIdx) != -1) {
                    // 拼接 filter 后的子属性，如 emails[type eq "work"].value
                    path = path + rawPath.substring(filterEndIdx + 1);
                }

                this.path = path;
                this.filter = rawPath.substring(filterStartIdx + 1, filterEndIdx);
            } else {
                this.path = rawPath;
                this.filter = null;
            }
        }
    }

    /** 返回不含 filter 的属性路径。 */
    public String getPath() {
        return path;
    }

    /** 若有 filter，解析并转换为匹配条件的 JSON 值节点。 */
    public JsonNode getValue(Attribute<?, ?> attribute) {
        if (filter == null) {
            return NullNode.getInstance();
        }

        ScimFilterParser.FilterContext filterContext = FilterUtils.parseFilter(filter);
        return new ScimFilterToJsonNodeConverter(attribute).visit(filterContext);
    }
}
