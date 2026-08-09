"""Chinese JavaDoc replacements for Spring Boot 4.1.0 wave16a StandardStackTracePrinter + Log4J2LoggingSystem."""

STANDARD_STACK_TRACE_PRINTER_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "/**\n * {@link StackTracePrinter} that prints a standard form stack trace. This printer\n * produces a result in a similar form to {@link Throwable#printStackTrace()}, but offers\n * more customization options.\n *\n * @author Phillip Webb\n * @since 3.5.0\n */",
        "/**\n * 打印标准形式堆栈跟踪的 {@link StackTracePrinter}。\n * 输出形态类似 {@link Throwable#printStackTrace()}，但提供更多定制选项。\n *\n * @author Phillip Webb\n * @since 3.5.0\n */",
    ),
    (
        "\t/**\n\t * Return a new {@link StandardStackTracePrinter} from this one that will print all\n\t * common frames rather than replacing them with the {@literal \"... N more\"} message.\n\t * @return a new {@link StandardStackTracePrinter} instance\n\t */",
        "\t/**\n\t * 返回会打印全部公共帧（而非 {@literal \"... N more\"} 省略消息）的新实例。\n\t *\n\t * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link StandardStackTracePrinter} from this one that will not print\n\t * {@link Throwable#getSuppressed() suppressed} items.\n\t * @return a new {@link StandardStackTracePrinter} instance\n\t */",
        "\t/**\n\t * 返回不打印 {@link Throwable#getSuppressed() 被抑制异常} 的新实例。\n\t *\n\t * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link StandardStackTracePrinter} from this one that will use ellipses\n\t * to truncate output longer than the specified length.\n\t * @param maximumLength the maximum length that can be printed\n\t * @return a new {@link StandardStackTracePrinter} instance\n\t */",
        "\t/**\n\t * 返回会用省略号截断超过指定长度输出的新实例。\n\t *\n\t * @param maximumLength the maximum length that can be printed 可打印的最大长度\n\t * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link StandardStackTracePrinter} from this one that filters frames\n\t * (including caused and suppressed) deeper than the specified maximum.\n\t * @param maximumThrowableDepth the maximum throwable depth\n\t * @return a new {@link StandardStackTracePrinter} instance\n\t */",
        "\t/**\n\t * 返回过滤深度超过指定最大值的帧（含 cause 与 suppressed）的新实例。\n\t *\n\t * @param maximumThrowableDepth the maximum throwable depth 最大异常深度\n\t * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link StandardStackTracePrinter} from this one that will only include\n\t * throwables (excluding caused and suppressed) that match the given predicate.\n\t * @param predicate the predicate used to filter the throwable\n\t * @return a new {@link StandardStackTracePrinter} instance\n\t */",
        "\t/**\n\t * 返回仅包含匹配给定谓词的异常（不含 cause 与 suppressed 链过滤逻辑）的新实例。\n\t *\n\t * @param predicate the predicate used to filter the throwable 过滤异常的谓词\n\t * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link StandardStackTracePrinter} from this one that will only include\n\t * frames that match the given predicate.\n\t * @param predicate the predicate used to filter frames\n\t * @return a new {@link StandardStackTracePrinter} instance\n\t */",
        "\t/**\n\t * 返回仅包含匹配给定谓词的栈帧的新实例。\n\t *\n\t * @param predicate the predicate used to filter frames 过滤栈帧的谓词\n\t * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link StandardStackTracePrinter} from this one that prints the stack\n\t * trace using the specified line separator.\n\t * @param lineSeparator the line separator to use\n\t * @return a new {@link StandardStackTracePrinter} instance\n\t */",
        "\t/**\n\t * 返回使用指定行分隔符打印堆栈的新实例。\n\t *\n\t * @param lineSeparator the line separator to use 行分隔符\n\t * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link StandardStackTracePrinter} from this one that uses the\n\t * specified formatter to create a string representation of a throwable.\n\t * @param formatter the formatter to use\n\t * @return a new {@link StandardStackTracePrinter} instance\n\t * @see #withLineSeparator(String)\n\t */",
        "\t/**\n\t * 返回使用指定格式化器生成异常字符串表示的新实例。\n\t *\n\t * @param formatter the formatter to use 格式化器\n\t * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例\n\t * @see #withLineSeparator(String)\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link StandardStackTracePrinter} from this one that uses the\n\t * specified formatter to create a string representation of a frame.\n\t * @param frameFormatter the frame formatter to use\n\t * @return a new {@link StandardStackTracePrinter} instance\n\t * @see #withLineSeparator(String)\n\t */",
        "\t/**\n\t * 返回使用指定格式化器生成栈帧字符串表示的新实例。\n\t *\n\t * @param frameFormatter the frame formatter to use 栈帧格式化器\n\t * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例\n\t * @see #withLineSeparator(String)\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link StandardStackTracePrinter} from this one that generates and\n\t * prints hashes for each stacktrace.\n\t * @return a new {@link StandardStackTracePrinter} instance\n\t */",
        "\t/**\n\t * 返回为每个堆栈生成并打印哈希值的新实例。\n\t *\n\t * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a new {@link StandardStackTracePrinter} from this one that changes if hashes\n\t * should be generated and printed for each stacktrace.\n\t * @param hashes if hashes should be added\n\t * @return a new {@link StandardStackTracePrinter} instance\n\t */",
        "\t/**\n\t * 返回控制是否为每个堆栈生成并打印哈希值的新实例。\n\t *\n\t * @param hashes if hashes should be added 是否添加哈希\n\t * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例\n\t */",
    ),
    (
        "\t/**\n\t * Return a {@link StandardStackTracePrinter} that prints the stack trace with the\n\t * root exception last (the same as {@link Throwable#printStackTrace()}).\n\t * @return a {@link StandardStackTracePrinter} that prints the stack trace root last\n\t */",
        "\t/**\n\t * 返回根异常最后打印的 {@link StandardStackTracePrinter}\n\t * （与 {@link Throwable#printStackTrace()} 相同）。\n\t *\n\t * @return a {@link StandardStackTracePrinter} that prints the stack trace root last 根异常在最后的打印器\n\t */",
    ),
    (
        "\t/**\n\t * Return a {@link StandardStackTracePrinter} that prints the stack trace with the\n\t * root exception first (the opposite of {@link Throwable#printStackTrace()}).\n\t * @return a {@link StandardStackTracePrinter} that prints the stack trace root first\n\t */",
        "\t/**\n\t * 返回根异常最先打印的 {@link StandardStackTracePrinter}\n\t * （与 {@link Throwable#printStackTrace()} 相反）。\n\t *\n\t * @return a {@link StandardStackTracePrinter} that prints the stack trace root first 根异常在前的打印器\n\t */",
    ),
    (
        "\t/**\n\t * Options supported by this printer.\n\t */",
        "\t/**\n\t * 本打印器支持的选项。\n\t */",
    ),
    (
        "\t/**\n\t * Prints the actual line output.\n\t */",
        "\t/**\n\t * 打印实际行输出。\n\t */",
    ),
    (
        "\t/**\n\t * Line-by-line output.\n\t */",
        "\t/**\n\t * 逐行输出。\n\t */",
    ),
    (
        "\t/**\n\t * Holds the stacktrace for a specific throwable and caches things that are expensive\n\t * to calculate.\n\t */",
        "\t/**\n\t * 持有特定异常的堆栈信息，并缓存计算开销较大的数据。\n\t */",
    ),
]

LOG4J2_LOGGING_SYSTEM_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "/**\n * {@link LoggingSystem} for <a href=\"https://logging.apache.org/log4j/2.x/\">Log4j 2</a>.\n *\n * @author Daniel Fullarton\n * @author Andy Wilkinson\n * @author Alexander Heusingfeld\n * @author Ben Hale\n * @author Ralph Goers\n * @author Piotr P. Karwasz\n * @since 1.2.0\n */",
        "/**\n * 面向 <a href=\"https://logging.apache.org/log4j/2.x/\">Log4j 2</a> 的 {@link LoggingSystem} 实现。\n * 负责 Log4j2 的初始化、配置加载、日志级别管理与 JUL 桥接。\n *\n * @author Daniel Fullarton\n * @author Andy Wilkinson\n * @author Alexander Heusingfeld\n * @author Ben Hale\n * @author Ralph Goers\n * @author Piotr P. Karwasz\n * @since 1.2.0\n */",
    ),
    (
        "\t/**\n\t * JUL handler that routes messages to the Log4j API (optional dependency).\n\t */",
        "\t/**\n\t * 将 JUL 消息路由到 Log4j API 的 JUL Handler（可选依赖）。\n\t */",
    ),
    (
        "\t/**\n\t * JUL LogManager that routes messages to the Log4j API as the backend.\n\t */",
        "\t/**\n\t * 以 Log4j API 为后端的 JUL LogManager。\n\t */",
    ),
    (
        "\t/**\n\t * Create a new {@link Log4J2LoggingSystem} instance.\n\t * @param classLoader the class loader to use.\n\t * @throws IllegalArgumentException if the logger context is not a\n\t * {@link LoggerContext}.\n\t */",
        "\t/**\n\t * 创建新的 {@link Log4J2LoggingSystem} 实例。\n\t *\n\t * @param classLoader the class loader to use 使用的类加载器\n\t * @throws IllegalArgumentException if the logger context is not a\n\t * {@link LoggerContext} Logger 上下文不是 {@link LoggerContext} 时\n\t */",
    ),
    (
        "\t\t// With Log4J2 we use the ConfigurationFactory",
        "\t\t// Log4J2 通过 ConfigurationFactory 加载配置，不使用标准配置路径",
    ),
    (
        "\t/**\n\t * Return the configuration location. The result may be:\n\t * <ul>\n\t * <li>{@code null}: if DefaultConfiguration is used (no explicit config loaded)</li>\n\t * <li>A file path: if provided explicitly by the user</li>\n\t * <li>A URI: if loaded from the classpath default or a custom location</li>\n\t * </ul>\n\t * @param configuration the source configuration\n\t * @return the config location or {@code null}\n\t */",
        "\t/**\n\t * 返回配置位置，可能为：\n\t * <ul>\n\t * <li>{@code null}：使用 DefaultConfiguration（未显式加载配置）</li>\n\t * <li>文件路径：用户显式指定</li>\n\t * <li>URI：从 classpath 默认或自定义位置加载</li>\n\t * </ul>\n\t *\n\t * @param configuration the source configuration 源配置\n\t * @return the config location or {@code null} 配置位置或 {@code null}\n\t */",
    ),
    (
        "\t\t\t// Ignore. No java.util.logging bridge is installed.",
        "\t\t\t// 忽略：未安装 java.util.logging 桥接",
    ),
    (
        "\t\t\t// Ignore and continue",
        "\t\t\t// 忽略并继续",
    ),
    (
        "\t\t// The error handling in Log4j Core 2.25.x is not consistent, some loading and\n\t\t// parsing errors result in a null configuration, others in an exception.",
        "\t\t// Log4j Core 2.25.x 的错误处理不一致：部分加载/解析错误返回 null 配置，部分抛出异常",
    ),
    (
        "\t/**\n\t * Get the Spring {@link Environment} attached to the given {@link LoggerContext} or\n\t * {@code null} if no environment is available.\n\t * @param loggerContext the logger context\n\t * @return the Spring {@link Environment} or {@code null}\n\t * @since 3.0.0\n\t */",
        "\t/**\n\t * 获取附加到给定 {@link LoggerContext} 的 Spring {@link Environment}；\n\t * 无可用环境时返回 {@code null}。\n\t *\n\t * @param loggerContext the logger context Logger 上下文\n\t * @return the Spring {@link Environment} or {@code null} Spring 环境或 {@code null}\n\t * @since 3.0.0\n\t */",
    ),
    (
        "\t/**\n\t * {@link LoggingSystemFactory} that returns {@link Log4J2LoggingSystem} if possible.\n\t */",
        "\t/**\n\t * 在可用时返回 {@link Log4J2LoggingSystem} 的 {@link LoggingSystemFactory}。\n\t */",
    ),
    (
        "\t\t\t\t\t// Continue",
        "\t\t\t\t\t// 创建失败则继续尝试其他 LoggingSystem",
    ),
    (
        "\t/**\n\t * {@link LoggerConfig} used when the user has set a specific {@link Level}.\n\t */",
        "\t/**\n\t * 用户显式设置 {@link Level} 时使用的 {@link LoggerConfig}。\n\t */",
    ),
]
