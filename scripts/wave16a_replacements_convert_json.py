"""Chinese JavaDoc replacements for Spring Boot 4.1.0 wave16a ApplicationConversionService + JsonValueWriter."""

APPLICATION_CONVERSION_SERVICE_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "/**\n * A specialization of {@link FormattingConversionService} configured by default with\n * converters and formatters appropriate for most Spring Boot applications.\n * <p>\n * Designed for direct instantiation but also exposes the static\n * {@link #addApplicationConverters} and\n * {@link #addApplicationFormatters(FormatterRegistry)} utility methods for ad-hoc use\n * against registry instance.\n *\n * @author Phillip Webb\n * @author Shixiong Guo\n * @since 2.0.0\n */",
        "/**\n * {@link FormattingConversionService} 的特化实现，默认注册适用于大多数 Spring Boot 应用的\n * 转换器与格式化器。\n * <p>\n * 可直接实例化，也提供静态工具方法 {@link #addApplicationConverters} 与\n * {@link #addApplicationFormatters(FormatterRegistry)}，便于向已有注册表追加配置。\n *\n * @author Phillip Webb\n * @author Shixiong Guo\n * @since 2.0.0\n */",
    ),
    (
        "\t/**\n\t * Return {@code true} if objects of {@code sourceType} can be converted to the\n\t * {@code targetType} and the converter has {@code Object.class} as a supported source\n\t * type.\n\t * @param sourceType the source type to test\n\t * @param targetType the target type to test\n\t * @return if conversion happens through an {@code ObjectTo...} converter\n\t * @since 2.4.3\n\t */",
        "\t/**\n\t * 若 {@code sourceType} 可通过以 {@code Object.class} 为源类型的 {@code ObjectTo...} 转换器\n\t * 转为 {@code targetType}，则返回 {@code true}。\n\t *\n\t * @param sourceType the source type to test 待测源类型\n\t * @param targetType the target type to test 待测目标类型\n\t * @return if conversion happens through an {@code ObjectTo...} converter 是否经由 ObjectTo 转换器\n\t * @since 2.4.3\n\t */",
    ),
    (
        "\t/**\n\t * Return a shared default application {@code ConversionService} instance, lazily\n\t * building it once needed.\n\t * <p>\n\t * Note: This method actually returns an {@link ApplicationConversionService}\n\t * instance. However, the {@code ConversionService} signature has been preserved for\n\t * binary compatibility.\n\t * @return the shared {@code ApplicationConversionService} instance (never\n\t * {@code null})\n\t */",
        "\t/**\n\t * 返回共享的默认应用 {@code ConversionService} 实例，按需懒加载构建。\n\t * <p>\n\t * 注意：实际返回 {@link ApplicationConversionService} 实例；\n\t * 为保持二进制兼容，方法签名仍为 {@code ConversionService}。\n\t *\n\t * @return the shared {@code ApplicationConversionService} instance (never\n\t * {@code null}) 共享实例（永不为 {@code null}）\n\t */",
    ),
    (
        "\t/**\n\t * Configure the given {@link FormatterRegistry} with formatters and converters\n\t * appropriate for most Spring Boot applications.\n\t * @param registry the registry of converters to add to (must also be castable to\n\t * ConversionService, e.g. being a {@link ConfigurableConversionService})\n\t * @throws ClassCastException if the given FormatterRegistry could not be cast to a\n\t * ConversionService\n\t */",
        "\t/**\n\t * 为给定 {@link FormatterRegistry} 配置适用于大多数 Spring Boot 应用的格式化器与转换器。\n\t *\n\t * @param registry the registry of converters to add to (must also be castable to\n\t * ConversionService, e.g. being a {@link ConfigurableConversionService}) 转换器注册表（须可转为 {@link ConversionService}）\n\t * @throws ClassCastException if the given FormatterRegistry could not be cast to a\n\t * ConversionService 无法转为 {@link ConversionService} 时\n\t */",
    ),
    (
        "\t/**\n\t * Add converters useful for most Spring Boot applications.\n\t * @param registry the registry of converters to add to (must also be castable to\n\t * ConversionService, e.g. being a {@link ConfigurableConversionService})\n\t * @throws ClassCastException if the given ConverterRegistry could not be cast to a\n\t * ConversionService\n\t */",
        "\t/**\n\t * 添加适用于大多数 Spring Boot 应用的转换器。\n\t *\n\t * @param registry the registry of converters to add to (must also be castable to\n\t * ConversionService, e.g. being a {@link ConfigurableConversionService}) 转换器注册表\n\t * @throws ClassCastException if the given ConverterRegistry could not be cast to a\n\t * ConversionService 无法转为 {@link ConversionService} 时\n\t */",
    ),
    (
        "\t/**\n\t * Add converters to support delimited strings.\n\t * @param registry the registry of converters to add to (must also be castable to\n\t * ConversionService, e.g. being a {@link ConfigurableConversionService})\n\t * @throws ClassCastException if the given ConverterRegistry could not be cast to a\n\t * ConversionService\n\t */",
        "\t/**\n\t * 添加支持分隔字符串的转换器。\n\t *\n\t * @param registry the registry of converters to add to (must also be castable to\n\t * ConversionService, e.g. being a {@link ConfigurableConversionService}) 转换器注册表\n\t * @throws ClassCastException if the given ConverterRegistry could not be cast to a\n\t * ConversionService 无法转为 {@link ConversionService} 时\n\t */",
    ),
    (
        "\t/**\n\t * Add formatters useful for most Spring Boot applications.\n\t * @param registry the service to register default formatters with\n\t */",
        "\t/**\n\t * 添加适用于大多数 Spring Boot 应用的格式化器。\n\t *\n\t * @param registry the service to register default formatters with 格式化器注册表\n\t */",
    ),
    (
        "\t/**\n\t * Add {@link Printer}, {@link Parser}, {@link Formatter}, {@link Converter},\n\t * {@link ConverterFactory}, {@link GenericConverter}, and beans from the specified\n\t * bean factory.\n\t * @param registry the service to register beans with\n\t * @param beanFactory the bean factory to get the beans from\n\t * @since 2.2.0\n\t */",
        "\t/**\n\t * 从指定 Bean 工厂注册 {@link Printer}、{@link Parser}、{@link Formatter}、\n\t * {@link Converter}、{@link ConverterFactory}、{@link GenericConverter} 等 Bean。\n\t *\n\t * @param registry the service to register beans with 注册目标\n\t * @param beanFactory the bean factory to get the beans from Bean 工厂\n\t * @since 2.2.0\n\t */",
    ),
    (
        "\t/**\n\t * Add {@link Printer}, {@link Parser}, {@link Formatter}, {@link Converter},\n\t * {@link ConverterFactory}, {@link GenericConverter}, and beans from the specified\n\t * bean factory.\n\t * @param registry the service to register beans with\n\t * @param beanFactory the bean factory to get the beans from\n\t * @param qualifier the qualifier required on the beans or {@code null}\n\t * @return the beans that were added\n\t * @since 3.5.0\n\t */",
        "\t/**\n\t * 从指定 Bean 工厂注册转换/格式化相关 Bean，可按限定符筛选。\n\t *\n\t * @param registry the service to register beans with 注册目标\n\t * @param beanFactory the bean factory to get the beans from Bean 工厂\n\t * @param qualifier the qualifier required on the beans or {@code null} Bean 限定符（可为 {@code null}）\n\t * @return the beans that were added 已添加的 Bean\n\t * @since 3.5.0\n\t */",
    ),
    (
        "\t/**\n\t * Base class for adapters that adapt a bean to a {@link GenericConverter}.\n\t *\n\t * @param <B> the base type of the bean\n\t */",
        "\t/**\n\t * 将 Bean 适配为 {@link GenericConverter} 的适配器基类。\n\t *\n\t * @param <B> the base type of the bean Bean 基类型\n\t */",
    ),
    (
        "\t/**\n\t * Adapts a {@link Printer} bean to a {@link GenericConverter}.\n\t */",
        "\t/**\n\t * 将 {@link Printer} Bean 适配为 {@link GenericConverter}。\n\t */",
    ),
    (
        "\t/**\n\t * Adapts a {@link Parser} bean to a {@link GenericConverter}.\n\t */",
        "\t/**\n\t * 将 {@link Parser} Bean 适配为 {@link GenericConverter}。\n\t */",
    ),
    (
        "\t/**\n\t * Adapts a {@link Converter} bean to a {@link GenericConverter}.\n\t */",
        "\t/**\n\t * 将 {@link Converter} Bean 适配为 {@link GenericConverter}。\n\t */",
    ),
    (
        "\t/**\n\t * Adapts a {@link ConverterFactory} bean to a {@link GenericConverter}.\n\t */",
        "\t/**\n\t * 将 {@link ConverterFactory} Bean 适配为 {@link GenericConverter}。\n\t */",
    ),
    (
        "\t/**\n\t * Convertible type information as extracted from bean generics.\n\t *\n\t * @param source the source type\n\t * @param target the target type\n\t */",
        "\t/**\n\t * 从 Bean 泛型提取的可转换类型信息。\n\t *\n\t * @param source the source type 源类型\n\t * @param target the target type 目标类型\n\t */",
    ),
]

JSON_VALUE_WRITER_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "/**\n * Internal class used by {@link JsonWriter} to handle the lower-level concerns of writing\n * JSON.\n *\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
        "/**\n * {@link JsonWriter} 内部使用的类，负责 JSON 写入的底层细节。\n *\n * @author Phillip Webb\n * @author Moritz Halbritter\n */",
    ),
    (
        "\t/**\n\t * Create a new {@link JsonValueWriter} instance.\n\t * @param out the {@link Appendable} used to receive the JSON output\n\t */",
        "\t/**\n\t * 创建新的 {@link JsonValueWriter} 实例。\n\t *\n\t * @param out the {@link Appendable} used to receive the JSON output 接收 JSON 输出的 {@link Appendable}\n\t */",
    ),
    (
        "\t/**\n\t * Create a new {@link JsonValueWriter} instance.\n\t * @param out the {@link Appendable} used to receive the JSON output\n\t * @param maxNestingDepth the maximum allowed nesting depth for JSON objects and\n\t * arrays\n\t */",
        "\t/**\n\t * 创建新的 {@link JsonValueWriter} 实例。\n\t *\n\t * @param out the {@link Appendable} used to receive the JSON output 接收 JSON 输出的 {@link Appendable}\n\t * @param maxNestingDepth the maximum allowed nesting depth for JSON objects and\n\t * arrays JSON 对象/数组允许的最大嵌套深度\n\t */",
    ),
    (
        "\t/**\n\t * Write a name value pair, or just a value if {@code name} is {@code null}.\n\t * @param <N> the name type in the pair\n\t * @param <V> the value type in the pair\n\t * @param name the name of the pair or {@code null} if only the value should be\n\t * written\n\t * @param value the value\n\t */",
        "\t/**\n\t * 写入名值对；若 {@code name} 为 {@code null} 则仅写入值。\n\t *\n\t * @param <N> the name type in the pair 名称类型\n\t * @param <V> the value type in the pair 值类型\n\t * @param name the name of the pair or {@code null} if only the value should be\n\t * written 名称；仅写值时为 {@code null}\n\t * @param value the value 值\n\t */",
    ),
    (
        "/**\n\t * Write a value to the JSON output. The following value types are supported:\n\t * <ul>\n\t * <li>Any {@code null} value</li>\n\t * <li>A {@link WritableJson} instance</li>\n\t * <li>Any {@link Iterable} or Array (written as a JSON array)</li>\n\t * <li>A {@link Map} (written as a JSON object)</li>\n\t * <li>Any {@link Number}</li>\n\t * <li>A {@link Boolean}</li>\n\t * </ul>\n\t * All other values are written as JSON strings.\n\t * @param <V> the value type\n\t * @param value the value to write\n\t */",
        "\t/**\n\t * 将值写入 JSON 输出。支持以下类型：\n\t * <ul>\n\t * <li>任意 {@code null}</li>\n\t * <li>{@link WritableJson} 实例</li>\n\t * <li>任意 {@link Iterable} 或数组（写为 JSON 数组）</li>\n\t * <li>{@link Map}（写为 JSON 对象）</li>\n\t * <li>任意 {@link Number}</li>\n\t * <li>{@link Boolean}</li>\n\t * </ul>\n\t * 其他值均写为 JSON 字符串。\n\t *\n\t * @param <V> the value type 值类型\n\t * @param value the value to write 待写入的值\n\t */",
    ),
    (
        "\t/**\n\t * Start a new {@link Series} (JSON object or array).\n\t * @param series the series to start\n\t * @see #end(Series)\n\t * @see #writePairs(Consumer)\n\t * @see #writeElements(Consumer)\n\t */",
        "\t/**\n\t * 开始新的 {@link Series}（JSON 对象或数组）。\n\t *\n\t * @param series the series to start 要开始的系列\n\t * @see #end(Series)\n\t * @see #writePairs(Consumer)\n\t * @see #writeElements(Consumer)\n\t */",
    ),
    (
        "\t/**\n\t * End an active {@link Series} (JSON object or array).\n\t * @param series the series type being ended (must match {@link #start(Series)})\n\t * @see #start(Series)\n\t */",
        "\t/**\n\t * 结束当前活动的 {@link Series}（JSON 对象或数组）。\n\t *\n\t * @param series the series type being ended (must match {@link #start(Series)}) 要结束的系列（须与 {@link #start(Series)} 一致）\n\t * @see #start(Series)\n\t */",
    ),
    (
        "\t/**\n\t * Write the specified elements to a newly started {@link Series#ARRAY array series}.\n\t * @param <E> the element type\n\t * @param elements a callback that will be used to provide each element. Typically a\n\t * {@code forEach} method reference.\n\t * @see #writeElements(Consumer)\n\t */",
        "\t/**\n\t * 将元素写入新开始的 {@link Series#ARRAY 数组系列}。\n\t *\n\t * @param <E> the element type 元素类型\n\t * @param elements a callback that will be used to provide each element. Typically a\n\t * {@code forEach} method reference 提供各元素的回调（通常为 {@code forEach} 方法引用）\n\t * @see #writeElements(Consumer)\n\t */",
    ),
    (
        "\t/**\n\t * Write the specified elements to an already started {@link Series#ARRAY array\n\t * series}.\n\t * @param <E> the element type\n\t * @param elements a callback that will be used to provide each element. Typically a\n\t * {@code forEach} method reference.\n\t * @see #writeElements(Consumer)\n\t */",
        "\t/**\n\t * 将元素写入已开始的 {@link Series#ARRAY 数组系列}。\n\t *\n\t * @param <E> the element type 元素类型\n\t * @param elements a callback that will be used to provide each element. Typically a\n\t * {@code forEach} method reference 提供各元素的回调\n\t * @see #writeElements(Consumer)\n\t */",
    ),
    (
        "\t/**\n\t * Write the specified pairs to a newly started {@link Series#OBJECT object series}.\n\t * @param <N> the name type in the pair\n\t * @param <V> the value type in the pair\n\t * @param pairs a callback that will be used to provide each pair. Typically a\n\t * {@code forEach} method reference.\n\t * @see #writePairs(Consumer)\n\t */",
        "\t/**\n\t * 将名值对写入新开始的 {@link Series#OBJECT 对象系列}。\n\t *\n\t * @param <N> the name type in the pair 名称类型\n\t * @param <V> the value type in the pair 值类型\n\t * @param pairs a callback that will be used to provide each pair. Typically a\n\t * {@code forEach} method reference 提供各名值对的回调\n\t * @see #writePairs(Consumer)\n\t */",
    ),
    (
        "\t/**\n\t * Write the specified pairs to an already started {@link Series#OBJECT object\n\t * series}.\n\t * @param <N> the name type in the pair\n\t * @param <V> the value type in the pair\n\t * @param pairs a callback that will be used to provide each pair. Typically a\n\t * {@code forEach} method reference.\n\t * @see #writePairs(Consumer)\n\t */",
        "\t/**\n\t * 将名值对写入已开始的 {@link Series#OBJECT 对象系列}。\n\t *\n\t * @param <N> the name type in the pair 名称类型\n\t * @param <V> the value type in the pair 值类型\n\t * @param pairs a callback that will be used to provide each pair. Typically a\n\t * {@code forEach} method reference 提供各名值对的回调\n\t * @see #writePairs(Consumer)\n\t */",
    ),
    (
        "\t// Lambda isn't detected with the correct nullability",
        "\t// Lambda 的空安全注解无法正确识别",
    ),
    (
        "\t/**\n\t * A series of items that can be written to the JSON output.\n\t */",
        "\t/**\n\t * 可写入 JSON 输出的项序列。\n\t */",
    ),
    (
        "\t\t/**\n\t\t * A JSON object series consisting of name/value pairs.\n\t\t */",
        "\t\t/**\n\t\t * 由名值对组成的 JSON 对象序列。\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * A JSON array series consisting of elements.\n\t\t */",
        "\t\t/**\n\t\t * 由元素组成的 JSON 数组序列。\n\t\t */",
    ),
    (
        "\t/**\n\t * Details of the currently active {@link Series}.\n\t */",
        "\t/**\n\t * 当前活动 {@link Series} 的详细信息。\n\t */",
    ),
]
