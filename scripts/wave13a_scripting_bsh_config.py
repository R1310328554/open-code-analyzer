"""Chinese JavaDoc replacements for springframework wave13a bsh + config [14:20]."""

SCRIPTING_BSH_CONFIG_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "BshScriptEvaluator.java": [
        (
            "/**\n * BeanShell-based implementation of Spring's {@link ScriptEvaluator} strategy interface.\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see Interpreter#eval(String)\n * @deprecated with no replacement as not actively maintained anymore\n */",
            "/**\n * 基于 BeanShell 的 Spring {@link ScriptEvaluator} 策略接口实现。\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see Interpreter#eval(String)\n * @deprecated 无替代方案，已不再积极维护\n */",
        ),
        (
            "\t/**\n\t * Construct a new BshScriptEvaluator.\n\t */",
            "\t/**\n\t * 构造新的 BshScriptEvaluator。\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new BshScriptEvaluator.\n\t * @param classLoader the ClassLoader to use for the {@link Interpreter}\n\t */",
            "\t/**\n\t * 构造新的 BshScriptEvaluator。\n\t * @param classLoader 用于 {@link Interpreter} 的 ClassLoader\n\t */",
        ),
    ],
    "BshScriptFactory.java": [
        (
            "/**\n * {@link org.springframework.scripting.ScriptFactory} implementation\n * for a BeanShell script.\n *\n * <p>Typically used in combination with a\n * {@link org.springframework.scripting.support.ScriptFactoryPostProcessor};\n * see the latter's javadoc for a configuration example.\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 2.0\n * @see BshScriptUtils\n * @see org.springframework.scripting.support.ScriptFactoryPostProcessor\n * @deprecated with no replacement as not actively maintained anymore\n */",
            "/**\n * BeanShell 脚本的 {@link org.springframework.scripting.ScriptFactory} 实现。\n *\n * <p>通常与 {@link org.springframework.scripting.support.ScriptFactoryPostProcessor}\n * 配合使用；配置示例见后者的 JavaDoc。\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @since 2.0\n * @see BshScriptUtils\n * @see org.springframework.scripting.support.ScriptFactoryPostProcessor\n * @deprecated 无替代方案，已不再积极维护\n */",
        ),
        (
            "\t/**\n\t * Create a new BshScriptFactory for the given script source.\n\t * <p>With this {@code BshScriptFactory} variant, the script needs to\n\t * declare a full class or return an actual instance of the scripted object.\n\t * @param scriptSourceLocator a locator that points to the source of the script.\n\t * Interpreted by the post-processor that actually creates the script.\n\t */",
            "\t/**\n\t * 为给定脚本来源创建新的 BshScriptFactory。\n\t * <p>此 {@code BshScriptFactory} 变体要求脚本声明完整类\n\t * 或返回脚本化对象的实际实例。\n\t * @param scriptSourceLocator 指向脚本来源的定位符，由实际创建脚本的后处理器解释。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new BshScriptFactory for the given script source.\n\t * <p>The script may either be a simple script that needs a corresponding proxy\n\t * generated (implementing the specified interfaces), or declare a full class\n\t * or return an actual instance of the scripted object (in which case the\n\t * specified interfaces, if any, need to be implemented by that class/instance).\n\t * @param scriptSourceLocator a locator that points to the source of the script.\n\t * Interpreted by the post-processor that actually creates the script.\n\t * @param scriptInterfaces the Java interfaces that the scripted object\n\t * is supposed to implement (may be {@code null})\n\t */",
            "\t/**\n\t * 为给定脚本来源创建新的 BshScriptFactory。\n\t * <p>脚本可以是需生成对应代理（实现指定接口）的简单脚本，\n\t * 也可声明完整类或返回脚本化对象实例\n\t *（此时指定接口须由该类/实例实现）。\n\t * @param scriptSourceLocator 指向脚本来源的定位符，由实际创建脚本的后处理器解释。\n\t * @param scriptInterfaces 脚本化对象应实现的 Java 接口（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * BeanShell scripts do require a config interface.\n\t */",
            "\t/**\n\t * BeanShell 脚本需要配置接口。\n\t */",
        ),
        (
            "\t/**\n\t * Load and parse the BeanShell script via {@link BshScriptUtils}.\n\t * @see BshScriptUtils#createBshObject(String, Class[], ClassLoader)\n\t */",
            "\t/**\n\t * 通过 {@link BshScriptUtils} 加载并解析 BeanShell 脚本。\n\t * @see BshScriptUtils#createBshObject(String, Class[], ClassLoader)\n\t */",
        ),
    ],
    "BshScriptUtils.java": [
        (
            "/**\n * Utility methods for handling BeanShell-scripted objects.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @deprecated with no replacement as not actively maintained anymore\n */",
            "/**\n * 处理 BeanShell 脚本化对象的实用方法。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @deprecated 无替代方案，已不再积极维护\n */",
        ),
        (
            "\t/**\n\t * Create a new BeanShell-scripted object from the given script source.\n\t * <p>With this {@code createBshObject} variant, the script needs to\n\t * declare a full class or return an actual instance of the scripted object.\n\t * @param scriptSource the script source text\n\t * @return the scripted Java object\n\t * @throws EvalError in case of BeanShell parsing failure\n\t */",
            "\t/**\n\t * 从给定脚本文本创建新的 BeanShell 脚本化对象。\n\t * <p>此 {@code createBshObject} 变体要求脚本声明完整类\n\t * 或返回脚本化对象的实际实例。\n\t * @param scriptSource 脚本文本\n\t * @return 脚本化 Java 对象\n\t * @throws EvalError BeanShell 解析失败时\n\t */",
        ),
        (
            "\t/**\n\t * Create a new BeanShell-scripted object from the given script source,\n\t * using the default ClassLoader.\n\t * <p>The script may either be a simple script that needs a corresponding proxy\n\t * generated (implementing the specified interfaces), or declare a full class\n\t * or return an actual instance of the scripted object (in which case the\n\t * specified interfaces, if any, need to be implemented by that class/instance).\n\t * @param scriptSource the script source text\n\t * @param scriptInterfaces the interfaces that the scripted Java object is\n\t * supposed to implement (may be {@code null} or empty if the script itself\n\t * declares a full class or returns an actual instance of the scripted object)\n\t * @return the scripted Java object\n\t * @throws EvalError in case of BeanShell parsing failure\n\t * @see #createBshObject(String, Class[], ClassLoader)\n\t */",
            "\t/**\n\t * 使用默认 ClassLoader 从给定脚本文本创建 BeanShell 脚本化对象。\n\t * <p>脚本可以是需生成对应代理的简单脚本，也可声明完整类或返回实例\n\t *（此时指定接口须由该类/实例实现）。\n\t * @param scriptSource 脚本文本\n\t * @param scriptInterfaces 脚本化 Java 对象应实现的接口\n\t *（若脚本自行声明完整类或返回实例，可为 {@code null} 或空）\n\t * @return 脚本化 Java 对象\n\t * @throws EvalError BeanShell 解析失败时\n\t * @see #createBshObject(String, Class[], ClassLoader)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new BeanShell-scripted object from the given script source.\n\t * <p>The script may either be a simple script that needs a corresponding proxy\n\t * generated (implementing the specified interfaces), or declare a full class\n\t * or return an actual instance of the scripted object (in which case the\n\t * specified interfaces, if any, need to be implemented by that class/instance).\n\t * @param scriptSource the script source text\n\t * @param scriptInterfaces the interfaces that the scripted Java object is\n\t * supposed to implement (may be {@code null} or empty if the script itself\n\t * declares a full class or returns an actual instance of the scripted object)\n\t * @param classLoader the ClassLoader to use for evaluating the script\n\t * @return the scripted Java object\n\t * @throws EvalError in case of BeanShell parsing failure\n\t */",
            "\t/**\n\t * 从给定脚本文本创建 BeanShell 脚本化对象。\n\t * <p>脚本可以是需生成对应代理的简单脚本，也可声明完整类或返回实例\n\t *（此时指定接口须由该类/实例实现）。\n\t * @param scriptSource 脚本文本\n\t * @param scriptInterfaces 脚本化 Java 对象应实现的接口\n\t *（若脚本自行声明完整类或返回实例，可为 {@code null} 或空）\n\t * @param classLoader 用于求值脚本的 ClassLoader\n\t * @return 脚本化 Java 对象\n\t * @throws EvalError BeanShell 解析失败时\n\t */",
        ),
        (
            "\t/**\n\t * Evaluate the specified BeanShell script based on the given script source,\n\t * returning the Class defined by the script.\n\t * <p>The script may either declare a full class or return an actual instance of\n\t * the scripted object (in which case the Class of the object will be returned).\n\t * In any other case, the returned Class will be {@code null}.\n\t * @param scriptSource the script source text\n\t * @param classLoader the ClassLoader to use for evaluating the script\n\t * @return the scripted Java class, or {@code null} if none could be determined\n\t * @throws EvalError in case of BeanShell parsing failure\n\t */",
            "\t/**\n\t * 根据给定脚本文本求值 BeanShell 脚本，返回脚本定义的 Class。\n\t * <p>脚本可声明完整类或返回脚本化对象实例（此时返回该对象的 Class）；\n\t * 其他情况返回 {@code null}。\n\t * @param scriptSource 脚本文本\n\t * @param classLoader 用于求值脚本的 ClassLoader\n\t * @return 脚本化 Java 类；若无法确定则为 {@code null}\n\t * @throws EvalError BeanShell 解析失败时\n\t */",
        ),
        (
            "\t/**\n\t * Evaluate the specified BeanShell script based on the given script source,\n\t * keeping a returned script Class or script Object as-is.\n\t * <p>The script may either be a simple script that needs a corresponding proxy\n\t * generated (implementing the specified interfaces), or declare a full class\n\t * or return an actual instance of the scripted object (in which case the\n\t * specified interfaces, if any, need to be implemented by that class/instance).\n\t * @param scriptSource the script source text\n\t * @param scriptInterfaces the interfaces that the scripted Java object is\n\t * supposed to implement (may be {@code null} or empty if the script itself\n\t * declares a full class or returns an actual instance of the scripted object)\n\t * @param classLoader the ClassLoader to use for evaluating the script\n\t * @return the scripted Java class or Java object\n\t * @throws EvalError in case of BeanShell parsing failure\n\t */",
            "\t/**\n\t * 根据给定脚本文本求值 BeanShell 脚本，原样保留返回的 Class 或 Object。\n\t * <p>脚本可以是需生成对应代理的简单脚本，也可声明完整类或返回实例\n\t *（此时指定接口须由该类/实例实现）。\n\t * @param scriptSource 脚本文本\n\t * @param scriptInterfaces 脚本化 Java 对象应实现的接口\n\t * @param classLoader 用于求值脚本的 ClassLoader\n\t * @return 脚本化 Java 类或 Java 对象\n\t * @throws EvalError BeanShell 解析失败时\n\t */",
        ),
        (
            "\t/**\n\t * InvocationHandler that invokes a BeanShell script method.\n\t */",
            "\t/**\n\t * 调用 BeanShell 脚本方法的 InvocationHandler。\n\t */",
        ),
        (
            "\t/**\n\t * Exception to be thrown on script execution failure.\n\t */",
            "\t/**\n\t * 脚本执行失败时抛出的异常。\n\t */",
        ),
    ],
    "LangNamespaceHandler.java": [
        (
            "/**\n * {@code NamespaceHandler} that supports the wiring of\n * objects backed by dynamic languages such as Groovy, JRuby and\n * BeanShell. The following is an example (from the reference\n * documentation) that details the wiring of a Groovy backed bean:\n *\n * <pre class=\"code\">\n * &lt;lang:groovy id=\"messenger\"\n *     refresh-check-delay=\"5000\"\n *     script-source=\"classpath:Messenger.groovy\"&gt;\n * &lt;lang:property name=\"message\" value=\"I Can Do The Frug\"/&gt;\n * &lt;/lang:groovy&gt;\n * </pre>\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n * @deprecated with no replacement as not actively maintained anymore\n */",
            "/**\n * 支持装配 Groovy、JRuby、BeanShell 等动态语言\n * 所支持对象的 {@code NamespaceHandler}。以下为参考文档中\n * 装配 Groovy Bean 的示例：\n *\n * <pre class=\"code\">\n * &lt;lang:groovy id=\"messenger\"\n *     refresh-check-delay=\"5000\"\n *     script-source=\"classpath:Messenger.groovy\"&gt;\n * &lt;lang:property name=\"message\" value=\"I Can Do The Frug\"/&gt;\n * &lt;/lang:groovy&gt;\n * </pre>\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n * @deprecated 无替代方案，已不再积极维护\n */",
        ),
    ],
    "LangNamespaceUtils.java": [
        (
            "/**\n * Utilities for use with {@link LangNamespaceHandler}.\n *\n * @author Rob Harrop\n * @author Mark Fisher\n * @since 2.5\n * @deprecated with no replacement as not actively maintained anymore\n */",
            "/**\n * 与 {@link LangNamespaceHandler} 配合使用的实用工具。\n *\n * @author Rob Harrop\n * @author Mark Fisher\n * @since 2.5\n * @deprecated 无替代方案，已不再积极维护\n */",
        ),
        (
            "\t/**\n\t * The unique name under which the internally managed {@link ScriptFactoryPostProcessor} is\n\t * registered in the {@link BeanDefinitionRegistry}.\n\t */",
            "\t/**\n\t * 内部管理的 {@link ScriptFactoryPostProcessor} 在\n\t * {@link BeanDefinitionRegistry} 中注册时使用的唯一名称。\n\t */",
        ),
        (
            "\t/**\n\t * Register a {@link ScriptFactoryPostProcessor} bean definition in the supplied\n\t * {@link BeanDefinitionRegistry} if the {@link ScriptFactoryPostProcessor} hasn't\n\t * already been registered.\n\t * @param registry the {@link BeanDefinitionRegistry} to register the script processor with\n\t * @return the {@link ScriptFactoryPostProcessor} bean definition (new or already registered)\n\t */",
            "\t/**\n\t * 若尚未注册，则在给定 {@link BeanDefinitionRegistry} 中\n\t * 注册 {@link ScriptFactoryPostProcessor} Bean 定义。\n\t * @param registry 要注册脚本处理器的 {@link BeanDefinitionRegistry}\n\t * @return {@link ScriptFactoryPostProcessor} Bean 定义（新建或已存在）\n\t */",
        ),
    ],
    "ScriptBeanDefinitionParser.java": [
        (
            "/**\n * BeanDefinitionParser implementation for the '{@code <lang:groovy/>}',\n * '{@code <lang:std/>}' and '{@code <lang:bsh/>}' tags.\n * Allows for objects written using dynamic languages to be easily exposed with\n * the {@link org.springframework.beans.factory.BeanFactory}.\n *\n * <p>The script for each object can be specified either as a reference to the\n * resource containing it (using the '{@code script-source}' attribute) or inline\n * in the XML configuration itself (using the '{@code inline-script}' attribute.\n *\n * <p>By default, dynamic objects created with these tags are <strong>not</strong>\n * refreshable. To enable refreshing, specify the refresh check delay for each\n * object (in milliseconds) using the '{@code refresh-check-delay}' attribute.\n *\n * @author Rob Harrop\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n * @deprecated with no replacement as not actively maintained anymore\n */",
            "/**\n * '{@code <lang:groovy/>}'、'{@code <lang:std/>}' 与 '{@code <lang:bsh/>}'\n * 标签的 BeanDefinitionParser 实现，便于将通过动态语言编写的对象\n * 暴露给 {@link org.springframework.beans.factory.BeanFactory}。\n *\n * <p>每个对象的脚本可通过引用资源（'{@code script-source}' 属性）\n * 或在 XML 配置中内联（'{@code inline-script}' 属性）指定。\n *\n * <p>默认情况下，通过这些标签创建的动态对象<strong>不可</strong>刷新。\n * 要启用刷新，请用 '{@code refresh-check-delay}' 属性（毫秒）指定刷新检查延迟。\n *\n * @author Rob Harrop\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n * @deprecated 无替代方案，已不再积极维护\n */",
        ),
        (
            "\t/**\n\t * The {@link org.springframework.scripting.ScriptFactory} class that this\n\t * parser instance will create bean definitions for.\n\t */",
            "\t/**\n\t * 本解析器实例将为其创建 Bean 定义的\n\t * {@link org.springframework.scripting.ScriptFactory} 类。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of this parser, creating bean definitions for the\n\t * supplied {@link org.springframework.scripting.ScriptFactory} class.\n\t * @param scriptFactoryClassName the ScriptFactory class to operate on\n\t */",
            "\t/**\n\t * 创建本解析器的新实例，为给定的\n\t * {@link org.springframework.scripting.ScriptFactory} 类创建 Bean 定义。\n\t * @param scriptFactoryClassName 要操作的 ScriptFactory 类\n\t */",
        ),
        (
            "\t/**\n\t * Parses the dynamic object element and returns the resulting bean definition.\n\t * Registers a {@link ScriptFactoryPostProcessor} if needed.\n\t */",
            "\t/**\n\t * 解析动态对象元素并返回生成的 Bean 定义。\n\t * 必要时注册 {@link ScriptFactoryPostProcessor}。\n\t */",
        ),
        (
            "\t/**\n\t * Resolves the script source from either the '{@code script-source}' attribute or\n\t * the '{@code inline-script}' element. Logs and {@link XmlReaderContext#error} and\n\t * returns {@code null} if neither or both of these values are specified.\n\t */",
            "\t/**\n\t * 从 '{@code script-source}' 属性或 '{@code inline-script}' 元素解析脚本来源。\n\t * 若两者均未指定或同时指定，则记录日志、调用 {@link XmlReaderContext#error}\n\t * 并返回 {@code null}。\n\t */",
        ),
        (
            "\t/**\n\t * Scripted beans may be anonymous as well.\n\t */",
            "\t/**\n\t * 脚本化 Bean 也可以是匿名的。\n\t */",
        ),
    ],
    "ScriptingDefaultsParser.java": [
        (
            "/**\n * A {@link BeanDefinitionParser} for use when loading scripting XML.\n *\n * @author Mark Fisher\n * @since 2.5\n * @deprecated with no replacement as not actively maintained anymore\n */",
            "/**\n * 加载脚本 XML 时使用的 {@link BeanDefinitionParser}。\n *\n * @author Mark Fisher\n * @since 2.5\n * @deprecated 无替代方案，已不再积极维护\n */",
        ),
    ],
}
