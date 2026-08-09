"""Chinese JavaDoc replacements for Spring Boot 4.1.0 wave16b logback files."""

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "LogbackLoggingSystem.java": [
        (
            "/**\n * {@link LoggingSystem} for <a href=\"https://logback.qos.ch\">logback</a>.\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Andy Wilkinson\n * @author Ben Hale\n * @since 1.0.0\n */",
            "/**\n * 面向 <a href=\"https://logback.qos.ch\">logback</a> 的 {@link LoggingSystem} 实现。\n * 负责 Logback 的初始化、配置加载、日志级别管理，以及 JUL 桥接与 AOT 生成物支持。\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Andy Wilkinson\n * @author Ben Hale\n * @since 1.0.0\n */",
        ),
        (
            "\t\tcatch (Throwable ex) {\n\t\t\t// Ignore. No java.util.logging bridge is installed.\n\t\t}",
            "\t\tcatch (Throwable ex) {\n\t\t\t// 忽略：未安装 java.util.logging 桥接。\n\t\t}",
        ),
        (
            "\t\tcatch (Throwable ex) {\n\t\t\t// Ignore and continue\n\t\t}",
            "\t\tcatch (Throwable ex) {\n\t\t\t// 忽略并继续\n\t\t}",
        ),
        (
            "\t\t\t// Apply system properties directly in case the same JVM runs multiple apps",
            "\t\t\t// 直接应用系统属性，以防同一 JVM 运行多个应用",
        ),
        (
            "\t\tcatch (SecurityException ex) {\n\t\t\t// Unable to determine location\n\t\t}",
            "\t\tcatch (SecurityException ex) {\n\t\t\t// 无法确定加载位置\n\t\t}",
        ),
        (
            "\t/**\n\t * {@link LoggingSystemFactory} that returns {@link LogbackLoggingSystem} if possible.\n\t */",
            "\t/**\n\t * 在 classpath 上存在 Logback 时返回 {@link LogbackLoggingSystem} 的 {@link LoggingSystemFactory}。\n\t */",
        ),
    ],
    "SpringBootJoranConfigurator.java": [
        (
            "/**\n * Extended version of the Logback {@link JoranConfigurator} that adds additional Spring\n * Boot rules.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n */",
            "/**\n * Logback {@link JoranConfigurator} 的扩展版本，添加了 Spring Boot 专用配置规则。\n * 支持 {@code springProperty}、{@code springProfile} 元素，并在 AOT 处理期间\n * 将解析后的模型与 pattern 规则序列化到生成物中。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n */",
        ),
    ],
}
