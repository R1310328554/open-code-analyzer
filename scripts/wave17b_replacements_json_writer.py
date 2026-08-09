"""Chinese JavaDoc replacements for Spring Boot 4.1.0 wave17b JsonWriter."""

JSON_WRITER_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "/**\n * Interface that can be used to write JSON output. Typically used to generate JSON when a\n * dependency on a fully marshalling library (such as Jackson or Gson) cannot be assumed.\n * <p>\n * For standard Java types, the {@link #standard()} factory method may be used to obtain\n * an instance of this interface. It supports {@link String}, {@link Number} and\n * {@link Boolean} as well as {@link Collection}, {@code Array}, {@link Map} and\n * {@link WritableJson} types. Typical usage would be:\n *\n * <pre class=\"code\">\n * JsonWriter&lt;Map&lt;String,Object&gt;&gt; writer = JsonWriter.standard();\n * writer.write(Map.of(\"Hello\", \"World!\"), out);\n * </pre>\n * <p>\n * More complex mappings can be created using the {@link #of(Consumer)} method with a\n * callback to configure the {@link Members JSON members} that should be written. Typical\n * usage would be:\n *\n * <pre class=\"code\">\n * JsonWriter&lt;Person&gt; writer = JsonWriter.of((members) -&gt; {\n *     members.add(\"first\", Person::firstName);\n *     members.add(\"last\", Person::lastName);\n *     members.add(\"dob\", Person::dateOfBirth)\n *         .whenNotNull()\n *         .as(DateTimeFormatter.ISO_DATE::format);\n * });\n * writer.write(person, out);\n * </pre>\n * <p>\n * The {@link #writeToString(Object)} method can be used if you want to write the JSON\n * directly to a {@link String}. To write to other types of output, the\n * {@link #write(Object)} method may be used to obtain a {@link WritableJson} instance.\n *\n * @param <T> the type being written\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 3.4.0\n */",
        "/**\n * 用于写出 JSON 输出的接口。在无法假定完整序列化库（如 Jackson 或 Gson）依赖时，\n * 通常用于生成 JSON。\n * <p>\n * 对于标准 Java 类型，可使用 {@link #standard()} 工厂方法获取本接口实例。\n * 支持 {@link String}、{@link Number}、{@link Boolean}，以及 {@link Collection}、\n * {@code Array}、{@link Map} 和 {@link WritableJson}。典型用法：\n *\n * <pre class=\"code\">\n * JsonWriter&lt;Map&lt;String,Object&gt;&gt; writer = JsonWriter.standard();\n * writer.write(Map.of(\"Hello\", \"World!\"), out);\n * </pre>\n * <p>\n * 更复杂的映射可通过 {@link #of(Consumer)} 配合回调配置要写入的 {@link Members JSON 成员}。\n * 典型用法：\n *\n * <pre class=\"code\">\n * JsonWriter&lt;Person&gt; writer = JsonWriter.of((members) -&gt; {\n *     members.add(\"first\", Person::firstName);\n *     members.add(\"last\", Person::lastName);\n *     members.add(\"dob\", Person::dateOfBirth)\n *         .whenNotNull()\n *         .as(DateTimeFormatter.ISO_DATE::format);\n * });\n * writer.write(person, out);\n * </pre>\n * <p>\n * 若需直接写入 {@link String}，可使用 {@link #writeToString(Object)}。\n * 写入其他输出类型时，可通过 {@link #write(Object)} 获取 {@link WritableJson} 实例。\n *\n * @param <T> the type being written 待写入的类型\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 3.4.0\n */",
    ),
    (
        "\t/**\n\t * Write the given instance to the provided {@link Appendable}.\n\t * @param instance the instance to write (may be {@code null}\n\t * @param out the output that should receive the JSON\n\t * @throws IOException on IO error\n\t */",
        "\t/**\n\t * 将给定实例写入提供的 {@link Appendable}。\n\t *\n\t * @param instance the instance to write (may be {@code null} 待写入实例（可为 {@code null}）\n\t * @param out the output that should receive the JSON 接收 JSON 的输出\n\t * @throws IOException on IO error IO 异常时\n\t */",
    ),
    (
        "\t/**\n\t * Write the given instance to a JSON string.\n\t * @param instance the instance to write (may be {@code null})\n\t * @return the JSON string\n\t */",
        "\t/**\n\t * 将给定实例写入 JSON 字符串。\n\t *\n\t * @param instance the instance to write (may be {@code null}) 待写入实例（可为 {@code null}）\n\t * @return the JSON string JSON 字符串\n\t */",
    ),
    (
        "\t/**\n\t * Provide a {@link WritableJson} implementation that may be used to write the given\n\t * instance to various outputs.\n\t * @param instance the instance to write (may be {@code null})\n\t * @return a {@link WritableJson} instance that may be used to write the JSON\n\t */",
        "\t/**\n\t * 提供 {@link WritableJson} 实现，用于将给定实例写入多种输出。\n\t *\n\t * @param instance the instance to write (may be {@code null}) 待写入实例（可为 {@code null}）\n\t * @return a {@link WritableJson} instance that may be used to write the JSON 可用于写出 JSON 的 {@link WritableJson} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link JsonWriter} instance that appends a new line after the JSON has\n\t * been written.\n\t * @return a new {@link JsonWriter} instance that appends a new line after the JSON\n\t */",
        "\t/**\n\t * 返回在 JSON 写入结束后追加换行符的新 {@link JsonWriter} 实例。\n\t *\n\t * @return a new {@link JsonWriter} instance that appends a new line after the JSON 写入后追加换行的新 {@link JsonWriter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link JsonWriter} instance that appends the given suffix after the\n\t * JSON has been written.\n\t * @param suffix the suffix to write, if any\n\t * @return a new {@link JsonWriter} instance that appends a suffix after the JSON\n\t */",
        "\t/**\n\t * 返回在 JSON 写入结束后追加给定后缀的新 {@link JsonWriter} 实例。\n\t *\n\t * @param suffix the suffix to write, if any 要追加的后缀（若有）\n\t * @return a new {@link JsonWriter} instance that appends a suffix after the JSON 写入后追加后缀的新 {@link JsonWriter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Factory method to return a {@link JsonWriter} for standard Java types. See\n\t * {@link JsonValueWriter class-level javadoc} for details.\n\t * @param <T> the type to write\n\t * @return a {@link JsonWriter} instance\n\t */",
        "\t/**\n\t * 返回适用于标准 Java 类型的 {@link JsonWriter} 工厂方法。\n\t * 详见 {@link JsonValueWriter class-level javadoc}。\n\t *\n\t * @param <T> the type to write 待写入类型\n\t * @return a {@link JsonWriter} instance {@link JsonWriter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Factory method to return a {@link JsonWriter} with specific {@link Members member\n\t * mapping}. See {@link JsonValueWriter class-level javadoc} and {@link Members} for\n\t * details.\n\t * @param <T> the type to write\n\t * @param members a consumer, which should configure the members\n\t * @return a {@link JsonWriter} instance\n\t * @see Members\n\t */",
        "\t/**\n\t * 返回带指定 {@link Members 成员映射} 的 {@link JsonWriter} 工厂方法。\n\t * 详见 {@link JsonValueWriter class-level javadoc} 与 {@link Members}。\n\t *\n\t * @param <T> the type to write 待写入类型\n\t * @param members a consumer, which should configure the members 用于配置成员的消费者\n\t * @return a {@link JsonWriter} instance {@link JsonWriter} 实例\n\t * @see Members\n\t */",
    ),
    (
        "\t\t// Don't inline 'new Members' (must be outside of lambda)",
        "\t\t// 勿内联 'new Members'（须在 lambda 外部创建）",
    ),
    (
        "\t/**\n\t * Callback used to configure JSON members. Individual members can be declared using\n\t * the various {@code add(...)} methods. Typically, members are declared with a\n\t * {@code \"name\"} and a {@link Function} that will extract the value from the\n\t * instance. Members can also be declared using a static value or a {@link Supplier}.\n\t * The {@link #add(String)} and {@link #add()} methods may be used to access the\n\t * actual instance being written.\n\t * <p>\n\t * Members can be added without a {@code name} when a {@code Member.using(...)} method\n\t * is used to complete the definition.\n\t * <p>\n\t * Members can filtered using {@code Member.when} methods and adapted to different\n\t * types using {@link Member#as(Extractor) Member.as(...)}.\n\t *\n\t * @param <T> the type that will be written\n\t */",
        "\t/**\n\t * 用于配置 JSON 成员的回调。可通过各类 {@code add(...)} 方法声明成员。\n\t * 通常以 {@code \"name\"} 与从实例提取值的 {@link Function} 声明成员；\n\t * 也可使用静态值或 {@link Supplier}。\n\t * {@link #add(String)} 与 {@link #add()} 可访问正在写入的实际实例。\n\t * <p>\n\t * 使用 {@code Member.using(...)} 完成定义时，可无 {@code name} 添加成员。\n\t * <p>\n\t * 可通过 {@code Member.when} 方法过滤成员，并通过 {@link Member#as(Extractor) Member.as(...)} 适配不同类型。\n\t *\n\t * @param <T> the type that will be written 待写入类型\n\t */",
    ),
    (
        "\t\t/**\n\t\t * Add a new member with access to the instance being written.\n\t\t * @param name the member name\n\t\t * @return the added {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 添加可访问正在写入实例的新成员。\n\t\t *\n\t\t * @param name the member name 成员名称\n\t\t * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add a new member with a static value.\n\t\t * @param <V> the value type\n\t\t * @param name the member name\n\t\t * @param value the member value\n\t\t * @return the added {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 添加带静态值的新成员。\n\t\t *\n\t\t * @param <V> the value type 值类型\n\t\t * @param name the member name 成员名称\n\t\t * @param value the member value 成员值\n\t\t * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add a new member with a supplied value.\n\t\t * @param <V> the value type\n\t\t * @param name the member name\n\t\t * @param supplier a supplier of the value\n\t\t * @return the added {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 添加由 Supplier 提供值的新成员。\n\t\t *\n\t\t * @param <V> the value type 值类型\n\t\t * @param name the member name 成员名称\n\t\t * @param supplier a supplier of the value 值供应器\n\t\t * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add a new member with an extracted value.\n\t\t * @param <V> the value type\n\t\t * @param name the member name\n\t\t * @param extractor {@link Extractor} to extract the value\n\t\t * @return the added {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 添加通过提取器获取值的新成员。\n\t\t *\n\t\t * @param <V> the value type 值类型\n\t\t * @param name the member name 成员名称\n\t\t * @param extractor {@link Extractor} to extract the value 用于提取值的 {@link Extractor}\n\t\t * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add a new member with access to the instance being written. The member is added\n\t\t * without a name, so one of the {@code Member.using(...)} methods must be used to\n\t\t * complete the configuration.\n\t\t * @return the added {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 添加可访问正在写入实例的新成员。成员无名称，\n\t\t * 须通过 {@code Member.using(...)} 方法之一完成配置。\n\t\t *\n\t\t * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add all entries from the given {@link Map} to the JSON.\n\t\t * @param <M> the map type\n\t\t * @param <K> the key type\n\t\t * @param <V> the value type\n\t\t * @param extractor {@link Extractor} to extract the map\n\t\t * @return the added {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 将给定 {@link Map} 的所有条目添加到 JSON。\n\t\t *\n\t\t * @param <M> the map type Map 类型\n\t\t * @param <K> the key type 键类型\n\t\t * @param <V> the value type 值类型\n\t\t * @param extractor {@link Extractor} to extract the map 用于提取 Map 的 {@link Extractor}\n\t\t * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add members from a static value. One of the {@code Member.using(...)} methods\n\t\t * must be used to complete the configuration.\n\t\t * @param <V> the value type\n\t\t * @param value the member value\n\t\t * @return the added {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 从静态值添加成员。须通过 {@code Member.using(...)} 方法之一完成配置。\n\t\t *\n\t\t * @param <V> the value type 值类型\n\t\t * @param value the member value 成员值\n\t\t * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add members from a supplied value. One of the {@code Member.using(...)} methods\n\t\t * must be used to complete the configuration.\n\t\t * @param <V> the value type\n\t\t * @param supplier a supplier of the value\n\t\t * @return the added {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 从 Supplier 提供的值添加成员。须通过 {@code Member.using(...)} 方法之一完成配置。\n\t\t *\n\t\t * @param <V> the value type 值类型\n\t\t * @param supplier a supplier of the value 值供应器\n\t\t * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add members from an extracted value. One of the {@code Member.using(...)}\n\t\t * methods must be used to complete the configuration.\n\t\t * @param <V> the value type\n\t\t * @param extractor {@link Extractor} to extract the value\n\t\t * @return the added {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 从提取的值添加成员。须通过 {@code Member.using(...)} 方法之一完成配置。\n\t\t *\n\t\t * @param <V> the value type 值类型\n\t\t * @param extractor {@link Extractor} to extract the value 用于提取值的 {@link Extractor}\n\t\t * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add a filter that will be used to restrict the members written to the JSON.\n\t\t * @param predicate the predicate used to filter members\n\t\t */",
        "\t\t/**\n\t\t * 添加用于限制写入 JSON 的成员的过滤器。\n\t\t *\n\t\t * @param predicate the predicate used to filter members 用于过滤成员的谓词\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add a {@link NameProcessor} to be applied when the JSON is written.\n\t\t * @param nameProcessor the name processor to add\n\t\t */",
        "\t\t/**\n\t\t * 添加写入 JSON 时应用的 {@link NameProcessor}。\n\t\t *\n\t\t * @param nameProcessor the name processor to add 要添加的名称处理器\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add a {@link ValueProcessor} to be applied when the JSON is written.\n\t\t * @param valueProcessor the value processor to add\n\t\t */",
        "\t\t/**\n\t\t * 添加写入 JSON 时应用的 {@link ValueProcessor}。\n\t\t *\n\t\t * @param valueProcessor the value processor to add 要添加的值处理器\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Writes the given instance using the configured {@link Member members}.\n\t\t * @param instance the instance to write\n\t\t * @param valueWriter the JSON value writer to use\n\t\t */",
        "\t\t/**\n\t\t * 使用已配置的 {@link Member 成员} 写入给定实例。\n\t\t *\n\t\t * @param instance the instance to write 待写入实例\n\t\t * @param valueWriter the JSON value writer to use 使用的 JSON 值写入器\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return if any of the members contributes a name/value pair to the JSON.\n\t\t * @return if a name/value pair is contributed\n\t\t */",
        "\t\t/**\n\t\t * 返回是否有任一成员向 JSON 贡献名值对。\n\t\t *\n\t\t * @return if a name/value pair is contributed 是否贡献名值对\n\t\t */",
    ),
    (
        "\t/**\n\t * A member that contributes JSON. Typically, a member will contribute a single\n\t * name/value pair based on an extracted value. They may also contribute more complex\n\t * JSON structures when configured with one of the {@code using(...)} methods.\n\t * <p>\n\t * The {@code when(...)} methods may be used to filter a member (omit it entirely from\n\t * the JSON). The {@link #as(Extractor)} method can be used to adapt to a different\n\t * type.\n\t *\n\t * @param <T> the member type\n\t */",
        "\t/**\n\t * 向 JSON 贡献内容的成员。通常基于提取值贡献单个名值对；\n\t * 配置 {@code using(...)} 方法后也可贡献更复杂的 JSON 结构。\n\t * <p>\n\t * {@code when(...)} 方法可过滤成员（从 JSON 中完全省略）。\n\t * {@link #as(Extractor)} 可将值适配为不同类型。\n\t *\n\t * @param <T> the member type 成员类型\n\t */",
    ),
    (
        "\t\t/**\n\t\t * Only include this member when its value is not {@code null}.\n\t\t * @return a {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 仅当值不为 {@code null} 时包含此成员。\n\t\t *\n\t\t * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Only include this member when an extracted value is not {@code null}.\n\t\t * @param extractor a function used to extract the value to test\n\t\t * @return a {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 仅当提取的值不为 {@code null} 时包含此成员。\n\t\t *\n\t\t * @param extractor a function used to extract the value to test 用于提取待测值的函数\n\t\t * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Only include this member when it is not {@code null} and has a\n\t\t * {@link Object#toString() toString()} that is not zero length.\n\t\t * @return a {@link Member} which may be configured further\n\t\t * @see StringUtils#hasLength(CharSequence)\n\t\t */",
        "\t\t/**\n\t\t * 仅当成员不为 {@code null} 且 {@link Object#toString() toString()} 非空时包含。\n\t\t *\n\t\t * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t * @see StringUtils#hasLength(CharSequence)\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Only include this member when it is not empty (See\n\t\t * {@link ObjectUtils#isEmpty(Object)} for details).\n\t\t * @return a {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 仅当成员非空时包含（详见 {@link ObjectUtils#isEmpty(Object)}）。\n\t\t *\n\t\t * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Only include this member when the given predicate does not match.\n\t\t * @param predicate the predicate to test\n\t\t * @return a {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 仅当给定谓词不匹配时包含此成员。\n\t\t *\n\t\t * @param predicate the predicate to test 待测谓词\n\t\t * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Only include this member when the given predicate matches.\n\t\t * @param predicate the predicate to test\n\t\t * @return a {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 仅当给定谓词匹配时包含此成员。\n\t\t *\n\t\t * @param predicate the predicate to test 待测谓词\n\t\t * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Adapt the value by applying the given {@link Function}.\n\t\t * @param <R> the result type\n\t\t * @param extractor a {@link Extractor} to adapt the value\n\t\t * @return a {@link Member} which may be configured further\n\t\t */",
        "\t\t/**\n\t\t * 通过给定 {@link Function} 适配值。\n\t\t *\n\t\t * @param <R> the result type 结果类型\n\t\t * @param extractor a {@link Extractor} to adapt the value 用于适配值的 {@link Extractor}\n\t\t * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add JSON name/value pairs by extracting values from a series of elements.\n\t\t * Typically used with a {@link Iterable#forEach(Consumer)} call, for example:\n\t\t *\n\t\t * <pre class=\"code\">\n\t\t * members.add(Event::getTags).usingExtractedPairs(Iterable::forEach, pairExtractor);\n\t\t * </pre>\n\t\t * <p>\n\t\t * When used with a named member, the pairs will be added as a new JSON value\n\t\t * object:\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"name\": {\n\t\t *     \"p1\": 1,\n\t\t *     \"p2\": 2\n\t\t *   }\n\t\t * }\n\t\t * </pre>\n\t\t *\n\t\t * When used with an unnamed member the pairs will be added to the existing JSON\n\t\t * object:\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"p1\": 1,\n\t\t *   \"p2\": 2\n\t\t * }\n\t\t * </pre>\n\t\t * @param <E> the element type\n\t\t * @param elements callback used to provide the elements\n\t\t * @param extractor a {@link PairExtractor} used to extract the name/value pair\n\t\t * @return a {@link Member} which may be configured further\n\t\t * @see #usingExtractedPairs(BiConsumer, Function, Function)\n\t\t * @see #usingPairs(BiConsumer)\n\t\t */",
        "\t\t/**\n\t\t * 从一系列元素提取值并添加 JSON 名值对。\n\t\t * 通常配合 {@link Iterable#forEach(Consumer)} 使用，例如：\n\t\t *\n\t\t * <pre class=\"code\">\n\t\t * members.add(Event::getTags).usingExtractedPairs(Iterable::forEach, pairExtractor);\n\t\t * </pre>\n\t\t * <p>\n\t\t * 用于具名成员时，名值对作为新的 JSON 对象写入：\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"name\": {\n\t\t *     \"p1\": 1,\n\t\t *     \"p2\": 2\n\t\t *   }\n\t\t * }\n\t\t * </pre>\n\t\t *\n\t\t * 用于无名成员时，名值对合并到现有 JSON 对象：\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"p1\": 1,\n\t\t *   \"p2\": 2\n\t\t * }\n\t\t * </pre>\n\t\t * @param <E> the element type 元素类型\n\t\t * @param elements callback used to provide the elements 提供元素的回调\n\t\t * @param extractor a {@link PairExtractor} used to extract the name/value pair 提取名值对的 {@link PairExtractor}\n\t\t * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t * @see #usingExtractedPairs(BiConsumer, Function, Function)\n\t\t * @see #usingPairs(BiConsumer)\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add JSON name/value pairs by extracting values from a series of elements.\n\t\t * Typically used with a {@link Iterable#forEach(Consumer)} call, for example:\n\t\t *\n\t\t * <pre class=\"code\">\n\t\t * members.add(Event::getTags).usingExtractedPairs(Iterable::forEach, Tag::getName, Tag::getValue);\n\t\t * </pre>\n\t\t * <p>\n\t\t * When used with a named member, the pairs will be added as a new JSON value\n\t\t * object:\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"name\": {\n\t\t *     \"p1\": 1,\n\t\t *     \"p2\": 2\n\t\t *   }\n\t\t * }\n\t\t * </pre>\n\t\t *\n\t\t * When used with an unnamed member the pairs will be added to the existing JSON\n\t\t * object:\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"p1\": 1,\n\t\t *   \"p2\": 2\n\t\t * }\n\t\t * </pre>\n\t\t * @param <E> the element type\n\t\t * @param <N> the name type\n\t\t * @param <V> the value type\n\t\t * @param elements callback used to provide the elements\n\t\t * @param nameExtractor {@link Function} used to extract the name\n\t\t * @param valueExtractor {@link Function} used to extract the value\n\t\t * @return a {@link Member} which may be configured further\n\t\t * @see #usingExtractedPairs(BiConsumer, PairExtractor)\n\t\t * @see #usingPairs(BiConsumer)\n\t\t */",
        "\t\t/**\n\t\t * 从一系列元素提取值并添加 JSON 名值对。\n\t\t * 通常配合 {@link Iterable#forEach(Consumer)} 使用，例如：\n\t\t *\n\t\t * <pre class=\"code\">\n\t\t * members.add(Event::getTags).usingExtractedPairs(Iterable::forEach, Tag::getName, Tag::getValue);\n\t\t * </pre>\n\t\t * <p>\n\t\t * 用于具名成员时，名值对作为新的 JSON 对象写入：\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"name\": {\n\t\t *     \"p1\": 1,\n\t\t *     \"p2\": 2\n\t\t *   }\n\t\t * }\n\t\t * </pre>\n\t\t *\n\t\t * 用于无名成员时，名值对合并到现有 JSON 对象：\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"p1\": 1,\n\t\t *   \"p2\": 2\n\t\t * }\n\t\t * </pre>\n\t\t * @param <E> the element type 元素类型\n\t\t * @param <N> the name type 名称类型\n\t\t * @param <V> the value type 值类型\n\t\t * @param elements callback used to provide the elements 提供元素的回调\n\t\t * @param nameExtractor {@link Function} used to extract the name 提取名称的 {@link Function}\n\t\t * @param valueExtractor {@link Function} used to extract the value 提取值的 {@link Function}\n\t\t * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t * @see #usingExtractedPairs(BiConsumer, PairExtractor)\n\t\t * @see #usingPairs(BiConsumer)\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add JSON name/value pairs. Typically used with a\n\t\t * {@link Map#forEach(BiConsumer)} call, for example:\n\t\t *\n\t\t * <pre class=\"code\">\n\t\t * members.add(Event::getLabels).usingPairs(Map::forEach);\n\t\t * </pre>\n\t\t * <p>\n\t\t * When used with a named member, the pairs will be added as a new JSON value\n\t\t * object:\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"name\": {\n\t\t *     \"p1\": 1,\n\t\t *     \"p2\": 2\n\t\t *   }\n\t\t * }\n\t\t * </pre>\n\t\t *\n\t\t * When used with an unnamed member the pairs will be added to the existing JSON\n\t\t * object:\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"p1\": 1,\n\t\t *   \"p2\": 2\n\t\t * }\n\t\t * </pre>\n\t\t * @param <N> the name type\n\t\t * @param <V> the value type\n\t\t * @param pairs callback used to provide the pairs\n\t\t * @return a {@link Member} which may be configured further\n\t\t * @see #usingExtractedPairs(BiConsumer, PairExtractor)\n\t\t * @see #usingPairs(BiConsumer)\n\t\t */",
        "\t\t/**\n\t\t * 添加 JSON 名值对。通常配合 {@link Map#forEach(BiConsumer)} 使用，例如：\n\t\t *\n\t\t * <pre class=\"code\">\n\t\t * members.add(Event::getLabels).usingPairs(Map::forEach);\n\t\t * </pre>\n\t\t * <p>\n\t\t * 用于具名成员时，名值对作为新的 JSON 对象写入：\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"name\": {\n\t\t *     \"p1\": 1,\n\t\t *     \"p2\": 2\n\t\t *   }\n\t\t * }\n\t\t * </pre>\n\t\t *\n\t\t * 用于无名成员时，名值对合并到现有 JSON 对象：\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"p1\": 1,\n\t\t *   \"p2\": 2\n\t\t * }\n\t\t * </pre>\n\t\t * @param <N> the name type 名称类型\n\t\t * @param <V> the value type 值类型\n\t\t * @param pairs callback used to provide the pairs 提供名值对的回调\n\t\t * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t * @see #usingExtractedPairs(BiConsumer, PairExtractor)\n\t\t * @see #usingPairs(BiConsumer)\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Add JSON based on further {@link Members} configuration. For example:\n\t\t *\n\t\t * <pre class=\"code\">\n\t\t * members.add(User::getName).usingMembers((personMembers) -> {\n\t\t *     personMembers.add(\"first\", Name::first);\n\t\t *     personMembers.add(\"last\", Name::last);\n\t\t * });\n\t\t * </pre>\n\t\t *\n\t\t * <p>\n\t\t * When used with a named member, the result will be added as a new JSON value\n\t\t * object:\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"name\": {\n\t\t *     \"first\": \"Jane\",\n\t\t *     \"last\": \"Doe\"\n\t\t *   }\n\t\t * }\n\t\t * </pre>\n\t\t *\n\t\t * When used with an unnamed member the result will be added to the existing JSON\n\t\t * object:\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"first\": \"John\",\n\t\t *   \"last\": \"Doe\"\n\t\t * }\n\t\t * </pre>\n\t\t * @param members callback to configure the members\n\t\t * @return a {@link Member} which may be configured further\n\t\t * @see #usingExtractedPairs(BiConsumer, PairExtractor)\n\t\t * @see #usingPairs(BiConsumer)\n\t\t */",
        "\t\t/**\n\t\t * 基于进一步 {@link Members} 配置添加 JSON。例如：\n\t\t *\n\t\t * <pre class=\"code\">\n\t\t * members.add(User::getName).usingMembers((personMembers) -> {\n\t\t *     personMembers.add(\"first\", Name::first);\n\t\t *     personMembers.add(\"last\", Name::last);\n\t\t * });\n\t\t * </pre>\n\t\t *\n\t\t * <p>\n\t\t * 用于具名成员时，结果作为新的 JSON 对象写入：\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"name\": {\n\t\t *     \"first\": \"Jane\",\n\t\t *     \"last\": \"Doe\"\n\t\t *   }\n\t\t * }\n\t\t * </pre>\n\t\t *\n\t\t * 用于无名成员时，结果合并到现有 JSON 对象：\n\t\t *\n\t\t * <pre>\n\t\t * {\n\t\t *   \"first\": \"John\",\n\t\t *   \"last\": \"Doe\"\n\t\t * }\n\t\t * </pre>\n\t\t * @param members callback to configure the members 配置成员的回调\n\t\t * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}\n\t\t * @see #usingExtractedPairs(BiConsumer, PairExtractor)\n\t\t * @see #usingPairs(BiConsumer)\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Writes the given instance using details configure by this member.\n\t\t * @param instance the instance to write\n\t\t * @param valueWriter the JSON value writer to use\n\t\t */",
        "\t\t/**\n\t\t * 使用本成员配置的详情写入给定实例。\n\t\t *\n\t\t * @param instance the instance to write 待写入实例\n\t\t * @param valueWriter the JSON value writer to use 使用的 JSON 值写入器\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Whether this contributes one or more name/value pairs to the JSON.\n\t\t * @return whether a name/value pair is contributed\n\t\t */",
        "\t\t/**\n\t\t * 是否向 JSON 贡献一个或多个名值对。\n\t\t *\n\t\t * @return whether a name/value pair is contributed 是否贡献名值对\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Internal class used to manage member value extraction and filtering.\n\t\t *\n\t\t * @param <T> the member type\n\t\t */",
        "\t\t/**\n\t\t * 管理成员值提取与过滤的内部类。\n\t\t *\n\t\t * @param <T> the member type 成员类型\n\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Represents a skipped value.\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 表示应跳过的值。\n\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Extract the value from the given instance.\n\t\t\t * @param instance the source instance\n\t\t\t * @return the extracted value or {@link #SKIP}\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 从给定实例提取值。\n\t\t\t *\n\t\t\t * @param instance the source instance 源实例\n\t\t\t * @return the extracted value or {@link #SKIP} 提取的值或 {@link #SKIP}\n\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Only extract when the given predicate matches.\n\t\t\t * @param predicate the predicate to test\n\t\t\t * @return a new {@link ValueExtractor}\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 仅当给定谓词匹配时提取。\n\t\t\t *\n\t\t\t * @param predicate the predicate to test 待测谓词\n\t\t\t * @return a new {@link ValueExtractor} 新的 {@link ValueExtractor}\n\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Adapt the extracted value.\n\t\t\t * @param <R> the result type\n\t\t\t * @param extractor the extractor to use\n\t\t\t * @return a new {@link ValueExtractor}\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 适配提取的值。\n\t\t\t *\n\t\t\t * @param <R> the result type 结果类型\n\t\t\t * @param extractor the extractor to use 使用的提取器\n\t\t\t * @return a new {@link ValueExtractor} 新的 {@link ValueExtractor}\n\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Create a new {@link ValueExtractor} based on the given {@link Function}.\n\t\t\t * @param <S> the source type\n\t\t\t * @param <T> the extracted type\n\t\t\t * @param extractor the extractor to use\n\t\t\t * @return a new {@link ValueExtractor} instance\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 基于给定 {@link Function} 创建新的 {@link ValueExtractor}。\n\t\t\t *\n\t\t\t * @param <S> the source type 源类型\n\t\t\t * @param <T> the extracted type 提取类型\n\t\t\t * @param extractor the extractor to use 使用的提取器\n\t\t\t * @return a new {@link ValueExtractor} instance 新的 {@link ValueExtractor} 实例\n\t\t\t */",
    ),
    (
        "\t\t\t/**\n\t\t\t * Return if the extracted value should be skipped.\n\t\t\t * @param <T> the value type\n\t\t\t * @param extracted the value to test\n\t\t\t * @return if the value is to be skipped\n\t\t\t */",
        "\t\t\t/**\n\t\t\t * 返回提取的值是否应跳过。\n\t\t\t *\n\t\t\t * @param <T> the value type 值类型\n\t\t\t * @param extracted the value to test 待测值\n\t\t\t * @return if the value is to be skipped 是否跳过\n\t\t\t */",
    ),
    (
        "\t/**\n\t * A path used to identify a specific JSON member. Paths can be represented as strings\n\t * in form {@code \"my.json[1].item\"} where elements are separated by {@code '.' } or\n\t * {@code [<index>]}. Reserved characters are escaped using {@code '\\'}.\n\t *\n\t * @param parent the parent of this path\n\t * @param name the name of the member or {@code null} if the member is indexed. Path\n\t * names are provided as they were defined when the member was added and do not\n\t * include any {@link NameProcessor name processing}.\n\t * @param index the index of the member or {@link MemberPath#UNINDEXED}\n\t */",
        "\t/**\n\t * 用于标识特定 JSON 成员的路径。路径可表示为字符串，\n\t * 形式如 {@code \"my.json[1].item\"}，元素以 {@code '.'} 或 {@code [<index>]} 分隔。\n\t * 保留字符使用 {@code '\\'} 转义。\n\t *\n\t * @param parent the parent of this path 父路径\n\t * @param name the name of the member or {@code null} if the member is indexed. Path\n\t * names are provided as they were defined when the member was added and do not\n\t * include any {@link NameProcessor name processing}. 成员名称；索引成员时为 {@code null}（为添加时的原始名称，不含 {@link NameProcessor 名称处理}）\n\t * @param index the index of the member or {@link MemberPath#UNINDEXED} 成员索引或 {@link MemberPath#UNINDEXED}\n\t */",
    ),
    (
        "\t\t/**\n\t\t * Indicates that the member has no index.\n\t\t */",
        "\t\t/**\n\t\t * 表示成员无索引。\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * The root of all member paths.\n\t\t */",
        "\t\t/**\n\t\t * 所有成员路径的根。\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Create a new child from this path with the specified index.\n\t\t * @param index the index of the child\n\t\t * @return a new {@link MemberPath} instance\n\t\t */",
        "\t\t/**\n\t\t * 创建带指定索引的子路径。\n\t\t *\n\t\t * @param index the index of the child 子路径索引\n\t\t * @return a new {@link MemberPath} instance 新的 {@link MemberPath} 实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Create a new child from this path with the specified name.\n\t\t * @param name the name of the child\n\t\t * @return a new {@link MemberPath} instance\n\t\t */",
        "\t\t/**\n\t\t * 创建带指定名称的子路径。\n\t\t *\n\t\t * @param name the name of the child 子路径名称\n\t\t * @return a new {@link MemberPath} instance 新的 {@link MemberPath} 实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a string representation of the path without any escaping.\n\t\t * @return the unescaped string representation\n\t\t */",
        "\t\t/**\n\t\t * 返回未转义的路径字符串表示。\n\t\t *\n\t\t * @return the unescaped string representation 未转义的字符串表示\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Create a new {@link MemberPath} instance from the given string.\n\t\t * @param value the path value\n\t\t * @return a new {@link MemberPath} instance\n\t\t */",
        "\t\t/**\n\t\t * 从给定字符串创建新的 {@link MemberPath} 实例。\n\t\t *\n\t\t * @param value the path value 路径字符串\n\t\t * @return a new {@link MemberPath} instance 新的 {@link MemberPath} 实例\n\t\t */",
    ),
    (
        "\t/**\n\t * Interface that can be used to extract name/value pairs from an element.\n\t *\n\t * @param <E> the element type\n\t */",
        "\t/**\n\t * 从元素提取名值对的接口。\n\t *\n\t * @param <E> the element type 元素类型\n\t */",
    ),
    (
        "\t\t/**\n\t\t * Extract the name.\n\t\t * @param <N> the name type\n\t\t * @param element the source element\n\t\t * @return the extracted name\n\t\t */",
        "\t\t/**\n\t\t * 提取名称。\n\t\t *\n\t\t * @param <N> the name type 名称类型\n\t\t * @param element the source element 源元素\n\t\t * @return the extracted name 提取的名称\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Extract the name.\n\t\t * @param <V> the value type\n\t\t * @param element the source element\n\t\t * @return the extracted value\n\t\t */",
        "\t\t/**\n\t\t * 提取值。\n\t\t *\n\t\t * @param <V> the value type 值类型\n\t\t * @param element the source element 源元素\n\t\t * @return the extracted value 提取的值\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Factory method to create a {@link PairExtractor} using distinct name and value\n\t\t * extraction functions.\n\t\t * @param <T> the element type\n\t\t * @param nameExtractor the name extractor\n\t\t * @param valueExtractor the value extraction\n\t\t * @return a new {@link PairExtractor} instance\n\t\t */",
        "\t\t/**\n\t\t * 使用独立的名称与值提取函数创建 {@link PairExtractor} 的工厂方法。\n\t\t *\n\t\t * @param <T> the element type 元素类型\n\t\t * @param nameExtractor the name extractor 名称提取器\n\t\t * @param valueExtractor the value extraction 值提取器\n\t\t * @return a new {@link PairExtractor} instance 新的 {@link PairExtractor} 实例\n\t\t */",
    ),
    (
        "\t/**\n\t * Callback interface that can be {@link Members#applyingNameProcessor(NameProcessor)\n\t * applied} to {@link Members} to change names or filter members.\n\t */",
        "\t/**\n\t * 可 {@link Members#applyingNameProcessor(NameProcessor) 应用}于 {@link Members} 的回调接口，\n\t * 用于更改名称或过滤成员。\n\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a new name for the JSON member or {@code null} if the member should be\n\t\t * filtered entirely.\n\t\t * @param path the path of the member\n\t\t * @param existingName the existing and possibly already processed name.\n\t\t * @return the new name\n\t\t */",
        "\t\t/**\n\t\t * 返回 JSON 成员的新名称；若应完全过滤该成员则返回 {@code null}。\n\t\t *\n\t\t * @param path the path of the member 成员路径\n\t\t * @param existingName the existing and possibly already processed name. 现有名称（可能已处理）\n\t\t * @return the new name 新名称\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Factory method to create a new {@link NameProcessor} for the given operation.\n\t\t * @param operation the operation to apply\n\t\t * @return a new {@link NameProcessor} instance\n\t\t */",
        "\t\t/**\n\t\t * 为给定操作创建新 {@link NameProcessor} 的工厂方法。\n\t\t *\n\t\t * @param operation the operation to apply 要应用的操作\n\t\t * @return a new {@link NameProcessor} instance 新的 {@link NameProcessor} 实例\n\t\t */",
    ),
    (
        "\t/**\n\t * Callback interface that can be\n\t * {@link Members#applyingValueProcessor(ValueProcessor) applied} to {@link Members}\n\t * to process values before they are written. Typically used to filter values, for\n\t * example to reduce superfluous information or sanitize sensitive data.\n\t *\n\t * @param <T> the value type\n\t */",
        "\t/**\n\t * 可 {@link Members#applyingValueProcessor(ValueProcessor) 应用}于 {@link Members} 的回调接口，\n\t * 在写入前处理值。通常用于过滤值，例如减少冗余信息或脱敏敏感数据。\n\t *\n\t * @param <T> the value type 值类型\n\t */",
    ),
    (
        "\t\t/**\n\t\t * Process the value at the given path.\n\t\t * @param path the path of the member containing the value\n\t\t * @param value the value being written (may be {@code null})\n\t\t * @return the processed value\n\t\t */",
        "\t\t/**\n\t\t * 处理给定路径处的值。\n\t\t *\n\t\t * @param path the path of the member containing the value 包含该值的成员路径\n\t\t * @param value the value being written (may be {@code null}) 待写入的值（可为 {@code null}）\n\t\t * @return the processed value 处理后的值\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a new processor from this one that only applied to members with the\n\t\t * given path (ignoring escape characters).\n\t\t * @param path the patch to match\n\t\t * @return a new {@link ValueProcessor} that only applies when the path matches\n\t\t */",
        "\t\t/**\n\t\t * 返回仅应用于给定路径（忽略转义字符）成员的新处理器。\n\t\t *\n\t\t * @param path the patch to match 要匹配的路径\n\t\t * @return a new {@link ValueProcessor} that only applies when the path matches 路径匹配时才应用的新 {@link ValueProcessor}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a new processor from this one that only applied to members with the\n\t\t * given path.\n\t\t * @param path the patch to match\n\t\t * @return a new {@link ValueProcessor} that only applies when the path matches\n\t\t */",
        "\t\t/**\n\t\t * 返回仅应用于给定路径成员的新处理器。\n\t\t *\n\t\t * @param path the patch to match 要匹配的路径\n\t\t * @return a new {@link ValueProcessor} that only applies when the path matches 路径匹配时才应用的新 {@link ValueProcessor}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a new processor from this one that only applied to members that match\n\t\t * the given path predicate.\n\t\t * @param predicate the predicate that must match\n\t\t * @return a new {@link ValueProcessor} that only applies when the predicate\n\t\t * matches\n\t\t */",
        "\t\t/**\n\t\t * 返回仅应用于匹配给定路径谓词的成员的新处理器。\n\t\t *\n\t\t * @param predicate the predicate that must match 必须匹配的谓词\n\t\t * @return a new {@link ValueProcessor} that only applies when the predicate\n\t\t * matches 谓词匹配时才应用的新 {@link ValueProcessor}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a new processor from this one that only applies to member with values of\n\t\t * the given type.\n\t\t * @param type the type that must match\n\t\t * @return a new {@link ValueProcessor} that only applies when value is the given\n\t\t * type.\n\t\t */",
        "\t\t/**\n\t\t * 返回仅应用于给定类型值的新处理器。\n\t\t *\n\t\t * @param type the type that must match 必须匹配的类型\n\t\t * @return a new {@link ValueProcessor} that only applies when value is the given\n\t\t * type. 值类型匹配时才应用的新 {@link ValueProcessor}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Return a new processor from this one that only applies to member with values\n\t\t * that match the given predicate.\n\t\t * @param predicate the predicate that must match\n\t\t * @return a new {@link ValueProcessor} that only applies when the predicate\n\t\t * matches\n\t\t */",
        "\t\t/**\n\t\t * 返回仅应用于匹配给定谓词的值的新处理器。\n\t\t *\n\t\t * @param predicate the predicate that must match 必须匹配的谓词\n\t\t * @return a new {@link ValueProcessor} that only applies when the predicate\n\t\t * matches 谓词匹配时才应用的新 {@link ValueProcessor}\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Factory method to crate a new {@link ValueProcessor} that applies the given\n\t\t * action.\n\t\t * @param <T> the value type\n\t\t * @param type the value type\n\t\t * @param action the action to apply\n\t\t * @return a new {@link ValueProcessor} instance\n\t\t */",
        "\t\t/**\n\t\t * 创建应用给定操作的新 {@link ValueProcessor} 的工厂方法。\n\t\t *\n\t\t * @param <T> the value type 值类型\n\t\t * @param type the value type 值类型\n\t\t * @param action the action to apply 要应用的操作\n\t\t * @return a new {@link ValueProcessor} instance 新的 {@link ValueProcessor} 实例\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Factory method to crate a new {@link ValueProcessor} that applies the given\n\t\t * action.\n\t\t * @param <T> the value type\n\t\t * @param action the action to apply\n\t\t * @return a new {@link ValueProcessor} instance\n\t\t */",
        "\t\t/**\n\t\t * 创建应用给定操作的新 {@link ValueProcessor} 的工厂方法。\n\t\t *\n\t\t * @param <T> the value type 值类型\n\t\t * @param action the action to apply 要应用的操作\n\t\t * @return a new {@link ValueProcessor} instance 新的 {@link ValueProcessor} 实例\n\t\t */",
    ),
    (
        "\t/**\n\t * Interface that can be used to extract one value from another.\n\t *\n\t * @param <T> the source type\n\t * @param <R> the result type\n\t */",
        "\t/**\n\t * 从一个值提取另一个值的接口。\n\t *\n\t * @param <T> the source type 源类型\n\t * @param <R> the result type 结果类型\n\t */",
    ),
    (
        "\t\t/**\n\t\t * Extract from the given value.\n\t\t * @param value the source value (never {@code null})\n\t\t * @return an extracted value or {@code null}\n\t\t */",
        "\t\t/**\n\t\t * 从给定值提取。\n\t\t *\n\t\t * @param value the source value (never {@code null}) 源值（永不为 {@code null}）\n\t\t * @return an extracted value or {@code null} 提取的值或 {@code null}\n\t\t */",
    ),
]
