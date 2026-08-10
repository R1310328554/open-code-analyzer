package org.keycloak.config;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.CaseFormat;

/**
 * 单个 Keycloak 配置选项的不可变描述，包含类型、键名、分类与默认值等元数据。
 *
 * @param <T> 选项值的 Java 类型
 */
public class Option<T> {
    /** 选项值的 Java 类型。 */
    private final Class<T> type;
    /** 集合元素或辅助组件类型。 */
    private final Class<?> componentType;
    /** 配置键名（kebab-case）。 */
    private final String key;
    /** 选项所属分类。 */
    private final OptionCategory category;
    /** 是否在帮助/文档中隐藏。 */
    private final boolean hidden;
    /** 是否为构建期（build-time）选项。 */
    private final boolean buildTime;
    /** 人类可读描述（可能含 Preview/Experimental 前缀）。 */
    private final String description;
    /** 默认值（可为空）。 */
    private final Optional<T> defaultValue;
    /** 允许的期望取值列表。 */
    private final List<String> expectedValues;
    /** 是否严格限制为期望取值。 */
    private final boolean strictExpectedValues;
    /** 期望取值比较是否忽略大小写。 */
    private final boolean caseInsensitiveExpectedValues;
    /** 弃用元数据（可为 null）。 */
    private final DeprecatedMetadata deprecatedMetadata;
    /** 关联的兄弟/联动选项键集合。 */
    private final Set<String> connectedOptions;
    /** 通配符兄弟选项键名。 */
    private String wildcardKey;
    /** 是否为合成（synthetic）选项。 */
    private final boolean synthetic;

    /** 构造完整的 {@link Option} 实例。 */
    public Option(Class<T> type, String key, OptionCategory category, boolean hidden, boolean buildTime, String description,
                  Optional<T> defaultValue, List<String> expectedValues, boolean strictExpectedValues, boolean caseInsensitiveExpectedValues,
                  DeprecatedMetadata deprecatedMetadata, Set<String> connectedOptions, String wildcardKey, Class<?> componentType, boolean synthetic) {
        this.type = type;
        this.key = key;
        this.category = category;
        this.hidden = hidden;
        this.buildTime = buildTime;
        this.description = getDescriptionByCategorySupportLevel(description, category);
        this.defaultValue = defaultValue;
        this.expectedValues = expectedValues;
        this.strictExpectedValues = strictExpectedValues;
        this.caseInsensitiveExpectedValues = caseInsensitiveExpectedValues;
        this.deprecatedMetadata = deprecatedMetadata;
        this.connectedOptions = connectedOptions;
        this.wildcardKey = wildcardKey;
        this.componentType = componentType;
        this.synthetic = synthetic;
    }

    /** @return 选项值的 Java 类型 */
    public Class<T> getType() {
        return type;
    }

    /** @return 是否在文档中隐藏 */
    public boolean isHidden() { return hidden; }

    /** @return 是否为构建期选项 */
    public boolean isBuildTime() {
        return buildTime;
    }

    /** @return 配置键名 */
    public String getKey() {
        return key;
    }

    /** @return 选项分类 */
    public OptionCategory getCategory() {
        return category;
    }

    /** @return 选项描述文本 */
    public String getDescription() { return description; }

    /** @return 默认值（可能为空） */
    public Optional<T> getDefaultValue() {
        return defaultValue;
    }

    /**
     * 若 {@link #isStrictExpectedValues()} 为 false，则允许在期望取值之外提供自定义值；
     * 否则仅可使用列出的期望取值。
     *
     * @return 期望取值列表
     */
    public List<String> getExpectedValues() {
        return expectedValues;
    }

    /**
     * 表示是否允许在期望取值之外提供自定义值。
     * 若为严格模式，提供未列出的自定义值将导致应用启动失败。
     *
     * @return 是否严格限制期望取值
     */
    public boolean isStrictExpectedValues() {
        return strictExpectedValues;
    }

    /** @return 期望取值比较是否忽略大小写 */
    public boolean isCaseInsensitiveExpectedValues() {
        return caseInsensitiveExpectedValues;
    }

    /** @return 弃用元数据（若存在） */
    public Optional<DeprecatedMetadata> getDeprecatedMetadata() {
        return Optional.ofNullable(deprecatedMetadata);
    }

    /** 返回带有运行时特定默认值的新选项副本。 */
    public Option<T> withRuntimeSpecificDefault(T defaultValue) {
        return toBuilder().defaultValue(defaultValue).build();
    }

    /**
     * 获取与当前选项存在关联关系的联动选项键集合。
     * 通常当设置当前选项时，联动选项也应一并配置。
     * <br>
     * 对非通配符选项目前尚无实际语义。
     *
     * @return 联动选项键集合
     */
    public Set<String> getConnectedOptions() {
        return connectedOptions; // return the set directly for ease of mutability
    }

    /**
     * 获取可使用命名键的通配符兄弟选项名，主要用于文档引用。
     * 例如 {@code db-username} 的通配符选项为 {@code db-username-<datasource>}。
     *
     * @return 通配符兄弟选项键（若存在）
     */
    public Optional<String> getWildcardKey() {
        return Optional.ofNullable(wildcardKey);
    }

    // 用于隐式设置命名通配符键
    void setWildcardKey(String wildcardKey) {
        this.wildcardKey = wildcardKey;
    }

    /** 基于此选项当前状态创建 {@link OptionBuilder}。 */
    public OptionBuilder<T> toBuilder() {
        var builder = new OptionBuilder<>(key, type)
                .category(category)
                .buildTime(buildTime)
                .description(description)
                .defaultValue(defaultValue)
                .expectedValues(expectedValues)
                .strictExpectedValues(strictExpectedValues)
                .caseInsensitiveExpectedValues(caseInsensitiveExpectedValues)
                .deprecatedMetadata(deprecatedMetadata)
                .wildcardKey(wildcardKey);

        if (hidden) {
            builder.hidden();
        }
        if (synthetic) {
            builder.synthetic();
        }
        return builder;
    }

    /** 按分类支持级别为描述添加 Preview/Experimental 前缀。 */
    private static String getDescriptionByCategorySupportLevel(String description, OptionCategory category) {
        if (description != null && !description.isBlank()) {
            switch (category.getSupportLevel()) {
            case PREVIEW:
                description = "Preview: " + description;
                break;
            case EXPERIMENTAL:
                description = "Experimental: " + description;
                break;
            default:
                break;
            }
        }

        return description;
    }

    /** 将默认值对象格式化为配置字符串（列表以逗号连接）。 */
    public static String getDefaultValueString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof List) {
            return ((List<?>) value).stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        return String.valueOf(value);
    }

    /**
     * 将枚举常量从 UPPER_UNDERSCORE 转为 lower-hyphen 形式。
     * 例如 HAS_SOMETHING → has-something。
     *
     * @param value 枚举常量名
     * @return 转换后的 kebab-case 字符串
     */
    public static String transformEnumValue(String value) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, value);
    }

    /** @return 集合元素或组件类型 */
    public Class<?> getComponentType() {
        return componentType;
    }

    /** @return 是否为合成选项 */
    public boolean isSynthetic() {
        return synthetic;
    }
}
