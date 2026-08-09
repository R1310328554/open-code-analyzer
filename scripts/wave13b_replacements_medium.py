"""Wave 13b [20:40] Chinese JavaDoc replacements — medium files."""

MEDIUM_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ResourceScriptSource.java": [
        (
            "/**\n * {@link org.springframework.scripting.ScriptSource} implementation\n * based on Spring's {@link org.springframework.core.io.Resource}\n * abstraction. Loads the script text from the underlying Resource's\n * {@link org.springframework.core.io.Resource#getFile() File} or\n * {@link org.springframework.core.io.Resource#getInputStream() InputStream},\n * and tracks the last-modified timestamp of the file (if possible).\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.core.io.Resource#getInputStream()\n * @see org.springframework.core.io.Resource#getFile()\n * @see org.springframework.core.io.ResourceLoader\n */",
            "/**\n * 基于 Spring {@link org.springframework.core.io.Resource}\n * 抽象的 {@link org.springframework.scripting.ScriptSource} 实现。\n * 从底层 Resource 的 {@link org.springframework.core.io.Resource#getFile() File} 或\n * {@link org.springframework.core.io.Resource#getInputStream() InputStream} 加载脚本文本，\n * 并跟踪文件的最后修改时间戳（若可能）。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.core.io.Resource#getInputStream()\n * @see org.springframework.core.io.Resource#getFile()\n * @see org.springframework.core.io.ResourceLoader\n */",
        ),
        (
            "\t/** Logger available to subclasses. */",
            "\t/** 子类可用的 Logger。 */",
        ),
        (
            "\t/**\n\t * Create a new ResourceScriptSource for the given resource.\n\t * @param resource the EncodedResource to load the script from\n\t */",
            "\t/**\n\t * 为给定资源创建新的 ResourceScriptSource。\n\t * @param resource 加载脚本的 EncodedResource\n\t */",
        ),
        (
            "\t/**\n\t * Create a new ResourceScriptSource for the given resource.\n\t * @param resource the Resource to load the script from (using UTF-8 encoding)\n\t */",
            "\t/**\n\t * 为给定资源创建新的 ResourceScriptSource。\n\t * @param resource 加载脚本的 Resource（使用 UTF-8 编码）\n\t */",
        ),
        (
            "\t/**\n\t * Return the {@link org.springframework.core.io.Resource} to load the\n\t * script from.\n\t */",
            "\t/**\n\t * 返回用于加载脚本的 {@link org.springframework.core.io.Resource}。\n\t */",
        ),
        (
            "\t/**\n\t * Set the encoding used for reading the script resource.\n\t * <p>The default value for regular Resources is \"UTF-8\".\n\t * A {@code null} value implies the platform default.\n\t */",
            "\t/**\n\t * 设置读取脚本资源时使用的编码。\n\t * <p>常规 Resource 的默认值为 \"UTF-8\"。\n\t * {@code null} 值表示使用平台默认编码。\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the current last-modified timestamp of the underlying resource.\n\t * @return the current timestamp, or 0 if not determinable\n\t */",
            "\t/**\n\t * 检索底层资源的当前最后修改时间戳。\n\t * @return 当前时间戳，若无法确定则返回 0\n\t */",
        ),
    ],
    "StandardScriptEvaluator.java": [
        (
            "/**\n * {@code javax.script} (JSR-223) based implementation of Spring's {@link ScriptEvaluator}\n * strategy interface.\n *\n * @author Juergen Hoeller\n * @author Costin Leau\n * @since 4.0\n * @see ScriptEngine#eval(String)\n */",
            "/**\n * 基于 {@code javax.script}（JSR-223）的 Spring {@link ScriptEvaluator}\n * 策略接口实现。\n *\n * @author Juergen Hoeller\n * @author Costin Leau\n * @since 4.0\n * @see ScriptEngine#eval(String)\n */",
        ),
        (
            "\t/**\n\t * Construct a new {@code StandardScriptEvaluator}.\n\t */",
            "\t/**\n\t * 构造新的 {@code StandardScriptEvaluator}。\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new {@code StandardScriptEvaluator} for the given class loader.\n\t * @param classLoader the class loader to use for script engine detection\n\t */",
            "\t/**\n\t * 为给定类加载器构造新的 {@code StandardScriptEvaluator}。\n\t * @param classLoader 用于脚本引擎检测的类加载器\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new {@code StandardScriptEvaluator} for the given JSR-223\n\t * {@link ScriptEngineManager} to obtain script engines from.\n\t * @param scriptEngineManager the ScriptEngineManager (or subclass thereof) to use\n\t * @since 4.2.2\n\t */",
            "\t/**\n\t * 为给定 JSR-223 {@link ScriptEngineManager} 构造新的\n\t * {@code StandardScriptEvaluator}，以从中获取脚本引擎。\n\t * @param scriptEngineManager 要使用的 ScriptEngineManager（或其子类）\n\t * @since 4.2.2\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the language meant for evaluating the scripts (for example, \"Groovy\").\n\t * <p>This is effectively an alias for {@link #setEngineName \"engineName\"},\n\t * potentially (but not yet) providing common abbreviations for certain languages\n\t * beyond what the JSR-223 script engine factory exposes.\n\t * @see #setEngineName\n\t */",
            "\t/**\n\t * 设置用于评估脚本的语言名称（例如 \"Groovy\"）。\n\t * <p>这实际上是 {@link #setEngineName \"engineName\"} 的别名，\n\t * 将来可能（但尚未）为 JSR-223 脚本引擎工厂未暴露的某些语言\n\t * 提供常见缩写。\n\t * @see #setEngineName\n\t */",
        ),
        (
            "\t/**\n\t * Set the name of the script engine for evaluating the scripts (for example, \"Groovy\"),\n\t * as exposed by the JSR-223 script engine factory.\n\t * @since 4.2.2\n\t * @see #setLanguage\n\t */",
            "\t/**\n\t * 设置用于评估脚本的脚本引擎名称（例如 \"Groovy\"），\n\t * 即 JSR-223 脚本引擎工厂暴露的名称。\n\t * @since 4.2.2\n\t * @see #setLanguage\n\t */",
        ),
        (
            "\t/**\n\t * Set the globally scoped bindings on the underlying script engine manager,\n\t * shared by all scripts, as an alternative to script argument bindings.\n\t * @since 4.2.2\n\t * @see #evaluate(ScriptSource, Map)\n\t * @see javax.script.ScriptEngineManager#setBindings(Bindings)\n\t * @see javax.script.SimpleBindings\n\t */",
            "\t/**\n\t * 在底层脚本引擎管理器上设置全局作用域绑定，\n\t * 由所有脚本共享，作为脚本参数绑定的替代方案。\n\t * @since 4.2.2\n\t * @see #evaluate(ScriptSource, Map)\n\t * @see javax.script.ScriptEngineManager#setBindings(Bindings)\n\t * @see javax.script.SimpleBindings\n\t */",
        ),
        (
            "\t/**\n\t * Obtain the JSR-223 ScriptEngine to use for the given script.\n\t * @param script the script to evaluate\n\t * @return the ScriptEngine (never {@code null})\n\t */",
            "\t/**\n\t * 获取用于给定脚本的 JSR-223 ScriptEngine。\n\t * @param script 要评估的脚本\n\t * @return ScriptEngine（永不为 {@code null}）\n\t */",
        ),
    ],
    "StandardScriptFactory.java": [
        (
            "/**\n * {@link org.springframework.scripting.ScriptFactory} implementation based\n * on the JSR-223 script engine abstraction (as included in Java).\n * Supports JavaScript, Groovy, JRuby, and other JSR-223 compliant engines.\n *\n * <p>Typically used in combination with a\n * {@link org.springframework.scripting.support.ScriptFactoryPostProcessor};\n * see the latter's javadoc for a configuration example.\n *\n * @author Juergen Hoeller\n * @since 4.2\n * @see ScriptFactoryPostProcessor\n */",
            "/**\n * 基于 JSR-223 脚本引擎抽象（Java 内置）的\n * {@link org.springframework.scripting.ScriptFactory} 实现。\n * 支持 JavaScript、Groovy、JRuby 及其他符合 JSR-223 的引擎。\n *\n * <p>通常与 {@link org.springframework.scripting.support.ScriptFactoryPostProcessor} 配合使用；\n * 配置示例见后者的 javadoc。\n *\n * @author Juergen Hoeller\n * @since 4.2\n * @see ScriptFactoryPostProcessor\n */",
        ),
        (
            "\t/**\n\t * Create a new StandardScriptFactory for the given script source.\n\t * @param scriptSourceLocator a locator that points to the source of the script.\n\t * Interpreted by the post-processor that actually creates the script.\n\t */",
            "\t/**\n\t * 为给定脚本源创建新的 StandardScriptFactory。\n\t * @param scriptSourceLocator 指向脚本源的定位器，\n\t * 由实际创建脚本的后处理器解释。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new StandardScriptFactory for the given script source.\n\t * @param scriptSourceLocator a locator that points to the source of the script.\n\t * Interpreted by the post-processor that actually creates the script.\n\t * @param scriptInterfaces the Java interfaces that the scripted object\n\t * is supposed to implement\n\t */",
            "\t/**\n\t * 为给定脚本源创建新的 StandardScriptFactory。\n\t * @param scriptSourceLocator 指向脚本源的定位器，\n\t * 由实际创建脚本的后处理器解释。\n\t * @param scriptInterfaces 脚本对象应实现的 Java 接口\n\t */",
        ),
        (
            "\t/**\n\t * Create a new StandardScriptFactory for the given script source.\n\t * @param scriptEngineName the name of the JSR-223 ScriptEngine to use\n\t * (explicitly given instead of inferred from the script source)\n\t * @param scriptSourceLocator a locator that points to the source of the script.\n\t * Interpreted by the post-processor that actually creates the script.\n\t */",
            "\t/**\n\t * 为给定脚本源创建新的 StandardScriptFactory。\n\t * @param scriptEngineName 要使用的 JSR-223 ScriptEngine 名称\n\t * （显式指定而非从脚本源推断）\n\t * @param scriptSourceLocator 指向脚本源的定位器，\n\t * 由实际创建脚本的后处理器解释。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new StandardScriptFactory for the given script source.\n\t * @param scriptEngineName the name of the JSR-223 ScriptEngine to use\n\t * (explicitly given instead of inferred from the script source)\n\t * @param scriptSourceLocator a locator that points to the source of the script.\n\t * Interpreted by the post-processor that actually creates the script.\n\t * @param scriptInterfaces the Java interfaces that the scripted object\n\t * is supposed to implement\n\t */",
            "\t/**\n\t * 为给定脚本源创建新的 StandardScriptFactory。\n\t * @param scriptEngineName 要使用的 JSR-223 ScriptEngine 名称\n\t * （显式指定而非从脚本源推断）\n\t * @param scriptSourceLocator 指向脚本源的定位器，\n\t * 由实际创建脚本的后处理器解释。\n\t * @param scriptInterfaces 脚本对象应实现的 Java 接口\n\t */",
        ),
        (
            "\t/**\n\t * Load and parse the script via JSR-223's ScriptEngine.\n\t */",
            "\t/**\n\t * 通过 JSR-223 的 ScriptEngine 加载并解析脚本。\n\t */",
        ),
    ],
    "Model.java": [
        (
            "/**\n * Interface that defines a holder for model attributes.\n *\n * <p>Primarily designed for adding attributes to the model.\n *\n * <p>Allows for accessing the overall model as a {@code java.util.Map}.\n *\n * @author Juergen Hoeller\n * @since 2.5.1\n */",
            "/**\n * 定义模型属性持有者的接口。\n *\n * <p>主要用于向模型添加属性。\n *\n * <p>允许以 {@code java.util.Map} 形式访问整体模型。\n *\n * @author Juergen Hoeller\n * @since 2.5.1\n */",
        ),
        (
            "\t/**\n\t * Add the supplied attribute under the supplied name.\n\t * @param attributeName the name of the model attribute (never {@code null})\n\t * @param attributeValue the model attribute value (can be {@code null})\n\t */",
            "\t/**\n\t * 以给定名称添加所提供的属性。\n\t * @param attributeName 模型属性名称（永不为 {@code null}）\n\t * @param attributeValue 模型属性值（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Add the supplied attribute to this {@code Map} using a\n\t * {@link org.springframework.core.Conventions#getVariableName generated name}.\n\t * <p><i>Note: Empty {@link java.util.Collection Collections} are not added to\n\t * the model when using this method because we cannot correctly determine\n\t * the true convention name. View code should check for {@code null} rather\n\t * than for empty collections as is already done by JSTL tags.</i>\n\t * @param attributeValue the model attribute value (never {@code null})\n\t */",
            "\t/**\n\t * 使用 {@link org.springframework.core.Conventions#getVariableName 生成的名称}\n\t * 将所提供的属性添加到本 {@code Map}。\n\t * <p><i>注意：使用本方法时，空的 {@link java.util.Collection Collection}\n\t * 不会添加到模型，因为我们无法正确确定真正的约定名称。\n\t * 视图代码应检查 {@code null} 而非空集合，与 JSTL 标签的做法一致。</i>\n\t * @param attributeValue 模型属性值（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Copy all attributes in the supplied {@code Collection} into this\n\t * {@code Map}, using attribute name generation for each element.\n\t * @see #addAttribute(Object)\n\t */",
            "\t/**\n\t * 将所提供 {@code Collection} 中的所有属性复制到本 {@code Map}，\n\t * 为每个元素生成属性名。\n\t * @see #addAttribute(Object)\n\t */",
        ),
        (
            "\t/**\n\t * Copy all attributes in the supplied {@code Map} into this {@code Map}.\n\t * @see #addAttribute(String, Object)\n\t */",
            "\t/**\n\t * 将所提供 {@code Map} 中的所有属性复制到本 {@code Map}。\n\t * @see #addAttribute(String, Object)\n\t */",
        ),
        (
            "\t/**\n\t * Copy all attributes in the supplied {@code Map} into this {@code Map},\n\t * with existing objects of the same name taking precedence (i.e. not getting\n\t * replaced).\n\t */",
            "\t/**\n\t * 将所提供 {@code Map} 中的所有属性复制到本 {@code Map}，\n\t * 同名已有对象优先（即不会被替换）。\n\t */",
        ),
        (
            "\t/**\n\t * Does this model contain an attribute of the given name?\n\t * @param attributeName the name of the model attribute (never {@code null})\n\t * @return whether this model contains a corresponding attribute\n\t */",
            "\t/**\n\t * 本模型是否包含给定名称的属性？\n\t * @param attributeName 模型属性名称（永不为 {@code null}）\n\t * @return 本模型是否包含对应属性\n\t */",
        ),
        (
            "\t/**\n\t * Return the attribute value for the given name, if any.\n\t * @param attributeName the name of the model attribute (never {@code null})\n\t * @return the corresponding attribute value, or {@code null} if none\n\t * @since 5.2\n\t */",
            "\t/**\n\t * 返回给定名称的属性值（若有）。\n\t * @param attributeName 模型属性名称（永不为 {@code null}）\n\t * @return 对应的属性值，若无则返回 {@code null}\n\t * @since 5.2\n\t */",
        ),
        (
            "\t/**\n\t * Return the current set of model attributes as a Map.\n\t */",
            "\t/**\n\t * 以 Map 形式返回当前模型属性集合。\n\t */",
        ),
    ],
}
