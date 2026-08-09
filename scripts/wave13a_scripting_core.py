"""Chinese JavaDoc replacements for springframework wave13a scripting core [9:13]."""

SCRIPTING_CORE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ScriptCompilationException.java": [
        (
            "/**\n * Exception to be thrown on script compilation failure.\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 脚本编译失败时抛出的异常。\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Constructor for ScriptCompilationException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * ScriptCompilationException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for ScriptCompilationException.\n\t * @param msg the detail message\n\t * @param cause the root cause (usually from using an underlying script compiler API)\n\t */",
            "\t/**\n\t * ScriptCompilationException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 根因（通常来自底层脚本编译 API）\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for ScriptCompilationException.\n\t * @param scriptSource the source for the offending script\n\t * @param msg the detail message\n\t * @since 4.2\n\t */",
            "\t/**\n\t * ScriptCompilationException 构造函数。\n\t * @param scriptSource 出错脚本的来源\n\t * @param msg 详细消息\n\t * @since 4.2\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for ScriptCompilationException.\n\t * @param scriptSource the source for the offending script\n\t * @param cause the root cause (usually from using an underlying script compiler API)\n\t */",
            "\t/**\n\t * ScriptCompilationException 构造函数。\n\t * @param scriptSource 出错脚本的来源\n\t * @param cause 根因（通常来自底层脚本编译 API）\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for ScriptCompilationException.\n\t * @param scriptSource the source for the offending script\n\t * @param msg the detail message\n\t * @param cause the root cause (usually from using an underlying script compiler API)\n\t */",
            "\t/**\n\t * ScriptCompilationException 构造函数。\n\t * @param scriptSource 出错脚本的来源\n\t * @param msg 详细消息\n\t * @param cause 根因（通常来自底层脚本编译 API）\n\t */",
        ),
        (
            "\t/**\n\t * Return the source for the offending script.\n\t * @return the source, or {@code null} if not available\n\t */",
            "\t/**\n\t * 返回出错脚本的来源。\n\t * @return 脚本来源，若不可用则为 {@code null}\n\t */",
        ),
    ],
    "ScriptEvaluator.java": [
        (
            "/**\n * Spring's strategy interface for evaluating a script.\n *\n * <p>Aside from language-specific implementations, Spring also ships\n * a version based on the standard {@code javax.script} package (JSR-223):\n * {@link org.springframework.scripting.support.StandardScriptEvaluator}.\n *\n * @author Juergen Hoeller\n * @author Costin Leau\n * @since 4.0\n */",
            "/**\n * Spring 用于求值脚本的策略接口。\n *\n * <p>除各语言专用实现外，Spring 还提供基于标准\n * {@code javax.script} 包（JSR-223）的实现：\n * {@link org.springframework.scripting.support.StandardScriptEvaluator}。\n *\n * @author Juergen Hoeller\n * @author Costin Leau\n * @since 4.0\n */",
        ),
        (
            "\t/**\n\t * Evaluate the given script.\n\t * @param script the ScriptSource for the script to evaluate\n\t * @return the return value of the script, if any\n\t * @throws ScriptCompilationException if the evaluator failed to read,\n\t * compile or evaluate the script\n\t */",
            "\t/**\n\t * 求值给定脚本。\n\t * @param script 待求值脚本的 ScriptSource\n\t * @return 脚本返回值（若有）\n\t * @throws ScriptCompilationException 若读取、编译或求值失败\n\t */",
        ),
        (
            "\t/**\n\t * Evaluate the given script with the given arguments.\n\t * @param script the ScriptSource for the script to evaluate\n\t * @param arguments the key-value pairs to expose to the script,\n\t * typically as script variables (may be {@code null} or empty)\n\t * @return the return value of the script, if any\n\t * @throws ScriptCompilationException if the evaluator failed to read,\n\t * compile or evaluate the script\n\t */",
            "\t/**\n\t * 以给定参数求值脚本。\n\t * @param script 待求值脚本的 ScriptSource\n\t * @param arguments 暴露给脚本的键值对，通常作为脚本变量（可为 {@code null} 或空）\n\t * @return 脚本返回值（若有）\n\t * @throws ScriptCompilationException 若读取、编译或求值失败\n\t */",
        ),
    ],
    "ScriptFactory.java": [
        (
            "/**\n * Script definition interface, encapsulating the configuration\n * of a specific script as well as a factory method for\n * creating the actual scripted Java {@code Object}.\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 2.0\n * @see #getScriptSourceLocator\n * @see #getScriptedObject\n */",
            "/**\n * 脚本定义接口，封装特定脚本的配置以及\n * 创建实际脚本化 Java {@code Object} 的工厂方法。\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 2.0\n * @see #getScriptSourceLocator\n * @see #getScriptedObject\n */",
        ),
        (
            "\t/**\n\t * Return a locator that points to the source of the script.\n\t * Interpreted by the post-processor that actually creates the script.\n\t * <p>Typical supported locators are Spring resource locations\n\t * (such as \"file:C:/myScript.bsh\" or \"classpath:myPackage/myScript.bsh\")\n\t * and inline scripts (\"inline:myScriptText...\").\n\t * @return the script source locator\n\t * @see org.springframework.scripting.support.ScriptFactoryPostProcessor#convertToScriptSource\n\t * @see org.springframework.core.io.ResourceLoader\n\t */",
            "\t/**\n\t * 返回指向脚本来源的定位符，由实际创建脚本的后处理器解释。\n\t * <p>常见定位符包括 Spring 资源位置\n\t *（如 \"file:C:/myScript.bsh\" 或 \"classpath:myPackage/myScript.bsh\"）\n\t * 以及内联脚本（\"inline:myScriptText...\"）。\n\t * @return 脚本来源定位符\n\t * @see org.springframework.scripting.support.ScriptFactoryPostProcessor#convertToScriptSource\n\t * @see org.springframework.core.io.ResourceLoader\n\t */",
        ),
        (
            "\t/**\n\t * Return the business interfaces that the script is supposed to implement.\n\t * <p>Can return {@code null} if the script itself determines\n\t * its Java interfaces (such as in the case of Groovy).\n\t * @return the interfaces for the script\n\t */",
            "\t/**\n\t * 返回脚本应实现的业务接口。\n\t * <p>若脚本自行决定 Java 接口（如 Groovy）可返回 {@code null}。\n\t * @return 脚本接口\n\t */",
        ),
        (
            "\t/**\n\t * Return whether the script requires a config interface to be\n\t * generated for it. This is typically the case for scripts that\n\t * do not determine Java signatures themselves, with no appropriate\n\t * config interface specified in {@code getScriptInterfaces()}.\n\t * @return whether the script requires a generated config interface\n\t * @see #getScriptInterfaces()\n\t */",
            "\t/**\n\t * 返回脚本是否需要为其生成配置接口。\n\t * 通常适用于无法自行确定 Java 签名且 {@code getScriptInterfaces()} 中\n\t * 未指定合适配置接口的脚本。\n\t * @return 是否需要生成配置接口\n\t * @see #getScriptInterfaces()\n\t */",
        ),
        (
            "\t/**\n\t * Factory method for creating the scripted Java object.\n\t * <p>Implementations are encouraged to cache script metadata such as\n\t * a generated script class. Note that this method may be invoked\n\t * concurrently and must be implemented in a thread-safe fashion.\n\t * @param scriptSource the actual ScriptSource to retrieve\n\t * the script source text from (never {@code null})\n\t * @param actualInterfaces the actual interfaces to expose,\n\t * including script interfaces as well as a generated config interface\n\t * (if applicable; may be {@code null})\n\t * @return the scripted Java object\n\t * @throws IOException if script retrieval failed\n\t * @throws ScriptCompilationException if script compilation failed\n\t */",
            "\t/**\n\t * 创建脚本化 Java 对象的工厂方法。\n\t * <p>建议实现缓存脚本元数据（如生成的脚本类）。\n\t * 本方法可能被并发调用，须线程安全实现。\n\t * @param scriptSource 获取脚本文本的 ScriptSource（永不为 {@code null}）\n\t * @param actualInterfaces 要暴露的实际接口，含脚本接口及生成的配置接口（若适用；可为 {@code null}）\n\t * @return 脚本化 Java 对象\n\t * @throws IOException 若脚本获取失败\n\t * @throws ScriptCompilationException 若脚本编译失败\n\t */",
        ),
        (
            "\t/**\n\t * Determine the type of the scripted Java object.\n\t * <p>Implementations are encouraged to cache script metadata such as\n\t * a generated script class. Note that this method may be invoked\n\t * concurrently and must be implemented in a thread-safe fashion.\n\t * @param scriptSource the actual ScriptSource to retrieve\n\t * the script source text from (never {@code null})\n\t * @return the type of the scripted Java object, or {@code null}\n\t * if none could be determined\n\t * @throws IOException if script retrieval failed\n\t * @throws ScriptCompilationException if script compilation failed\n\t * @since 2.0.3\n\t */",
            "\t/**\n\t * 确定脚本化 Java 对象的类型。\n\t * <p>建议实现缓存脚本元数据（如生成的脚本类）。\n\t * 本方法可能被并发调用，须线程安全实现。\n\t * @param scriptSource 获取脚本文本的 ScriptSource（永不为 {@code null}）\n\t * @return 脚本化 Java 对象的类型；若无法确定则为 {@code null}\n\t * @throws IOException 若脚本获取失败\n\t * @throws ScriptCompilationException 若脚本编译失败\n\t * @since 2.0.3\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether a refresh is required (for example, through\n\t * ScriptSource's {@code isModified()} method).\n\t * @param scriptSource the actual ScriptSource to retrieve\n\t * the script source text from (never {@code null})\n\t * @return whether a fresh {@link #getScriptedObject} call is required\n\t * @since 2.5.2\n\t * @see ScriptSource#isModified()\n\t */",
            "\t/**\n\t * 判断是否需要刷新（例如通过 ScriptSource 的 {@code isModified()}）。\n\t * @param scriptSource 获取脚本文本的 ScriptSource（永不为 {@code null}）\n\t * @return 是否需要重新调用 {@link #getScriptedObject}\n\t * @since 2.5.2\n\t * @see ScriptSource#isModified()\n\t */",
        ),
    ],
    "ScriptSource.java": [
        (
            "/**\n * Interface that defines the source of a script.\n * Tracks whether the underlying script has been modified.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 定义脚本来源的接口，跟踪底层脚本是否已修改。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Retrieve the current script source text as String.\n\t * @return the script text\n\t * @throws IOException if script retrieval failed\n\t */",
            "\t/**\n\t * 以 String 形式获取当前脚本文本。\n\t * @return 脚本文本\n\t * @throws IOException 若脚本获取失败\n\t */",
        ),
        (
            "\t/**\n\t * Indicate whether the underlying script data has been modified since\n\t * the last time {@link #getScriptAsString()} was called.\n\t * Returns {@code true} if the script has not been read yet.\n\t * @return whether the script data has been modified\n\t */",
            "\t/**\n\t * 指示自上次调用 {@link #getScriptAsString()} 以来底层脚本数据是否已修改。\n\t * 若脚本尚未读取则返回 {@code true}。\n\t * @return 脚本数据是否已修改\n\t */",
        ),
        (
            "\t/**\n\t * Determine a class name for the underlying script.\n\t * @return the suggested class name, or {@code null} if none available\n\t */",
            "\t/**\n\t * 确定底层脚本的类名。\n\t * @return 建议的类名；若无可用名称则为 {@code null}\n\t */",
        ),
    ],
}
