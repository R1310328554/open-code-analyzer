"""Chinese JavaDoc replacements for Spring Boot 4.1.0 wave16a PropertyMapper."""

PROPERTY_MAPPER_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "/**\n * Utility that can be used to map values from a supplied source to a destination.\n * Primarily intended to be help when mapping from\n * {@link ConfigurationProperties @ConfigurationProperties} to third-party classes.\n * <p>\n * Can filter values based on predicates and adapt values if needed. For example:\n * <pre class=\"code\">\n * PropertyMapper map = PropertyMapper.get();\n * map.from(source::getName)\n *   .to(destination::setName);\n * map.from(source::getTimeout)\n *   .when(this::thisYear)\n *   .asInt(Duration::getSeconds)\n *   .to(destination::setTimeoutSecs);\n * map.from(source::isEnabled)\n *   .whenFalse().\n *   .toCall(destination::disable);\n * </pre>\n * <p>\n * Mappings can ultimately be applied to a {@link Source#to(Consumer) setter}, trigger a\n * {@link Source#toCall(Runnable) method call} or create a\n * {@link Source#toInstance(Function) new instance}.\n * <p>\n * By default {@code null} values and any {@link NullPointerException} thrown from the\n * supplier are filtered and will not be applied to consumers. If you want to apply nulls,\n * you can use {@link Source#always()}.\n *\n * @author Phillip Webb\n * @author Artsiom Yudovin\n * @author Chris Bono\n * @author Moritz Halbritter\n * @since 2.0.0\n */",
        "/**\n * 将给定源中的值映射到目标对象的工具类。\n * 主要用于从 {@link ConfigurationProperties @ConfigurationProperties} 映射到第三方类。\n * <p>\n * 可按谓词过滤值，并在需要时适配值。例如：\n * <pre class=\"code\">\n * PropertyMapper map = PropertyMapper.get();\n * map.from(source::getName)\n *   .to(destination::setName);\n * map.from(source::getTimeout)\n *   .when(this::thisYear)\n *   .asInt(Duration::getSeconds)\n *   .to(destination::setTimeoutSecs);\n * map.from(source::isEnabled)\n *   .whenFalse().\n *   .toCall(destination::disable);\n * </pre>\n * <p>\n * 映射最终可应用到 {@link Source#to(Consumer) setter}、触发\n * {@link Source#toCall(Runnable) 方法调用}，或创建\n * {@link Source#toInstance(Function) 新实例}。\n * <p>\n * 默认会过滤 {@code null} 值以及 supplier 抛出的 {@link NullPointerException}，\n * 不会将其传递给 consumer。若需应用 null，可使用 {@link Source#always()}。\n *\n * @author Phillip Webb\n * @author Artsiom Yudovin\n * @author Chris Bono\n * @author Moritz Halbritter\n * @since 2.0.0\n */",
    ),
    (
        "\t/**\n\t * Return a new {@link PropertyMapper} instance that applies the given\n\t * {@link SourceOperator} to every source.\n\t * @param operator the source operator to apply\n\t * @return a new property mapper instance\n\t */",
        "\t/**\n\t * 返回新的 {@link PropertyMapper} 实例，对每个 {@link Source} 应用给定 {@link SourceOperator}。\n\t *\n\t * @param operator the source operator to apply 要应用的源操作\n\t * @return a new property mapper instance 新的 PropertyMapper 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link Source} from the specified value that can be used to perform\n\t * the mapping.\n\t * @param <T> the source type\n\t * @param value the value\n\t * @return a {@link Source} that can be used to complete the mapping\n\t */",
        "\t/**\n\t * 从指定值创建新的 {@link Source}，用于完成映射。\n\t *\n\t * @param <T> the source type 源类型\n\t * @param value the value 源值\n\t * @return a {@link Source} that can be used to complete the mapping 可用于完成映射的 {@link Source}\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link Source} from the specified value supplier that can be used to\n\t * perform the mapping.\n\t * @param <T> the source type\n\t * @param supplier the value supplier\n\t * @return a {@link Source} that can be used to complete the mapping\n\t * @see #from(Object)\n\t */",
        "\t/**\n\t * 从指定值 supplier 创建新的 {@link Source}，用于完成映射。\n\t *\n\t * @param <T> the source type 源类型\n\t * @param supplier the value supplier 值 supplier\n\t * @return a {@link Source} that can be used to complete the mapping 可用于完成映射的 {@link Source}\n\t * @see #from(Object)\n\t */",
    ),
    (
        "\t/**\n\t * Return the property mapper.\n\t * @return the property mapper\n\t */",
        "\t/**\n\t * 返回 PropertyMapper 单例。\n\t *\n\t * @return the property mapper PropertyMapper 实例\n\t */",
    ),
    (
        "\t/**\n\t * An operation that can be applied to a {@link Source}.\n\t */",
        "\t/**\n\t * 可应用于 {@link Source} 的操作。\n\t */",
    ),
    (
        "\t\t/**\n\t\t * Apply the operation to the given source.\n\t\t * @param <T> the source type\n\t\t * @param source the source to operate on\n\t\t * @return the updated source\n\t\t */",
        "\t\t/**\n\t\t * 对给定 {@link Source} 应用操作。\n\t\t *\n\t\t * @param <T> the source type 源类型\n\t\t * @param source the source to operate on 待操作的源\n\t\t * @return the updated source 更新后的源\n\t\t */",
    ),
    (
        "\t/**\n\t * A source that is in the process of being mapped.\n\t *\n\t * @param <T> the source type\n\t */",
        "\t/**\n\t * 正在映射过程中的源。\n\t *\n\t * @param <T> the source type 源类型\n\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a source that will use the given supplier to obtain a fallback value to\n\t\t * use in place of {@code null}.\n\t\t * @param fallback the fallback supplier\n\t\t * @return a new {@link Source} instance\n\t\t * @since 4.0.0\n\t\t */",
        "\t\t/**\n\t\t * 返回在值为 {@code null} 时使用给定 supplier 获取回退值的新 {@link Source}。\n\t\t *\n\t\t * @param fallback the fallback supplier 回退值 supplier\n\t\t * @return a new {@link Source} instance 新的 {@link Source} 实例\n\t\t * @since 4.0.0\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return an adapted version of the source with {@link Integer} type.\n\t\t * @param <R> the resulting type\n\t\t * @param adapter an adapter to convert the current value to a number.\n\t\t * @return a new adapted source instance\n\t\t */",
        "\t\t/**\n\t\t * 返回适配为 {@link Integer} 类型的源。\n\t\t *\n\t\t * @param <R> the resulting type 结果数值类型\n\t\t * @param adapter an adapter to convert the current value to a number 将当前值转为数值的适配器\n\t\t * @return a new adapted source instance 新的适配源实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return an adapted version of the source changed through the given adapter\n\t\t * function.\n\t\t * @param <R> the resulting type\n\t\t * @param adapter the adapter to apply\n\t\t * @return a new adapted source instance\n\t\t */",
        "\t\t/**\n\t\t * 通过给定适配函数返回转换后的源。\n\t\t *\n\t\t * @param <R> the resulting type 结果类型\n\t\t * @param adapter the adapter to apply 要应用的适配器\n\t\t * @return a new adapted source instance 新的适配源实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a filtered version of the source that will only map values that are\n\t\t * {@code true}.\n\t\t * @return a new filtered source instance\n\t\t */",
        "\t\t/**\n\t\t * 返回仅映射 {@code true} 值的过滤源。\n\t\t *\n\t\t * @return a new filtered source instance 新的过滤源实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a filtered version of the source that will only map values that are\n\t\t * {@code false}.\n\t\t * @return a new filtered source instance\n\t\t */",
        "\t\t/**\n\t\t * 返回仅映射 {@code false} 值的过滤源。\n\t\t *\n\t\t * @return a new filtered source instance 新的过滤源实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a filtered version of the source that will only map values that have a\n\t\t * {@code toString()} containing actual text.\n\t\t * @return a new filtered source instance\n\t\t */",
        "\t\t/**\n\t\t * 返回仅映射 {@code toString()} 含实际文本的值的过滤源。\n\t\t *\n\t\t * @return a new filtered source instance 新的过滤源实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a filtered version of the source that will only map values equal to the\n\t\t * specified {@code object}.\n\t\t * @param object the object to match\n\t\t * @return a new filtered source instance\n\t\t */",
        "\t\t/**\n\t\t * 返回仅映射与指定 {@code object} 相等的值的过滤源。\n\t\t *\n\t\t * @param object the object to match 要匹配的对象\n\t\t * @return a new filtered source instance 新的过滤源实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a filtered version of the source that will only map values that are an\n\t\t * instance of the given type.\n\t\t * @param <R> the target type\n\t\t * @param target the target type to match\n\t\t * @return a new filtered source instance\n\t\t */",
        "\t\t/**\n\t\t * 返回仅映射为给定类型实例的值的过滤源。\n\t\t *\n\t\t * @param <R> the target type 目标类型\n\t\t * @param target the target type to match 要匹配的目标类型\n\t\t * @return a new filtered source instance 新的过滤源实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a filtered version of the source that won't map values that match the\n\t\t * given predicate.\n\t\t * @param predicate the predicate used to filter values\n\t\t * @return a new filtered source instance\n\t\t */",
        "\t\t/**\n\t\t * 返回不映射满足给定谓词的值的过滤源。\n\t\t *\n\t\t * @param predicate the predicate used to filter values 用于过滤值的谓词\n\t\t * @return a new filtered source instance 新的过滤源实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a filtered version of the source that won't map values that don't match\n\t\t * the given predicate.\n\t\t * @param predicate the predicate used to filter values\n\t\t * @return a new filtered source instance\n\t\t */",
        "\t\t/**\n\t\t * 返回仅映射满足给定谓词的值的过滤源。\n\t\t *\n\t\t * @param predicate the predicate used to filter values 用于过滤值的谓词\n\t\t * @return a new filtered source instance 新的过滤源实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Complete the mapping by passing any non-filtered value to the specified\n\t\t * consumer. The method is designed to be used with mutable objects.\n\t\t * @param consumer the consumer that should accept the value if it's not been\n\t\t * filtered\n\t\t */",
        "\t\t/**\n\t\t * 将未过滤的值传递给指定 consumer 以完成映射。\n\t\t * 适用于可变对象。\n\t\t *\n\t\t * @param consumer the consumer that should accept the value if it's not been\n\t\t * filtered 接收未过滤值的 consumer\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Complete the mapping for any non-filtered value by applying the given function\n\t\t * to an existing instance and returning a new one. For filtered values, the\n\t\t * {@code instance} parameter is returned unchanged. The method is designed to be\n\t\t * used with immutable objects.\n\t\t * @param <R> the result type\n\t\t * @param instance the current instance\n\t\t * @param mapper the mapping function\n\t\t * @return a new mapped instance or the original instance\n\t\t * @since 3.0.0\n\t\t */",
        "\t\t/**\n\t\t * 对未过滤值将给定函数应用于现有实例并返回新实例以完成映射。\n\t\t * 值被过滤时原样返回 {@code instance}。适用于不可变对象。\n\t\t *\n\t\t * @param <R> the result type 结果类型\n\t\t * @param instance the current instance 当前实例\n\t\t * @param mapper the mapping function 映射函数\n\t\t * @return a new mapped instance or the original instance 新映射实例或原实例\n\t\t * @since 3.0.0\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Complete the mapping by creating a new instance from the non-filtered value.\n\t\t * @param <R> the resulting type\n\t\t * @param factory the factory used to create the instance\n\t\t * @return the instance\n\t\t * @throws NoSuchElementException if the value has been filtered\n\t\t */",
        "\t\t/**\n\t\t * 从未过滤值创建新实例以完成映射。\n\t\t *\n\t\t * @param <R> the resulting type 结果类型\n\t\t * @param factory the factory used to create the instance 创建实例的工厂\n\t\t * @return the instance 新实例\n\t\t * @throws NoSuchElementException if the value has been filtered 值已被过滤时\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Complete the mapping by calling the specified method when the value has not\n\t\t * been filtered.\n\t\t * @param runnable the method to call if the value has not been filtered\n\t\t */",
        "\t\t/**\n\t\t * 值未被过滤时调用指定方法以完成映射。\n\t\t *\n\t\t * @param runnable the method to call if the value has not been filtered 未过滤时要调用的方法\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a version of this source that can be used to always complete mappings,\n\t\t * even if values are {@code null}.\n\t\t * @return a new {@link Always} instance\n\t\t * @since 4.0.0\n\t\t */",
        "\t\t/**\n\t\t * 返回即使值为 {@code null} 也能完成映射的源版本。\n\t\t *\n\t\t * @return a new {@link Always} instance 新的 {@link Always} 实例\n\t\t * @since 4.0.0\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Adapter used to adapt a value and possibly return a {@code null} result.\n\t\t *\n\t\t * @param <T> the source type\n\t\t * @param <R> the result type\n\t\t * @since 4.0.0\n\t\t */",
        "\t\t/**\n\t\t * 适配值并可能返回 {@code null} 的适配器。\n\t\t *\n\t\t * @param <T> the source type 源类型\n\t\t * @param <R> the result type 结果类型\n\t\t * @since 4.0.0\n\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Adapt the given value.\n\t\t\t * @param value the value to adapt\n\t\t\t * @return an adapted value or {@code null}\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 适配给定值。\n\t\t\t *\n\t\t\t * @param value the value to adapt 待适配的值\n\t\t\t * @return an adapted value or {@code null} 适配后的值或 {@code null}\n\t\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Allow source mapping to complete using methods that accept nulls.\n\t\t *\n\t\t * @param <T> the source type\n\t\t * @since 4.0.0\n\t\t */",
        "\t\t/**\n\t\t * 允许使用可接受 null 的方法完成源映射。\n\t\t *\n\t\t * @param <T> the source type 源类型\n\t\t * @since 4.0.0\n\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Return an adapted version of the source changed through the given adapter\n\t\t\t * function.\n\t\t\t * @param <R> the resulting type\n\t\t\t * @param adapter the adapter to apply\n\t\t\t * @return a new adapted source instance\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 通过给定适配函数返回转换后的源。\n\t\t\t *\n\t\t\t * @param <R> the resulting type 结果类型\n\t\t\t * @param adapter the adapter to apply 要应用的适配器\n\t\t\t * @return a new adapted source instance 新的适配源实例\n\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Complete the mapping by passing any non-filtered value to the specified\n\t\t\t * consumer. The method is designed to be used with mutable objects.\n\t\t\t * @param consumer the consumer that should accept the value if it's not been\n\t\t\t * filtered\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 将未过滤的值传递给指定 consumer 以完成映射。\n\t\t\t * 适用于可变对象。\n\t\t\t *\n\t\t\t * @param consumer the consumer that should accept the value if it's not been\n\t\t\t * filtered 接收未过滤值的 consumer\n\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Complete the mapping for any non-filtered value by applying the given\n\t\t\t * function to an existing instance and returning a new one. For filtered\n\t\t\t * values, the {@code instance} parameter is returned unchanged. The method is\n\t\t\t * designed to be used with immutable objects.\n\t\t\t * @param <R> the result type\n\t\t\t * @param instance the current instance\n\t\t\t * @param mapper the mapping function\n\t\t\t * @return a new mapped instance or the original instance\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 对未过滤值将给定函数应用于现有实例并返回新实例以完成映射。\n\t\t\t * 值被过滤时原样返回 {@code instance}。适用于不可变对象。\n\t\t\t *\n\t\t\t * @param <R> the result type 结果类型\n\t\t\t * @param instance the current instance 当前实例\n\t\t\t * @param mapper the mapping function 映射函数\n\t\t\t * @return a new mapped instance or the original instance 新映射实例或原实例\n\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Complete the mapping by creating a new instance from the non-filtered\n\t\t\t * value.\n\t\t\t * @param <R> the resulting type\n\t\t\t * @param factory the factory used to create the instance\n\t\t\t * @return the instance\n\t\t\t * @throws NoSuchElementException if the value has been filtered\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 从未过滤值创建新实例以完成映射。\n\t\t\t *\n\t\t\t * @param <R> the resulting type 结果类型\n\t\t\t * @param factory the factory used to create the instance 创建实例的工厂\n\t\t\t * @return the instance 新实例\n\t\t\t * @throws NoSuchElementException if the value has been filtered 值已被过滤时\n\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Complete the mapping by calling the specified method when the value has not\n\t\t\t * been filtered.\n\t\t\t * @param runnable the method to call if the value has not been filtered\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 值未被过滤时调用指定方法以完成映射。\n\t\t\t *\n\t\t\t * @param runnable the method to call if the value has not been filtered 未过滤时要调用的方法\n\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Adapter that support nullable values.\n\t\t\t *\n\t\t\t * @param <T> the source type\n\t\t\t * @param <R> the result type\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 支持 nullable 值的适配器。\n\t\t\t *\n\t\t\t * @param <T> the source type 源类型\n\t\t\t * @param <R> the result type 结果类型\n\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Factory that supports nullable values.\n\t\t\t *\n\t\t\t * @param <T> the source type\n\t\t\t * @param <R> the result type\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 支持 nullable 值的工厂。\n\t\t\t *\n\t\t\t * @param <T> the source type 源类型\n\t\t\t * @param <R> the result type 结果类型\n\t\t\t */",
    ),
    (
        "\t\t\t\t/**\n\t\t\t\t * Create a new instance for the given nullable value.\n\t\t\t\t * @param value the value used to create the instance (may be\n\t\t\t\t * {@code null})\n\t\t\t\t * @return the resulting instance\n\t\t\t\t */",
        "\t\t\t\t/**\n\t\t\t\t * 为给定 nullable 值创建新实例。\n\t\t\t\t *\n\t\t\t\t * @param value the value used to create the instance (may be\n\t\t\t\t * {@code null}) 用于创建实例的值（可为 {@code null}）\n\t\t\t\t * @return the resulting instance 结果实例\n\t\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Mapper that supports nullable values.\n\t\t\t *\n\t\t\t * @param <T> the source type\n\t\t\t * @param <R> the result type\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 支持 nullable 值的映射器。\n\t\t\t *\n\t\t\t * @param <T> the source type 源类型\n\t\t\t * @param <R> the result type 结果类型\n\t\t\t */",
    ),
    (
        "\t\t\t\t/**\n\t\t\t\t * Map an existing instance for the given nullable value.\n\t\t\t\t * @param instance the existing instance\n\t\t\t\t * @param value the value to map (may be {@code null})\n\t\t\t\t * @return the resulting mapped instance\n\t\t\t\t */",
        "\t\t\t\t/**\n\t\t\t\t * 为给定 nullable 值映射现有实例。\n\t\t\t\t *\n\t\t\t\t * @param instance the existing instance 现有实例\n\t\t\t\t * @param value the value to map (may be {@code null}) 待映射的值（可为 {@code null}）\n\t\t\t\t * @return the resulting mapped instance 映射后的实例\n\t\t\t\t */",
    ),
]
