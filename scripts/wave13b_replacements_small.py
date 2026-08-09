"""Wave 13b [20:40] Chinese JavaDoc replacements — small files."""

SMALL_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "GroovyObjectCustomizer.java": [
        (
            "/**\n * Strategy used by {@link GroovyScriptFactory} to allow the customization of\n * a created {@link GroovyObject}.\n *\n * <p>This is useful to allow the authoring of DSLs, the replacement of missing\n * methods, and so forth. For example, a custom {@link groovy.lang.MetaClass}\n * could be specified.\n *\n * @author Rod Johnson\n * @since 2.0.2\n * @see GroovyScriptFactory\n */",
            "/**\n * {@link GroovyScriptFactory} 使用的策略，用于定制所创建的 {@link GroovyObject}。\n *\n * <p>可用于编写 DSL、替换缺失方法等。例如，可指定自定义 {@link groovy.lang.MetaClass}。\n *\n * @author Rod Johnson\n * @since 2.0.2\n * @see GroovyScriptFactory\n */",
        ),
        (
            "\t/**\n\t * Customize the supplied {@link GroovyObject}.\n\t * <p>For example, this can be used to set a custom metaclass to\n\t * handle missing methods.\n\t * @param goo the {@code GroovyObject} to customize\n\t */",
            "\t/**\n\t * 定制所提供的 {@link GroovyObject}。\n\t * <p>例如，可设置自定义元类以处理缺失的方法。\n\t * @param goo 要定制的 {@code GroovyObject}\n\t */",
        ),
    ],
    "GroovyScriptEvaluator.java": [
        (
            "/**\n * Groovy-based implementation of Spring's {@link ScriptEvaluator} strategy interface.\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see GroovyShell#evaluate(String, String)\n */",
            "/**\n * Spring {@link ScriptEvaluator} 策略接口的 Groovy 实现。\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see GroovyShell#evaluate(String, String)\n */",
        ),
        (
            "\t/**\n\t * Construct a new GroovyScriptEvaluator.\n\t */",
            "\t/**\n\t * 构造新的 GroovyScriptEvaluator。\n\t */",
        ),
        (
            "\t/**\n\t * Construct a new GroovyScriptEvaluator.\n\t * @param classLoader the ClassLoader to use as a parent for the {@link GroovyShell}\n\t */",
            "\t/**\n\t * 构造新的 GroovyScriptEvaluator。\n\t * @param classLoader 用作 {@link GroovyShell} 父级的 ClassLoader\n\t */",
        ),
        (
            "\t/**\n\t * Set a custom compiler configuration for this evaluator.\n\t * @since 4.3.3\n\t * @see #setCompilationCustomizers\n\t */",
            "\t/**\n\t * 为本评估器设置自定义编译器配置。\n\t * @since 4.3.3\n\t * @see #setCompilationCustomizers\n\t */",
        ),
        (
            "\t/**\n\t * Return this evaluator's compiler configuration (never {@code null}).\n\t * @since 4.3.3\n\t * @see #setCompilerConfiguration\n\t */",
            "\t/**\n\t * 返回本评估器的编译器配置（永不为 {@code null}）。\n\t * @since 4.3.3\n\t * @see #setCompilerConfiguration\n\t */",
        ),
        (
            "\t/**\n\t * Set one or more customizers to be applied to this evaluator's compiler configuration.\n\t * <p>Note that this modifies the shared compiler configuration held by this evaluator.\n\t * @since 4.3.3\n\t * @see #setCompilerConfiguration\n\t */",
            "\t/**\n\t * 设置一个或多个定制器，应用于本评估器的编译器配置。\n\t * <p>注意，这会修改本评估器持有的共享编译器配置。\n\t * @since 4.3.3\n\t * @see #setCompilerConfiguration\n\t */",
        ),
    ],
    "RefreshableScriptTargetSource.java": [
        (
            "/**\n * Subclass of {@link BeanFactoryRefreshableTargetSource} that determines whether\n * a refresh is required through the given {@link ScriptFactory}.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n */",
            "/**\n * {@link BeanFactoryRefreshableTargetSource} 的子类，\n * 通过给定 {@link ScriptFactory} 判断是否需要刷新。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new RefreshableScriptTargetSource.\n\t * @param beanFactory the BeanFactory to fetch the scripted bean from\n\t * @param beanName the name of the target bean\n\t * @param scriptFactory the ScriptFactory to delegate to for determining\n\t * whether a refresh is required\n\t * @param scriptSource the ScriptSource for the script definition\n\t * @param isFactoryBean whether the target script defines a FactoryBean\n\t */",
            "\t/**\n\t * 创建新的 RefreshableScriptTargetSource。\n\t * @param beanFactory 用于获取脚本 Bean 的 BeanFactory\n\t * @param beanName 目标 Bean 的名称\n\t * @param scriptFactory 用于判断是否需刷新的 ScriptFactory 委托对象\n\t * @param scriptSource 脚本定义的 ScriptSource\n\t * @param isFactoryBean 目标脚本是否定义 FactoryBean\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether a refresh is required through calling\n\t * ScriptFactory's {@code requiresScriptedObjectRefresh} method.\n\t * @see ScriptFactory#requiresScriptedObjectRefresh(ScriptSource)\n\t */",
            "\t/**\n\t * 通过调用 ScriptFactory 的 {@code requiresScriptedObjectRefresh} 方法\n\t * 判断是否需要刷新。\n\t * @see ScriptFactory#requiresScriptedObjectRefresh(ScriptSource)\n\t */",
        ),
        (
            "\t/**\n\t * Obtain a fresh target object, retrieving a FactoryBean if necessary.\n\t */",
            "\t/**\n\t * 获取新的目标对象，必要时检索 FactoryBean。\n\t */",
        ),
    ],
    "StandardScriptEvalException.java": [
        (
            "/**\n * Exception decorating a {@link javax.script.ScriptException} coming out of\n * JSR-223 script evaluation, i.e. a {@link javax.script.ScriptEngine#eval}\n * call or {@link javax.script.Invocable#invokeMethod} /\n * {@link javax.script.Invocable#invokeFunction} call.\n *\n * <p>This exception does not print the Java stacktrace, since the JSR-223\n * {@link ScriptException} results in a rather convoluted text output.\n * From that perspective, this exception is primarily a decorator for a\n * {@link ScriptException} root cause passed into an outer exception.\n *\n * @author Juergen Hoeller\n * @author Sebastien Deleuze\n * @since 4.2.2\n */",
            "/**\n * 装饰 JSR-223 脚本评估产生的 {@link javax.script.ScriptException} 的异常，\n * 即 {@link javax.script.ScriptEngine#eval} 调用或\n * {@link javax.script.Invocable#invokeMethod} /\n * {@link javax.script.Invocable#invokeFunction} 调用。\n *\n * <p>本异常不打印 Java 堆栈跟踪，因为 JSR-223 {@link ScriptException}\n * 会产生相当冗杂的文本输出。从该角度看，本异常主要是传递给外层异常的\n * {@link ScriptException} 根因的装饰器。\n *\n * @author Juergen Hoeller\n * @author Sebastien Deleuze\n * @since 4.2.2\n */",
        ),
        (
            "\t/**\n\t * Construct a new script eval exception with the specified original exception.\n\t */",
            "\t/**\n\t * 使用指定原始异常构造新的脚本评估异常。\n\t */",
        ),
    ],
    "StandardScriptUtils.java": [
        (
            "/**\n * Common operations for dealing with a JSR-223 {@link ScriptEngine}.\n *\n * @author Juergen Hoeller\n * @since 4.2.2\n */",
            "/**\n * 处理 JSR-223 {@link ScriptEngine} 的常用操作。\n *\n * @author Juergen Hoeller\n * @since 4.2.2\n */",
        ),
        (
            "\t/**\n\t * Retrieve a {@link ScriptEngine} from the given {@link ScriptEngineManager}\n\t * by name, delegating to {@link ScriptEngineManager#getEngineByName} but\n\t * throwing a descriptive exception if not found or if initialization failed.\n\t * @param scriptEngineManager the ScriptEngineManager to use\n\t * @param engineName the name of the engine\n\t * @return a corresponding ScriptEngine (never {@code null})\n\t * @throws IllegalArgumentException if no matching engine has been found\n\t * @throws IllegalStateException if the desired engine failed to initialize\n\t */",
            "\t/**\n\t * 按名称从给定 {@link ScriptEngineManager} 检索 {@link ScriptEngine}，\n\t * 委托 {@link ScriptEngineManager#getEngineByName}，\n\t * 若未找到或初始化失败则抛出描述性异常。\n\t * @param scriptEngineManager 要使用的 ScriptEngineManager\n\t * @param engineName 引擎名称\n\t * @return 对应的 ScriptEngine（永不为 {@code null}）\n\t * @throws IllegalArgumentException 若未找到匹配引擎\n\t * @throws IllegalStateException 若所需引擎初始化失败\n\t */",
        ),
    ],
    "StaticScriptSource.java": [
        (
            "/**\n * Static implementation of the\n * {@link org.springframework.scripting.ScriptSource} interface,\n * encapsulating a given String that contains the script source text.\n * Supports programmatic updates of the script String.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * {@link org.springframework.scripting.ScriptSource} 接口的静态实现，\n * 封装包含脚本源文本的给定 String。支持以编程方式更新脚本 String。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new StaticScriptSource for the given script.\n\t * @param script the script String\n\t */",
            "\t/**\n\t * 为给定脚本创建新的 StaticScriptSource。\n\t * @param script 脚本 String\n\t */",
        ),
        (
            "\t/**\n\t * Create a new StaticScriptSource for the given script.\n\t * @param script the script String\n\t * @param className the suggested class name for the script\n\t * (may be {@code null})\n\t */",
            "\t/**\n\t * 为给定脚本创建新的 StaticScriptSource。\n\t * @param script 脚本 String\n\t * @param className 脚本的建议类名（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Set a fresh script String, overriding the previous script.\n\t * @param script the script String\n\t */",
            "\t/**\n\t * 设置新的脚本 String，覆盖先前的脚本。\n\t * @param script 脚本 String\n\t */",
        ),
    ],
    "Controller.java": [
        (
            "/**\n * Indicates that an annotated class is a \"Controller\" (for example, a web controller).\n *\n * <p>This annotation serves as a specialization of {@link Component @Component},\n * allowing for implementation classes to be autodetected through classpath scanning.\n * It is typically used in combination with annotated handler methods based on the\n * {@link org.springframework.web.bind.annotation.RequestMapping} annotation.\n *\n * @author Arjen Poutsma\n * @author Juergen Hoeller\n * @since 2.5\n * @see Component\n * @see org.springframework.web.bind.annotation.RequestMapping\n * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner\n */",
            "/**\n * 表示被注解的类是“Controller”（例如 Web 控制器）。\n *\n * <p>本注解是 {@link Component @Component} 的特化，\n * 允许实现类通过类路径扫描自动检测。\n * 通常与基于 {@link org.springframework.web.bind.annotation.RequestMapping} 注解的\n * 处理器方法配合使用。\n *\n * @author Arjen Poutsma\n * @author Juergen Hoeller\n * @since 2.5\n * @see Component\n * @see org.springframework.web.bind.annotation.RequestMapping\n * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner\n */",
        ),
        (
            "\t/**\n\t * Alias for {@link Component#value}.\n\t */",
            "\t/**\n\t * {@link Component#value} 的别名。\n\t */",
        ),
    ],
    "Repository.java": [
        (
            "/**\n * Indicates that an annotated class is a \"Repository\", originally defined by\n * Domain-Driven Design (Evans, 2003) as \"a mechanism for encapsulating storage,\n * retrieval, and search behavior which emulates a collection of objects\".\n *\n * <p>Teams implementing traditional Jakarta EE patterns such as \"Data Access Object\"\n * may also apply this stereotype to DAO classes, though care should be taken to\n * understand the distinction between Data Access Object and DDD-style repositories\n * before doing so. This annotation is a general-purpose stereotype and individual teams\n * may narrow their semantics and use as appropriate.\n *\n * <p>A class thus annotated is eligible for Spring\n * {@link org.springframework.dao.DataAccessException DataAccessException} translation\n * when used in conjunction with a {@link\n * org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor\n * PersistenceExceptionTranslationPostProcessor}. The annotated class is also clarified as\n * to its role in the overall application architecture for the purpose of tooling,\n * aspects, etc.\n *\n * <p>This annotation also serves as a specialization of {@link Component @Component},\n * allowing for implementation classes to be autodetected through classpath scanning.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see Component\n * @see Service\n * @see org.springframework.dao.DataAccessException\n * @see org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor\n */",
            "/**\n * 表示被注解的类是“Repository”，最初由领域驱动设计（Evans, 2003）定义为\n * “封装存储、检索和搜索行为、模拟对象集合的机制”。\n *\n * <p>实现传统 Jakarta EE 模式（如“Data Access Object”）的团队\n * 也可将此构造型应用于 DAO 类，但在此之前应理解\n * Data Access Object 与 DDD 风格仓储之间的区别。\n * 本注解是通用构造型，各团队可按需收窄语义与用法。\n *\n * <p>如此注解的类在与 {@link\n * org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor\n * PersistenceExceptionTranslationPostProcessor} 配合使用时，\n * 有资格进行 Spring {@link org.springframework.dao.DataAccessException DataAccessException} 转换。\n * 被注解类在整体应用架构中的角色也会为工具、切面等目的而明确。\n *\n * <p>本注解也是 {@link Component @Component} 的特化，\n * 允许实现类通过类路径扫描自动检测。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see Component\n * @see Service\n * @see org.springframework.dao.DataAccessException\n * @see org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor\n */",
        ),
        (
            "\t/**\n\t * Alias for {@link Component#value}.\n\t */",
            "\t/**\n\t * {@link Component#value} 的别名。\n\t */",
        ),
    ],
    "Service.java": [
        (
            "/**\n * Indicates that an annotated class is a \"Service\", originally defined by Domain-Driven\n * Design (Evans, 2003) as \"an operation offered as an interface that stands alone in the\n * model, with no encapsulated state.\"\n *\n * <p>May also indicate that a class is a \"Business Service Facade\" (in the Core J2EE\n * patterns sense), or something similar. This annotation is a general-purpose stereotype\n * and individual teams may narrow their semantics and use as appropriate.\n *\n * <p>This annotation serves as a specialization of {@link Component @Component},\n * allowing for implementation classes to be autodetected through classpath scanning.\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see Component\n * @see Repository\n */",
            "/**\n * 表示被注解的类是“Service”，最初由领域驱动设计（Evans, 2003）定义为\n * “作为接口提供的、在模型中独立存在且无封装状态的操作”。\n *\n * <p>也可表示类是“Business Service Facade”（Core J2EE 模式意义下）或类似角色。\n * 本注解是通用构造型，各团队可按需收窄语义与用法。\n *\n * <p>本注解是 {@link Component @Component} 的特化，\n * 允许实现类通过类路径扫描自动检测。\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see Component\n * @see Repository\n */",
        ),
        (
            "\t/**\n\t * Alias for {@link Component#value}.\n\t */",
            "\t/**\n\t * {@link Component#value} 的别名。\n\t */",
        ),
    ],
    "ExtendedModelMap.java": [
        (
            "/**\n * Subclass of {@link ModelMap} that implements the {@link Model} interface.\n *\n * <p>This is an implementation class exposed to handler methods by Spring MVC, typically via\n * a declaration of the {@link org.springframework.ui.Model} interface. There is no need to\n * build it within user code; a plain {@link org.springframework.ui.ModelMap} or even a just\n * a regular {@link Map} with String keys will be good enough to return a user model.\n *\n * @author Juergen Hoeller\n * @since 2.5.1\n */",
            "/**\n * 实现 {@link Model} 接口的 {@link ModelMap} 子类。\n *\n * <p>这是 Spring MVC 暴露给处理器方法的实现类，通常通过声明\n * {@link org.springframework.ui.Model} 接口提供。\n * 用户代码无需构建它；普通 {@link org.springframework.ui.ModelMap} 或\n * 甚至带 String 键的常规 {@link Map} 就足以返回用户模型。\n *\n * @author Juergen Hoeller\n * @since 2.5.1\n */",
        ),
    ],
}
