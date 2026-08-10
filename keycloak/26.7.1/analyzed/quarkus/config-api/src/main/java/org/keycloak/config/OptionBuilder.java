package org.keycloak.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.smallrye.common.constraint.Assert;

/**
 * 流式构建 {@link Option} 实例的建造器。
 *
 * @param <T> 选项值的 Java 类型
 */
@SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType", "rawtypes"})
public class OptionBuilder<T> {

    /** 布尔类型选项的期望字符串取值。 */
    private static final List<String> BOOLEAN_TYPE_VALUES = List.of(Boolean.TRUE.toString(), Boolean.FALSE.toString());

    private final Class<T> type;
    private final Class<?> auxiliaryType;
    private final Set<String> connectedOptions = new HashSet<>();

    private String key;
    private OptionCategory category;
    private boolean hidden;
    private boolean build;
    private String description;
    private Optional<T> defaultValue;
    private List<String> expectedValues;
    private boolean transformEnumValues;
    // 表示是否允许在期望取值之外提供自定义值
    private boolean strictExpectedValues;
    private boolean caseInsensitiveExpectedValues;
    private DeprecatedMetadata deprecatedMetadata;
    private String wildcardKey;

    private boolean synthetic;

    /** 创建 List 类型多值选项的建造器。 */
    public static <A> OptionBuilder<List<A>> listOptionBuilder(String key, Class<A> type) {
        return new OptionBuilder(key, List.class, type);
    }

    /** 以键名与值类型创建建造器。 */
    public OptionBuilder(String key, Class<T> type) {
        this(key, type, null);
    }

    private OptionBuilder(String key, Class<T> type, Class<?> auxiliaryType) {
        this.type = type;
        this.auxiliaryType = auxiliaryType;
        if (type.isArray() || ((Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) && type != java.util.List.class)) {
            throw new IllegalArgumentException("Non-List multi-valued options are not yet supported");
        }
        this.key = key;
        category = OptionCategory.GENERAL;
        hidden = false;
        build = false;
        description = null;
        strictExpectedValues = true;
    }

    /** 设置配置键名（包内使用）。 */
    OptionBuilder<T> key(String key) {
        this.key = key;
        return this;
    }

    /** 设置选项分类。 */
    public OptionBuilder<T> category(OptionCategory category) {
        this.category = category;
        return this;
    }

    /** 标记为在帮助/文档中隐藏。 */
    public OptionBuilder<T> hidden() {
        this.hidden = true;
        return this;
    }

    /** 设置是否为构建期选项。 */
    public OptionBuilder<T> buildTime(boolean build) {
        this.build = build;
        return this;
    }

    /** 设置人类可读描述。 */
    public OptionBuilder<T> description(String description) {
        this.description = description;
        return this;
    }

    /** 以 {@link Optional} 设置默认值。 */
    public OptionBuilder<T> defaultValue(Optional<T> defaultV) {
        this.defaultValue = defaultV;
        return this;
    }

    /** 设置默认值。 */
    public OptionBuilder<T> defaultValue(T defaultV) {
        this.defaultValue = Optional.ofNullable(defaultV);
        return this;
    }

    /** 设置期望取值字符串列表。 */
    public OptionBuilder<T> expectedValues(List<String> expected) {
        Assert.assertNotNull(expected);
        this.expectedValues = expected;
        return this;
    }

    /** 从枚举类型推导期望取值。 */
    public OptionBuilder<T> expectedValues(Class<? extends Enum> expected) {
        return expectedValues(Stream.of(expected.getEnumConstants()).map(Object::toString).collect(Collectors.toList()));
    }

    /** 以可变参数设置期望取值。 */
    public OptionBuilder<T> expectedValues(T ... expected) {
        return expectedValues(Stream.of(expected).map(Object::toString).collect(Collectors.toList()));
    }

    /**
     * 是否将枚举期望取值转换为 kebab-case，详见 {@link Option#transformEnumValue(String)}。
     */
    public OptionBuilder<T> transformEnumValues(boolean transform) {
        this.transformEnumValues = transform;
        return this;
    }

    /** 设置是否严格限制为期望取值。 */
    public OptionBuilder<T> strictExpectedValues(boolean strictExpectedValues) {
        this.strictExpectedValues = strictExpectedValues;
        return this;
    }

    /** 设置期望取值比较是否忽略大小写。 */
    public OptionBuilder<T> caseInsensitiveExpectedValues(boolean caseInsensitiveExpectedValues) {
        this.caseInsensitiveExpectedValues = caseInsensitiveExpectedValues;
        return this;
    }

    /** 标记整个选项为已弃用。 */
    public OptionBuilder<T> deprecated() {
        this.deprecatedMetadata = DeprecatedMetadata.deprecateOption(null);
        return this;
    }

    /** 设置完整的弃用元数据。 */
    public OptionBuilder<T> deprecatedMetadata(DeprecatedMetadata deprecatedMetadata) {
        this.deprecatedMetadata = deprecatedMetadata;
        return this;
    }

    /** 标记部分取值为已弃用。 */
    public OptionBuilder<T> deprecatedValues(String note, T... values) {
        this.deprecatedMetadata = DeprecatedMetadata.deprecateValues(note, Stream.of(values).map(Object::toString).toArray(String[]::new));
        return this;
    }

    /**
     * 声明联动选项，详见 {@link Option#getConnectedOptions()}。
     */
    public OptionBuilder<T> connectedOptions(Option<?>... connectedOptions) {
        this.connectedOptions.addAll(Arrays.stream(connectedOptions).map(Option::getKey).collect(Collectors.toSet()));
        return this;
    }

    /**
     * 设置通配符兄弟选项键，详见 {@link Option#getWildcardKey()}。
     */
    public OptionBuilder<T> wildcardKey(String wildcardKey) {
        this.wildcardKey = wildcardKey;
        return this;
    }

    /** 构建不可变的 {@link Option} 实例。 */
    public Option<T> build() {
        if (deprecatedMetadata == null && category.getSupportLevel() == ConfigSupportLevel.DEPRECATED) {
            deprecated();
        }

        Class<?> expected = type;
        if (auxiliaryType != null) {
            expected = auxiliaryType;
        }

        boolean isEnumType = Enum.class.isAssignableFrom(expected);

        if (expectedValues == null) {
            if (Boolean.class.equals(expected)) {
                expectedValues(BOOLEAN_TYPE_VALUES);
            } else if (isEnumType) {
                expectedValues((Class<? extends Enum>) expected);
            } else {
                expectedValues = List.of();
            }
        }

        if (defaultValue == null) {
            if (Boolean.class.equals(expected)) {
                defaultValue = Optional.of((T) Boolean.FALSE);
            } else {
                defaultValue = Optional.empty();
            }
        }

        if (transformEnumValues) {
            if (isEnumType) {
                expectedValues(expectedValues.stream().map(Option::transformEnumValue).toList());
                defaultValue.ifPresent(t -> defaultValue(Optional.of((T) Option.transformEnumValue(t.toString()))));
            } else {
                throw new IllegalArgumentException("You can use 'transformEnumValues' only for Enum types");
            }
        }

        return new Option<T>(type, key, category, hidden || synthetic, build, description, defaultValue, expectedValues, strictExpectedValues, caseInsensitiveExpectedValues, deprecatedMetadata, connectedOptions, wildcardKey, expected, synthetic);
    }

    /** 标记为合成（synthetic）选项。 */
    public OptionBuilder<T> synthetic() {
        this.synthetic = true;
        return this;
    }

}
