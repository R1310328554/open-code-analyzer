package org.keycloak.config;

/**
 * 处理含通配符占位符的配置键的工具类。
 * <p>
 * 通配符选项的配置键在 {@link #WILDCARD_START} 与 {@link #WILDCARD_END} 之间包含可变片段。
 * <p>
 * Keycloak 中的通配符选项<strong>总是</strong>以可变片段结尾。
 */
public class WildcardOptionsUtil {

    /**
     * 标记配置键中通配符片段的起始字符。
     */
    public static final String WILDCARD_START = "<";

    /**
     * 标记配置键中通配符片段的结束字符。
     */
    public static final String WILDCARD_END = ">";

    /**
     * 判断给定配置键是否为通配符选项（含可变片段）。
     * <p>
     * 示例：
     * <pre>{@code
     * isWildcardOption("tracing-header-<header>")      → "true"
     * isWildcardOption("tracing-header-<headxxx")      → "false"
     * isWildcardOption("tracing-header-headxxx>")      → "false"
     * isWildcardOption("db-kind-<datasource>")         → "true"
     * isWildcardOption("http-port")                    → "false"
     * isWildcardOption("quarkus.<sth>.end")            → "true"
     * }</pre>
     *
     * @param key 待检查的配置键
     * @return 若键表示通配符选项则为 {@code true}
     */
    public static boolean isWildcardOption(String key) {
        return key != null && key.contains(WILDCARD_START) && key.contains(WILDCARD_END);
    }

    /**
     * 提取通配符键的前缀部分。
     * 应始终先通过 {@link #isWildcardOption(String)} 确认存在通配符。
     * <p>
     * 示例：
     * <pre>{@code
     * getWildcardPrefix("tracing-header-<header>")       → "tracing-header-"
     * getWildcardPrefix("db-kind-<datasource>")         → "db-kind-"
     * }</pre>
     *
     * @param wildcardKey 含通配符片段的配置键
     * @return 通配符标记之前的键前缀，否则为 {@code null}
     */
    public static String getWildcardPrefix(String wildcardKey) {
        return wildcardKey != null && wildcardKey.contains(WILDCARD_START) ? wildcardKey.substring(0, wildcardKey.indexOf(WILDCARD_START)) : null;
    }

    /**
     * 将通配符占位符替换为具体值，生成实际配置键。
     * 应始终先通过 {@link #isWildcardOption(String)} 确认存在通配符。
     * <p>
     * 示例：
     * <pre>{@code
     * getWildcardNamedKey("tracing-header-<header>", "Authorization")  → "tracing-header-Authorization"
     * getWildcardNamedKey("db-kind-<datasource>", "user-store") → "db-kind-user-store"
     * }</pre>
     *
     * @param wildcardKey 含通配符片段的配置键
     * @param value       替换通配符的具体值
     * @return 解析后的完整键名，否则为 {@code null}
     */
    public static String getWildcardNamedKey(String wildcardKey, String value) {
        var prefix = getWildcardPrefix(wildcardKey);
        return prefix != null ? prefix.concat(value) : null;
    }

}
