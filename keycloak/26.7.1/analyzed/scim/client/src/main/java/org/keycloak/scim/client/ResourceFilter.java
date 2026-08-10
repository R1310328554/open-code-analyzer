package org.keycloak.scim.client;

/**
 * SCIM 过滤表达式的流式构建器，支持比较、逻辑运算符与括号分组。
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class ResourceFilter {

    /** 创建新的过滤器构建器实例。 */
    public static ResourceFilter filter() {
        return new ResourceFilter();
    }

    /** 累积的过滤表达式字符串。 */
    private final StringBuilder filter = new StringBuilder();

    // 比较运算符

    /** 等于（eq）比较。 */
    public ResourceFilter eq(String property, String value) {
        append(property + " eq " + quote(value));
        return this;
    }

    public ResourceFilter ne(String property, String value) {
        append(property + " ne " + quote(value));
        return this;
    }

    public ResourceFilter co(String property, String value) {
        append(property + " co " + quote(value));
        return this;
    }

    public ResourceFilter sw(String property, String value) {
        append(property + " sw " + quote(value));
        return this;
    }

    public ResourceFilter ew(String property, String value) {
        append(property + " ew " + quote(value));
        return this;
    }

    public ResourceFilter gt(String property, Object value) {
        if (value instanceof String) {
            value = quote((String) value);
        }
        append(property + " gt " + value);
        return this;
    }

    public ResourceFilter ge(String property, Object value) {
        if (value instanceof String) {
            value = quote((String) value);
        }
        append(property + " ge " + value);
        return this;
    }

    public ResourceFilter lt(String property, Object value) {
        if (value instanceof String) {
            value = quote((String) value);
        }
        append(property + " lt " + value);
        return this;
    }

    public ResourceFilter le(String property, Object value) {
        if (value instanceof String) {
            value = quote((String) value);
        }
        append(property + " le " + value);
        return this;
    }

    public ResourceFilter pr(String property) {
        append(property + " pr");
        return this;
    }

    // 逻辑运算符

    /** 追加逻辑与（and）。 */
    public ResourceFilter and() {
        filter.append(" and ");
        return this;
    }

    public ResourceFilter or() {
        filter.append(" or ");
        return this;
    }

    public ResourceFilter not() {
        append("not ");
        return this;
    }

    // 分组括号

    public ResourceFilter lparen() {
        filter.append("(");
        return this;
    }

    public ResourceFilter rparen() {
        filter.append(")");
        return this;
    }

    /** 生成完整的 SCIM filter 查询字符串。 */
    public String build() {
        return filter.toString();
    }

    private void append(String s) {
        if (!filter.isEmpty() && !filter.toString().endsWith("(") && !filter.toString().endsWith("not ")) {
            filter.append(" ");
        }
        filter.append(s);
    }

    private String quote(String value) {
        // Escape backslashes and quotes
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
