"""Chinese JavaDoc replacements for Spring Boot 4.1.0 wave17a ConfigurationPropertyName."""

CONFIGURATION_PROPERTY_NAME_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "/**\n * A configuration property name composed of elements separated by dots. User created\n * names may contain the characters \"{@code a-z}\" \"{@code 0-9}\" and \"{@code -}\", they must\n * be lower-case and must start with an alphanumeric character. The \"{@code -}\" is used\n * purely for formatting, i.e. \"{@code foo-bar}\" and \"{@code foobar}\" are considered\n * equivalent.\n * <p>\n * The \"{@code [}\" and \"{@code ]}\" characters may be used to indicate an associative\n * index(i.e. a {@link Map} key or a {@link Collection} index). Indexes names are not\n * restricted and are considered case-sensitive.\n * <p>\n * Here are some typical examples:\n * <ul>\n * <li>{@code spring.main.banner-mode}</li>\n * <li>{@code server.hosts[0].name}</li>\n * <li>{@code log[org.springboot].level}</li>\n * </ul>\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see #of(CharSequence)\n * @see ConfigurationPropertySource\n */",
        "/**\n * 由点号分隔元素组成的配置属性名。\n * 用户创建的名称可包含 \"{@code a-z}\"、\"{@code 0-9}\" 与 \"{@code -}\"，\n * 须为小写且以字母或数字开头。\"{@code -}\" 仅用于格式化，\n * 例如 \"{@code foo-bar}\" 与 \"{@code foobar}\" 视为等价。\n * <p>\n * 可用 \"{@code [}\" 与 \"{@code ]}\" 表示关联索引（即 {@link Map} 键或 {@link Collection} 索引）。\n * 索引名不受上述字符限制，且区分大小写。\n * <p>\n * 典型示例：\n * <ul>\n * <li>{@code spring.main.banner-mode}</li>\n * <li>{@code server.hosts[0].name}</li>\n * <li>{@code log[org.springboot].level}</li>\n * </ul>\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.0.0\n * @see #of(CharSequence)\n * @see ConfigurationPropertySource\n */",
    ),
    (
        "\t/**\n\t * An empty {@link ConfigurationPropertyName}.\n\t */",
        "\t/**\n\t * 空的 {@link ConfigurationPropertyName} 常量。\n\t */",
    ),
    (
        "\t/**\n\t * Returns {@code true} if this {@link ConfigurationPropertyName} is empty.\n\t * @return {@code true} if the name is empty\n\t */",
        "\t/**\n\t * 若此 {@link ConfigurationPropertyName} 为空则返回 {@code true}。\n\t * @return {@code true} if the name is empty 名称为空时为 true\n\t */",
    ),
    (
        "\t/**\n\t * Return if the last element in the name is indexed.\n\t * @return {@code true} if the last element is indexed\n\t */",
        "\t/**\n\t * 判断名称中最后一个元素是否为索引元素。\n\t * @return {@code true} if the last element is indexed 最后一个元素为索引时为 true\n\t */",
    ),
    (
        "\t/**\n\t * Return {@code true} if any element in the name is indexed.\n\t * @return if the element has one or more indexed elements\n\t * @since 2.2.10\n\t */",
        "\t/**\n\t * 若名称中任一元素为索引元素则返回 {@code true}。\n\t * @return if the element has one or more indexed elements 存在索引元素时为 true\n\t * @since 2.2.10\n\t */",
    ),
    (
        "\t/**\n\t * Return if the element in the name is indexed.\n\t * @param elementIndex the index of the element\n\t * @return {@code true} if the element is indexed\n\t */",
        "\t/**\n\t * 判断指定位置的元素是否为索引元素。\n\t * @param elementIndex the index of the element 元素下标\n\t * @return {@code true} if the element is indexed 该元素为索引时为 true\n\t */",
    ),
    (
        "\t/**\n\t * Return if the element in the name is indexed and numeric.\n\t * @param elementIndex the index of the element\n\t * @return {@code true} if the element is indexed and numeric\n\t */",
        "\t/**\n\t * 判断指定元素是否为数值型索引。\n\t * @param elementIndex the index of the element 元素下标\n\t * @return {@code true} if the element is indexed and numeric 为数值索引时为 true\n\t */",
    ),
    (
        "\t/**\n\t * Return the last element in the name in the given form.\n\t * @param form the form to return\n\t * @return the last element\n\t */",
        "\t/**\n\t * 以给定形式返回名称中的最后一个元素。\n\t * @param form the form to return 元素形式\n\t * @return the last element 最后一个元素\n\t */",
    ),
    (
        "\t/**\n\t * Return an element in the name in the given form.\n\t * @param elementIndex the element index\n\t * @param form the form to return\n\t * @return the last element\n\t */",
        "\t/**\n\t * 以给定形式返回名称中指定下标的元素。\n\t * @param elementIndex the element index 元素下标\n\t * @param form the form to return 元素形式\n\t * @return the last element 对应元素字符串\n\t */",
    ),
    (
        "\t/**\n\t * Return the total number of elements in the name.\n\t * @return the number of elements\n\t */",
        "\t/**\n\t * 返回名称中的元素总数。\n\t * @return the number of elements 元素个数\n\t */",
    ),
    (
        "\t/**\n\t * Create a new {@link ConfigurationPropertyName} by appending the given suffix.\n\t * @param suffix the elements to append\n\t * @return a new {@link ConfigurationPropertyName}\n\t * @throws InvalidConfigurationPropertyNameException if the result is not valid\n\t */",
        "\t/**\n\t * 在末尾追加给定后缀，创建新的 {@link ConfigurationPropertyName}。\n\t * @param suffix the elements to append 要追加的元素字符串\n\t * @return a new {@link ConfigurationPropertyName} 新名称实例\n\t * @throws InvalidConfigurationPropertyNameException if the result is not valid 结果无效时抛出\n\t */",
    ),
    (
        "\t/**\n\t * Create a new {@link ConfigurationPropertyName} by appending the given suffix.\n\t * @param suffix the elements to append\n\t * @return a new {@link ConfigurationPropertyName}\n\t * @since 2.5.0\n\t */",
        "\t/**\n\t * 在末尾追加给定 {@link ConfigurationPropertyName} 后缀，创建新名称。\n\t * @param suffix the elements to append 要追加的名称\n\t * @return a new {@link ConfigurationPropertyName} 新名称实例\n\t * @since 2.5.0\n\t */",
    ),
    (
        "\t/**\n\t * Return the parent of this {@link ConfigurationPropertyName} or\n\t * {@link ConfigurationPropertyName#EMPTY} if there is no parent.\n\t * @return the parent name\n\t */",
        "\t/**\n\t * 返回此 {@link ConfigurationPropertyName} 的父名称；\n\t * 若无父级则返回 {@link ConfigurationPropertyName#EMPTY}。\n\t * @return the parent name 父名称\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link ConfigurationPropertyName} by chopping this name to the given\n\t * {@code size}. For example, {@code chop(1)} on the name {@code foo.bar} will return\n\t * {@code foo}.\n\t * @param size the size to chop\n\t * @return the chopped name\n\t */",
        "\t/**\n\t * 将名称截断至给定 {@code size} 个元素，返回新 {@link ConfigurationPropertyName}。\n\t * 例如对 {@code foo.bar} 调用 {@code chop(1)} 返回 {@code foo}。\n\t * @param size the size to chop 保留的元素个数\n\t * @return the chopped name 截断后的名称\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link ConfigurationPropertyName} by based on this name offset by\n\t * specific element index. For example, {@code subName(1)} on the name {@code foo.bar}\n\t * will return {@code bar}.\n\t * @param offset the element offset\n\t * @return the sub name\n\t * @since 2.5.0\n\t */",
        "\t/**\n\t * 从指定元素偏移量起取子名称，返回新 {@link ConfigurationPropertyName}。\n\t * 例如对 {@code foo.bar} 调用 {@code subName(1)} 返回 {@code bar}。\n\t * @param offset the element offset 元素偏移量\n\t * @return the sub name 子名称\n\t * @since 2.5.0\n\t */",
    ),
    (
        "\t/**\n\t * Returns {@code true} if this element is an immediate parent of the specified name.\n\t * @param name the name to check\n\t * @return {@code true} if this name is an ancestor\n\t */",
        "\t/**\n\t * 若此名称为指定名称的直接父级则返回 {@code true}。\n\t * @param name the name to check 待检查的名称\n\t * @return {@code true} if this name is an ancestor 为直接父级时为 true\n\t */",
    ),
    (
        "\t/**\n\t * Returns {@code true} if this element is an ancestor (immediate or nested parent) of\n\t * the specified name.\n\t * @param name the name to check\n\t * @return {@code true} if this name is an ancestor\n\t */",
        "\t/**\n\t * 若此名称为指定名称的祖先（直接或非直接父级）则返回 {@code true}。\n\t * @param name the name to check 待检查的名称\n\t * @return {@code true} if this name is an ancestor 为祖先时为 true\n\t */",
    ),
    (
        "\t/**\n\t * Returns if the given name is valid. If this method returns {@code true} then the\n\t * name may be used with {@link #of(CharSequence)} without throwing an exception.\n\t * @param name the name to test\n\t * @return {@code true} if the name is valid\n\t */",
        "\t/**\n\t * 判断给定名称是否有效。返回 {@code true} 时，\n\t * 可安全用于 {@link #of(CharSequence)} 而不会抛出异常。\n\t * @param name the name to test 待测试的名称\n\t * @return {@code true} if the name is valid 名称有效时为 true\n\t */",
    ),
    (
        "\t/**\n\t * Return a {@link ConfigurationPropertyName} for the specified string.\n\t * @param name the source name\n\t * @return a {@link ConfigurationPropertyName} instance\n\t * @throws InvalidConfigurationPropertyNameException if the name is not valid\n\t */",
        "\t/**\n\t * 根据给定字符串创建 {@link ConfigurationPropertyName}。\n\t * @param name the source name 源名称字符串\n\t * @return a {@link ConfigurationPropertyName} instance 名称实例\n\t * @throws InvalidConfigurationPropertyNameException if the name is not valid 名称无效时抛出\n\t */",
    ),
    (
        "\t/**\n\t * Return a {@link ConfigurationPropertyName} for the specified string or {@code null}\n\t * if the name is not valid.\n\t * @param name the source name\n\t * @return a {@link ConfigurationPropertyName} instance\n\t * @since 2.3.1\n\t */",
        "\t/**\n\t * 根据给定字符串创建 {@link ConfigurationPropertyName}；无效时返回 {@code null}。\n\t * @param name the source name 源名称字符串\n\t * @return a {@link ConfigurationPropertyName} instance 名称实例或 null\n\t * @since 2.3.1\n\t */",
    ),
    (
        "\t/**\n\t * Return a {@link ConfigurationPropertyName} for the specified string.\n\t * @param name the source name\n\t * @param returnNullIfInvalid if null should be returned if the name is not valid\n\t * @return a {@link ConfigurationPropertyName} instance\n\t * @throws InvalidConfigurationPropertyNameException if the name is not valid and\n\t * {@code returnNullIfInvalid} is {@code false}\n\t */",
        "\t/**\n\t * 根据给定字符串创建 {@link ConfigurationPropertyName}。\n\t * @param name the source name 源名称字符串\n\t * @param returnNullIfInvalid if null should be returned if the name is not valid 无效时是否返回 null\n\t * @return a {@link ConfigurationPropertyName} instance 名称实例或 null\n\t * @throws InvalidConfigurationPropertyNameException if the name is not valid and\n\t * {@code returnNullIfInvalid} is {@code false} 无效且不允许 null 时抛出\n\t */",
    ),
    (
        "\t/**\n\t * Create a {@link ConfigurationPropertyName} by adapting the given source. See\n\t * {@link #adapt(CharSequence, char, Function)} for details.\n\t * @param name the name to parse\n\t * @param separator the separator used to split the name\n\t * @return a {@link ConfigurationPropertyName}\n\t */",
        "\t/**\n\t * 通过适配给定源创建 {@link ConfigurationPropertyName}。\n\t * 详见 {@link #adapt(CharSequence, char, Function)}。\n\t * @param name the name to parse 待解析的名称\n\t * @param separator the separator used to split the name 元素分隔符\n\t * @return a {@link ConfigurationPropertyName} 名称实例\n\t */",
    ),
    (
        "\t/**\n\t * Create a {@link ConfigurationPropertyName} by adapting the given source. The name\n\t * is split into elements around the given {@code separator}. This method is more\n\t * lenient than {@link #of} in that it allows mixed case names and '{@code _}'\n\t * characters. Other invalid characters are stripped out during parsing.\n\t * <p>\n\t * The {@code elementValueProcessor} function may be used if additional processing is\n\t * required on the extracted element values.\n\t * @param name the name to parse\n\t * @param separator the separator used to split the name\n\t * @param elementValueProcessor a function to process element values\n\t * @return a {@link ConfigurationPropertyName}\n\t */",
        "\t/**\n\t * 通过适配给定源创建 {@link ConfigurationPropertyName}。\n\t * 名称按 {@code separator} 拆分为元素。比 {@link #of} 更宽松：\n\t * 允许混合大小写与 '{@code _}' 字符，其他无效字符在解析时被剔除。\n\t * <p>\n\t * 若需对提取的元素值做额外处理，可使用 {@code elementValueProcessor} 函数。\n\t * @param name the name to parse 待解析的名称\n\t * @param separator the separator used to split the name 元素分隔符\n\t * @param elementValueProcessor a function to process element values 元素值处理函数\n\t * @return a {@link ConfigurationPropertyName} 名称实例\n\t */",
    ),
    (
        "\t/**\n\t * The various forms that a non-indexed element value can take.\n\t */",
        "\t/**\n\t * 非索引元素值可呈现的各种形式。\n\t */",
    ),
    (
        "\t\t/**\n\t\t * The original form as specified when the name was created or adapted. For\n\t\t * example:\n\t\t * <ul>\n\t\t * <li>\"{@code foo-bar}\" = \"{@code foo-bar}\"</li>\n\t\t * <li>\"{@code fooBar}\" = \"{@code fooBar}\"</li>\n\t\t * <li>\"{@code foo_bar}\" = \"{@code foo_bar}\"</li>\n\t\t * <li>\"{@code [Foo.bar]}\" = \"{@code Foo.bar}\"</li>\n\t\t * </ul>\n\t\t */",
        "\t\t/**\n\t\t * 创建或适配名称时指定的原始形式。例如：\n\t\t * <ul>\n\t\t * <li>\"{@code foo-bar}\" = \"{@code foo-bar}\"</li>\n\t\t * <li>\"{@code fooBar}\" = \"{@code fooBar}\"</li>\n\t\t * <li>\"{@code foo_bar}\" = \"{@code foo_bar}\"</li>\n\t\t * <li>\"{@code [Foo.bar]}\" = \"{@code Foo.bar}\"</li>\n\t\t * </ul>\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * The dashed configuration form (used for toString; lower-case with only\n\t\t * alphanumeric characters and dashes).\n\t\t * <ul>\n\t\t * <li>\"{@code foo-bar}\" = \"{@code foo-bar}\"</li>\n\t\t * <li>\"{@code fooBar}\" = \"{@code foobar}\"</li>\n\t\t * <li>\"{@code foo_bar}\" = \"{@code foobar}\"</li>\n\t\t * <li>\"{@code [Foo.bar]}\" = \"{@code Foo.bar}\"</li>\n\t\t * </ul>\n\t\t */",
        "\t\t/**\n\t\t * 短横线配置形式（用于 toString；小写且仅含字母数字与短横线）。\n\t\t * <ul>\n\t\t * <li>\"{@code foo-bar}\" = \"{@code foo-bar}\"</li>\n\t\t * <li>\"{@code fooBar}\" = \"{@code foobar}\"</li>\n\t\t * <li>\"{@code foo_bar}\" = \"{@code foobar}\"</li>\n\t\t * <li>\"{@code [Foo.bar]}\" = \"{@code Foo.bar}\"</li>\n\t\t * </ul>\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * The uniform configuration form (used for equals/hashCode; lower-case with only\n\t\t * alphanumeric characters).\n\t\t * <ul>\n\t\t * <li>\"{@code foo-bar}\" = \"{@code foobar}\"</li>\n\t\t * <li>\"{@code fooBar}\" = \"{@code foobar}\"</li>\n\t\t * <li>\"{@code foo_bar}\" = \"{@code foobar}\"</li>\n\t\t * <li>\"{@code [Foo.bar]}\" = \"{@code Foo.bar}\"</li>\n\t\t * </ul>\n\t\t */",
        "\t\t/**\n\t\t * 统一配置形式（用于 equals/hashCode；小写且仅含字母数字）。\n\t\t * <ul>\n\t\t * <li>\"{@code foo-bar}\" = \"{@code foobar}\"</li>\n\t\t * <li>\"{@code fooBar}\" = \"{@code foobar}\"</li>\n\t\t * <li>\"{@code foo_bar}\" = \"{@code foobar}\"</li>\n\t\t * <li>\"{@code [Foo.bar]}\" = \"{@code Foo.bar}\"</li>\n\t\t * </ul>\n\t\t */",
    ),
    (
        "\t/**\n\t * Allows access to the individual elements that make up the name. We store the\n\t * indexes in arrays rather than a list of object in order to conserve memory.\n\t */",
        "\t/**\n\t * 提供对组成名称的各个元素的访问。\n\t * 为节省内存，索引信息存储在数组中而非对象列表。\n\t */",
    ),
    (
        "\t\t/**\n\t\t * Contains any resolved elements or can be {@code null} if there aren't any.\n\t\t * Resolved elements allow us to modify the element values in some way (or example\n\t\t * when adapting with a mapping function, or when append has been called). Note\n\t\t * that this array is not used as a cache, in fact, when it's not null then\n\t\t * {@link #canShortcutWithSource} will always return false which may hurt\n\t\t * performance.\n\t\t */",
        "\t\t/**\n\t\t * 存放已解析的元素；若无则为 {@code null}。\n\t\t * 已解析元素允许以某种方式修改元素值（例如适配时使用映射函数，或调用 append 后）。\n\t\t * 注意此数组并非缓存：非 null 时 {@link #canShortcutWithSource} 恒为 false，可能影响性能。\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Returns if the element source can be used as a shortcut for an operation such\n\t\t * as {@code equals} or {@code toString}.\n\t\t * @param requiredType the required type\n\t\t * @return {@code true} if all elements match at least one of the types\n\t\t */",
        "\t\t/**\n\t\t * 判断元素源是否可作为 {@code equals}、{@code toString} 等操作的快捷路径。\n\t\t * @param requiredType the required type 要求的元素类型\n\t\t * @return {@code true} if all elements match at least one of the types 全部元素匹配时为 true\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * Returns if the element source can be used as a shortcut for an operation such\n\t\t * as {@code equals} or {@code toString}.\n\t\t * @param requiredType the required type\n\t\t * @param alternativeType and alternative required type\n\t\t * @return {@code true} if all elements match at least one of the types\n\t\t */",
        "\t\t/**\n\t\t * 判断元素源是否可作为 {@code equals}、{@code toString} 等操作的快捷路径。\n\t\t * @param requiredType the required type 要求的元素类型\n\t\t * @param alternativeType and alternative required type 备选要求的元素类型\n\t\t * @return {@code true} if all elements match at least one of the types 全部元素匹配其一为 true\n\t\t */",
    ),
    (
        "\t/**\n\t * Main parsing logic used to convert a {@link CharSequence} to {@link Elements}.\n\t */",
        "\t/**\n\t * 将 {@link CharSequence} 解析为 {@link Elements} 的核心逻辑。\n\t */",
    ),
    (
        "\t/**\n\t * The various types of element that we can detect.\n\t */",
        "\t/**\n\t * 可检测到的各种元素类型。\n\t */",
    ),
    (
        "\t\t/**\n\t\t * The element is logically empty (contains no valid chars).\n\t\t */",
        "\t\t/**\n\t\t * 逻辑上为空（不含有效字符）。\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * The element is a uniform name (a-z, 0-9, no dashes, lowercase).\n\t\t */",
        "\t\t/**\n\t\t * 统一名称（a-z、0-9，无短横线，小写）。\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * The element is almost uniform, but it contains (but does not start with) at\n\t\t * least one dash.\n\t\t */",
        "\t\t/**\n\t\t * 近似统一：含至少一个短横线，但短横线不在开头。\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * The element contains non-uniform characters and will need to be converted.\n\t\t */",
        "\t\t/**\n\t\t * 含非统一字符，需要转换。\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * The element is non-numerically indexed.\n\t\t */",
        "\t\t/**\n\t\t * 非数值型索引元素。\n\t\t */",
    ),
    (
        "\t\t/**\n\t\t * The element is numerically indexed.\n\t\t */",
        "\t\t/**\n\t\t * 数值型索引元素。\n\t\t */",
    ),
    (
        "\t/**\n\t * Predicate used to filter element chars.\n\t */",
        "\t/**\n\t * 用于过滤元素字符的谓词。\n\t */",
    ),
    (
        "\t/**\n\t * Formats for {@code toString}.\n\t */",
        "\t/**\n\t * {@code toString} 使用的格式枚举。\n\t */",
    ),
]
