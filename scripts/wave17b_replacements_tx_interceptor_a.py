"""Chinese JavaDoc replacements for springframework wave17b interceptor [1:10]."""

TX_INTERCEPTOR_A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "MatchAlwaysTransactionAttributeSource.java": [
        (
            "/**\n * Very simple implementation of TransactionAttributeSource which will always return\n * the same TransactionAttribute for all methods fed to it. The TransactionAttribute\n * may be specified, but will otherwise default to PROPAGATION_REQUIRED. This may be\n * used in the cases where you want to use the same transaction attribute with all\n * methods being handled by a transaction interceptor.\n *\n * @author Colin Sampaleanu\n * @author Juergen Hoeller\n * @since 15.10.2003\n * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean\n * @see org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator\n */",
            "/**\n * {@link TransactionAttributeSource} 的极简实现：对传入的所有方法始终返回\n * 同一个 {@link TransactionAttribute}。可显式指定该属性，否则默认为\n * PROPAGATION_REQUIRED。适用于希望事务拦截器处理的所有方法\n * 使用相同事务属性的场景。\n *\n * @author Colin Sampaleanu\n * @author Juergen Hoeller\n * @since 15.10.2003\n * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean\n * @see org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator\n */",
        ),
        (
            "\t/**\n\t * Allows a transaction attribute to be specified, using the String form, for\n\t * example, \"PROPAGATION_REQUIRED\".\n\t * @param transactionAttribute the String form of the transactionAttribute to use.\n\t * @see org.springframework.transaction.interceptor.TransactionAttributeEditor\n\t */",
            "\t/**\n\t * 允许指定事务属性，可使用字符串形式，例如 \"PROPAGATION_REQUIRED\"。\n\t * @param transactionAttribute 要使用的事务属性（字符串形式）。\n\t * @see org.springframework.transaction.interceptor.TransactionAttributeEditor\n\t */",
        ),
    ],
    "MethodMapTransactionAttributeSource.java": [
        (
            "/**\n * Simple {@link TransactionAttributeSource} implementation that\n * allows attributes to be stored per method in a {@link Map}.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 24.04.2003\n * @see #isMatch\n * @see NameMatchTransactionAttributeSource\n */",
            "/**\n * 简单的 {@link TransactionAttributeSource} 实现，\n * 允许在 {@link Map} 中按方法存储事务属性。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 24.04.2003\n * @see #isMatch\n * @see NameMatchTransactionAttributeSource\n */",
        ),
        (
            "\t/** Logger available to subclasses. */",
            "\t/** 子类可用的日志记录器。 */",
        ),
        (
            "\t/** Map from method name to attribute value. */",
            "\t/** 从方法名到属性值的映射。 */",
        ),
        (
            "\t/** Map from Method to TransactionAttribute. */",
            "\t/** 从 Method 到 TransactionAttribute 的映射。 */",
        ),
        (
            "\t/** Map from Method to name pattern used for registration. */",
            "\t/** 从 Method 到注册时所用名称模式的映射。 */",
        ),
        (
            "\t/**\n\t * Set a name/attribute map, consisting of \"{@code <fully-qualified class name>.<method-name>}\"\n\t * method names (for example, \"com.mycompany.mycode.MyClass.myMethod\") and\n\t * {@link TransactionAttribute} instances (or Strings to be converted\n\t * to {@code TransactionAttribute} instances).\n\t * <p>Intended for configuration via setter injection, typically within\n\t * a Spring bean factory. Relies on {@link #afterPropertiesSet()}\n\t * being called afterwards.\n\t * @param methodMap said {@link Map} from method name to attribute value\n\t * @see TransactionAttribute\n\t * @see TransactionAttributeEditor\n\t */",
            "\t/**\n\t * 设置名称/属性映射，由 \"{@code <全限定类名>.<方法名>}\"\n\t * 形式的方法名（例如 \"com.mycompany.mycode.MyClass.myMethod\"）与\n\t * {@link TransactionAttribute} 实例（或可转换为\n\t * {@code TransactionAttribute} 的字符串）组成。\n\t * <p>用于通过 setter 注入配置，通常在 Spring Bean 工厂中。\n\t * 依赖随后调用 {@link #afterPropertiesSet()}。\n\t * @param methodMap 从方法名到属性值的 {@link Map}\n\t * @see TransactionAttribute\n\t * @see TransactionAttributeEditor\n\t */",
        ),
        (
            "\t/**\n\t * Eagerly initializes the specified\n\t * {@link #setMethodMap(java.util.Map) \"methodMap\"}, if any.\n\t * @see #initMethodMap(java.util.Map)\n\t */",
            "\t/**\n\t * 预先初始化指定的\n\t * {@link #setMethodMap(java.util.Map) \"methodMap\"}（若有）。\n\t * @see #initMethodMap(java.util.Map)\n\t */",
        ),
        (
            "\t/**\n\t * Initialize the specified {@link #setMethodMap(java.util.Map) \"methodMap\"}, if any.\n\t * @param methodMap a Map from method names to {@code TransactionAttribute} instances\n\t * @see #setMethodMap\n\t */",
            "\t/**\n\t * 初始化指定的 {@link #setMethodMap(java.util.Map) \"methodMap\"}（若有）。\n\t * @param methodMap 从方法名到 {@code TransactionAttribute} 实例的 Map\n\t * @see #setMethodMap\n\t */",
        ),
        (
            "\t/**\n\t * Add an attribute for a transactional method.\n\t * <p>Method names can end or start with \"*\" for matching multiple methods.\n\t * @param name class and method name, separated by a dot\n\t * @param attr attribute associated with the method\n\t * @throws IllegalArgumentException in case of an invalid name\n\t */",
            "\t/**\n\t * 为事务方法添加属性。\n\t * <p>方法名可以 \"*\" 开头或结尾以匹配多个方法。\n\t * @param name 类名与方法名，以点分隔\n\t * @param attr 与方法关联的属性\n\t * @throws IllegalArgumentException 名称无效时\n\t */",
        ),
        (
            "\t/**\n\t * Add an attribute for a transactional method.\n\t * Method names can end or start with \"*\" for matching multiple methods.\n\t * @param clazz target interface or class\n\t * @param mappedName mapped method name\n\t * @param attr attribute associated with the method\n\t */",
            "\t/**\n\t * 为事务方法添加属性。\n\t * 方法名可以 \"*\" 开头或结尾以匹配多个方法。\n\t * @param clazz 目标接口或类\n\t * @param mappedName 映射的方法名\n\t * @param attr 与方法关联的属性\n\t */",
        ),
        (
            "\t\t// Register all matching methods",
            "\t\t// 注册所有匹配的方法",
        ),
        (
            "\t\t\t\t// No already registered method name, or more specific\n\t\t\t\t// method name specification now -> (re-)register method.",
            "\t\t\t\t// 尚无已注册方法名，或当前方法名更具体 -> （重新）注册方法。",
        ),
        (
            "\t/**\n\t * Add an attribute for a transactional method.\n\t * @param method the method\n\t * @param attr attribute associated with the method\n\t */",
            "\t/**\n\t * 为事务方法添加属性。\n\t * @param method 方法\n\t * @param attr 与方法关联的属性\n\t */",
        ),
        (
            "\t/**\n\t * Return if the given method name matches the mapped name.\n\t * <p>The default implementation checks for \"xxx*\", \"*xxx\" and \"*xxx*\"\n\t * matches, as well as direct equality.\n\t * @param methodName the method name of the class\n\t * @param mappedName the name in the descriptor\n\t * @return if the names match\n\t * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)\n\t */",
            "\t/**\n\t * 返回给定方法名是否与映射名匹配。\n\t * <p>默认实现检查 \"xxx*\"、\"*xxx\"、\"*xxx*\" 匹配及直接相等。\n\t * @param methodName 类的方法名\n\t * @param mappedName 描述符中的名称\n\t * @return 名称是否匹配\n\t * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)\n\t */",
        ),
    ],
    "MethodRollbackEvent.java": [
        (
            "/**\n * Event published for every exception encountered that triggers a transaction rollback\n * through a proxy-triggered method invocation or a reactive publisher returned from it.\n * Can be listened to via an {@code ApplicationListener<MethodRollbackEvent>} bean or\n * an {@code @EventListener(MethodRollbackEvent.class)} method.\n *\n * <p>Note: This event gets published right <i>before</i> the actual transaction rollback.\n * As a consequence, the exposed {@link #getTransaction() transaction} reflects the state\n * of the transaction right before the rollback.\n *\n * @author Juergen Hoeller\n * @since 7.0.3\n * @see TransactionInterceptor\n * @see org.springframework.transaction.annotation.Transactional\n * @see org.springframework.context.ApplicationListener\n * @see org.springframework.context.event.EventListener\n */",
            "/**\n * 每当通过代理触发的方法调用或其返回的响应式 Publisher 中\n * 遇到触发事务回滚的异常时发布的事件。\n * 可通过 {@code ApplicationListener<MethodRollbackEvent>} Bean 或\n * {@code @EventListener(MethodRollbackEvent.class)} 方法监听。\n *\n * <p>注意：该事件在实际事务回滚<i>之前</i>发布。\n * 因此暴露的 {@link #getTransaction() 事务}反映回滚前的事务状态。\n *\n * @author Juergen Hoeller\n * @since 7.0.3\n * @see TransactionInterceptor\n * @see org.springframework.transaction.annotation.Transactional\n * @see org.springframework.context.ApplicationListener\n * @see org.springframework.context.event.EventListener\n */",
        ),
        (
            "\t/**\n\t * Create a new event for the given rolled-back method invocation.\n\t * @param invocation the transactional method invocation\n\t * @param failure the exception encountered that triggered a rollback\n\t * @param transaction the transaction status right before the rollback\n\t */",
            "\t/**\n\t * 为给定已回滚的方法调用创建新事件。\n\t * @param invocation 事务方法调用\n\t * @param failure 触发回滚的异常\n\t * @param transaction 回滚前的事务状态\n\t */",
        ),
        (
            "\t/**\n\t * Return the exception encountered.\n\t * <p>This may be an exception thrown by the method or emitted by the\n\t * reactive publisher returned from the method.\n\t */",
            "\t/**\n\t * 返回遇到的异常。\n\t * <p>可能是方法抛出的异常，或方法返回的响应式 Publisher 发出的异常。\n\t */",
        ),
        (
            "\t/**\n\t * Return the corresponding transaction status.\n\t */",
            "\t/**\n\t * 返回对应的事务状态。\n\t */",
        ),
    ],
    "NameMatchTransactionAttributeSource.java": [
        (
            "/**\n * Simple {@link TransactionAttributeSource} implementation that\n * allows attributes to be matched by registered name.\n *\n * @author Juergen Hoeller\n * @since 21.08.2003\n * @see #isMatch\n * @see MethodMapTransactionAttributeSource\n */",
            "/**\n * 简单的 {@link TransactionAttributeSource} 实现，\n * 允许按注册名称匹配事务属性。\n *\n * @author Juergen Hoeller\n * @since 21.08.2003\n * @see #isMatch\n * @see MethodMapTransactionAttributeSource\n */",
        ),
        (
            "\t/**\n\t * Logger available to subclasses.\n\t * <p>Static for optimal serialization.\n\t */",
            "\t/**\n\t * 子类可用的日志记录器。\n\t * <p>为优化序列化而设为 static。\n\t */",
        ),
        (
            "\t/** Keys are method names; values are TransactionAttributes. */",
            "\t/** 键为方法名；值为 TransactionAttribute。 */",
        ),
        (
            "\t/**\n\t * Set a name/attribute map, consisting of method names\n\t * (for example, \"myMethod\") and {@link TransactionAttribute} instances.\n\t * @see #setProperties\n\t * @see TransactionAttribute\n\t */",
            "\t/**\n\t * 设置名称/属性映射，由方法名\n\t * （例如 \"myMethod\"）与 {@link TransactionAttribute} 实例组成。\n\t * @see #setProperties\n\t * @see TransactionAttribute\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given properties into a name/attribute map.\n\t * <p>Expects method names as keys and String attributes definitions as values,\n\t * parsable into {@link TransactionAttribute} instances via a\n\t * {@link TransactionAttributeEditor}.\n\t * @see #setNameMap\n\t * @see TransactionAttributeEditor\n\t */",
            "\t/**\n\t * 将给定 Properties 解析为名称/属性映射。\n\t * <p>期望以方法名为键、字符串属性定义为值，\n\t * 可通过 {@link TransactionAttributeEditor} 解析为 {@link TransactionAttribute}。\n\t * @see #setNameMap\n\t * @see TransactionAttributeEditor\n\t */",
        ),
        (
            "\t/**\n\t * Add an attribute for a transactional method.\n\t * <p>Method names can be exact matches, or of the pattern \"xxx*\",\n\t * \"*xxx\", or \"*xxx*\" for matching multiple methods.\n\t * @param methodName the name of the method\n\t * @param attr attribute associated with the method\n\t */",
            "\t/**\n\t * 为事务方法添加属性。\n\t * <p>方法名可为精确匹配，或 \"xxx*\"、\"*xxx\"、\"*xxx*\" 模式以匹配多个方法。\n\t * @param methodName 方法名\n\t * @param attr 与方法关联的属性\n\t */",
        ),
        (
            "\t\t// Look for direct name match.",
            "\t\t// 查找直接名称匹配。",
        ),
        (
            "\t\t\t// Look for most specific name match.",
            "\t\t\t// 查找最具体的名称匹配。",
        ),
        (
            "\t/**\n\t * Determine if the given method name matches the mapped name.\n\t * <p>The default implementation checks for \"xxx*\", \"*xxx\", and \"*xxx*\" matches,\n\t * as well as direct equality. Can be overridden in subclasses.\n\t * @param methodName the method name of the class\n\t * @param mappedName the name in the descriptor\n\t * @return {@code true} if the names match\n\t * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)\n\t */",
            "\t/**\n\t * 判断给定方法名是否与映射名匹配。\n\t * <p>默认实现检查 \"xxx*\"、\"*xxx\"、\"*xxx*\" 匹配及直接相等。\n\t * 子类可覆盖。\n\t * @param methodName 类的方法名\n\t * @param mappedName 描述符中的名称\n\t * @return 名称匹配时为 {@code true}\n\t * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)\n\t */",
        ),
    ],
    "NoRollbackRuleAttribute.java": [
        (
            "/**\n * Tag subclass of {@link RollbackRuleAttribute} that has the opposite behavior\n * to the {@code RollbackRuleAttribute} superclass.\n *\n * @author Rod Johnson\n * @author Sam Brannen\n * @since 09.04.2003\n */",
            "/**\n * {@link RollbackRuleAttribute} 的标记子类，行为与\n * {@code RollbackRuleAttribute} 超类相反。\n *\n * @author Rod Johnson\n * @author Sam Brannen\n * @since 09.04.2003\n */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@code NoRollbackRuleAttribute} class\n\t * for the given {@code exceptionType}.\n\t * @param exceptionType exception type; must be {@link Throwable} or a subclass\n\t * of {@code Throwable}\n\t * @throws IllegalArgumentException if the supplied {@code exceptionType} is\n\t * not a {@code Throwable} type or is {@code null}\n\t * @see RollbackRuleAttribute#RollbackRuleAttribute(Class)\n\t */",
            "\t/**\n\t * 为给定 {@code exceptionType} 创建新的 {@code NoRollbackRuleAttribute} 实例。\n\t * @param exceptionType 异常类型；必须是 {@link Throwable} 或其子类\n\t * @throws IllegalArgumentException 若 {@code exceptionType} 不是 {@code Throwable} 类型或为 {@code null}\n\t * @see RollbackRuleAttribute#RollbackRuleAttribute(Class)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@code NoRollbackRuleAttribute} class\n\t * for the supplied {@code exceptionPattern}.\n\t * @param exceptionPattern the exception name pattern; can also be a fully\n\t * package-qualified class name\n\t * @throws IllegalArgumentException if the supplied {@code exceptionPattern}\n\t * is {@code null} or empty\n\t * @see RollbackRuleAttribute#RollbackRuleAttribute(String)\n\t */",
            "\t/**\n\t * 为给定 {@code exceptionPattern} 创建新的 {@code NoRollbackRuleAttribute} 实例。\n\t * @param exceptionPattern 异常名称模式；也可为全限定类名\n\t * @throws IllegalArgumentException 若 {@code exceptionPattern} 为 {@code null} 或空\n\t * @see RollbackRuleAttribute#RollbackRuleAttribute(String)\n\t */",
        ),
    ],
    "RollbackRuleAttribute.java": [
        (
            "/**\n * Rule determining whether a given exception should cause a rollback.\n *\n * <p>Multiple such rules can be applied to determine whether a transaction\n * should commit or rollback after an exception has been thrown.\n *\n * <p>Each rule is based on an exception type or exception pattern, supplied via\n * {@link #RollbackRuleAttribute(Class)} or {@link #RollbackRuleAttribute(String)},\n * respectively.\n *\n * <p>When a rollback rule is defined with an exception type, that type will be\n * used to match against the type of a thrown exception and its super types,\n * providing type safety and avoiding any unintentional matches that may occur\n * when using a pattern. For example, a value of\n * {@code jakarta.servlet.ServletException.class} will only match thrown exceptions\n * of type {@code jakarta.servlet.ServletException} and its subclasses.\n *\n * <p>When a rollback rule is defined with an exception pattern, the pattern can\n * be a fully qualified class name or a substring of a fully qualified class name\n * for an exception type (which must be a subclass of {@code Throwable}), with no\n * wildcard support at present. For example, a value of\n * {@code \"jakarta.servlet.ServletException\"} or {@code \"ServletException\"} will\n * match {@code jakarta.servlet.ServletException} and its subclasses.\n *\n * <p>See the javadocs for\n * {@link org.springframework.transaction.annotation.Transactional @Transactional}\n * for further details on rollback rule semantics, patterns, and warnings regarding\n * possible unintentional matches with pattern-based rules.\n *\n * @author Rod Johnson\n * @author Sam Brannen\n * @since 09.04.2003\n * @see NoRollbackRuleAttribute\n */",
            "/**\n * 判定给定异常是否应导致回滚的规则。\n *\n * <p>可应用多条此类规则，以在抛出异常后决定事务应提交还是回滚。\n *\n * <p>每条规则基于异常类型或异常模式，分别通过\n * {@link #RollbackRuleAttribute(Class)} 或 {@link #RollbackRuleAttribute(String)} 提供。\n *\n * <p>以异常类型定义回滚规则时，该类型用于匹配抛出异常的类型及其超类型，\n * 提供类型安全并避免使用模式时可能出现的意外匹配。\n * 例如 {@code jakarta.servlet.ServletException.class} 仅匹配\n * {@code jakarta.servlet.ServletException} 及其子类的抛出异常。\n *\n * <p>以异常模式定义回滚规则时，模式可为全限定类名或全限定类名的子串\n * （异常类型必须是 {@code Throwable} 的子类），目前不支持通配符。\n * 例如 {@code \"jakarta.servlet.ServletException\"} 或 {@code \"ServletException\"}\n * 将匹配 {@code jakarta.servlet.ServletException} 及其子类。\n *\n * <p>有关回滚规则语义、模式及基于模式规则可能意外匹配的警告，\n * 请参阅 {@link org.springframework.transaction.annotation.Transactional @Transactional} 的 JavaDoc。\n *\n * @author Rod Johnson\n * @author Sam Brannen\n * @since 09.04.2003\n * @see NoRollbackRuleAttribute\n */",
        ),
        (
            "\t/**\n\t * The {@linkplain RollbackRuleAttribute rollback rule} for\n\t * {@link RuntimeException RuntimeExceptions}.\n\t */",
            "\t/**\n\t * 针对 {@link RuntimeException 运行时异常} 的\n\t * {@linkplain RollbackRuleAttribute 回滚规则}。\n\t */",
        ),
        (
            "\t/**\n\t * The {@linkplain RollbackRuleAttribute rollback rule} for all\n\t * {@link Exception Exceptions}, including checked exceptions.\n\t * @since 6.2\n\t */",
            "\t/**\n\t * 针对所有 {@link Exception 异常}（含受检异常）的\n\t * {@linkplain RollbackRuleAttribute 回滚规则}。\n\t * @since 6.2\n\t */",
        ),
        (
            "\t/**\n\t * Exception pattern: used when searching for matches in a thrown exception's\n\t * class hierarchy based on names of exceptions, with zero type safety and\n\t * potentially resulting in unintentional matches for similarly named exception\n\t * types and nested exception types.\n\t */",
            "\t/**\n\t * 异常模式：基于异常名称在抛出异常的类层次中搜索匹配时使用，\n\t * 无类型安全，可能对名称相似的异常类型和嵌套异常类型产生意外匹配。\n\t */",
        ),
        (
            "\t/**\n\t * Exception type: used to ensure type safety when searching for matches in\n\t * a thrown exception's class hierarchy.\n\t * @since 6.0\n\t */",
            "\t/**\n\t * 异常类型：在抛出异常的类层次中搜索匹配时用于保证类型安全。\n\t * @since 6.0\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@code RollbackRuleAttribute} class\n\t * for the given {@code exceptionType}.\n\t * <p>This is the preferred way to construct a rollback rule that matches\n\t * the supplied exception type and its subclasses with type safety.\n\t * <p>See the javadocs for\n\t * {@link org.springframework.transaction.annotation.Transactional @Transactional}\n\t * for further details on rollback rule semantics.\n\t * @param exceptionType exception type; must be {@link Throwable} or a subclass\n\t * of {@code Throwable}\n\t * @throws IllegalArgumentException if the supplied {@code exceptionType} is\n\t * not a {@code Throwable} type or is {@code null}\n\t */",
            "\t/**\n\t * 为给定 {@code exceptionType} 创建新的 {@code RollbackRuleAttribute} 实例。\n\t * <p>这是构造以类型安全方式匹配所供异常类型及其子类的回滚规则的首选方式。\n\t * <p>有关回滚规则语义的更多细节，请参阅\n\t * {@link org.springframework.transaction.annotation.Transactional @Transactional} 的 JavaDoc。\n\t * @param exceptionType 异常类型；必须是 {@link Throwable} 或其子类\n\t * @throws IllegalArgumentException 若 {@code exceptionType} 不是 {@code Throwable} 类型或为 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@code RollbackRuleAttribute} class\n\t * for the given {@code exceptionPattern}.\n\t * <p>See the javadocs for\n\t * {@link org.springframework.transaction.annotation.Transactional @Transactional}\n\t * for further details on rollback rule semantics, patterns, and warnings regarding\n\t * possible unintentional matches.\n\t * <p>For improved type safety and to avoid unintentional matches, use\n\t * {@link #RollbackRuleAttribute(Class)} instead.\n\t * @param exceptionPattern the exception name pattern; can also be a fully\n\t * package-qualified class name\n\t * @throws IllegalArgumentException if the supplied {@code exceptionPattern}\n\t * is {@code null} or empty\n\t */",
            "\t/**\n\t * 为给定 {@code exceptionPattern} 创建新的 {@code RollbackRuleAttribute} 实例。\n\t * <p>有关回滚规则语义、模式及可能意外匹配的警告，请参阅\n\t * {@link org.springframework.transaction.annotation.Transactional @Transactional} 的 JavaDoc。\n\t * <p>为提升类型安全并避免意外匹配，请改用 {@link #RollbackRuleAttribute(Class)}。\n\t * @param exceptionPattern 异常名称模式；也可为全限定类名\n\t * @throws IllegalArgumentException 若 {@code exceptionPattern} 为 {@code null} 或空\n\t */",
        ),
        (
            "\t/**\n\t * Get the configured exception name pattern that this rule uses for matching.\n\t * @see #getDepth(Throwable)\n\t */",
            "\t/**\n\t * 获取本规则用于匹配的已配置异常名称模式。\n\t * @see #getDepth(Throwable)\n\t */",
        ),
        (
            "\t/**\n\t * Return the depth of the superclass matching, with the following semantics.\n\t * <ul>\n\t * <li>{@code -1} means this rule does not match the supplied {@code exception}.</li>\n\t * <li>{@code 0} means this rule matches the supplied {@code exception} directly.</li>\n\t * <li>Any other positive value means this rule matches the supplied {@code exception}\n\t * within the superclass hierarchy, where the value is the number of levels in the\n\t * class hierarchy between the supplied {@code exception} and the exception against\n\t * which this rule matches directly.</li>\n\t * </ul>\n\t * <p>When comparing roll back rules that match against a given exception, a rule\n\t * with a lower matching depth wins. For example, a direct match ({@code depth == 0})\n\t * wins over a match in the superclass hierarchy ({@code depth > 0}).\n\t * <p>When constructed with an exception pattern via {@link #RollbackRuleAttribute(String)},\n\t * a match against a nested exception type or similarly named exception type\n\t * will return a depth signifying a match at the corresponding level in the\n\t * class hierarchy as if there had been a direct match.\n\t */",
            "\t/**\n\t * 返回超类匹配深度，语义如下。\n\t * <ul>\n\t * <li>{@code -1} 表示本规则不匹配所供 {@code exception}。</li>\n\t * <li>{@code 0} 表示本规则直接匹配所供 {@code exception}。</li>\n\t * <li>其他正数表示本规则在超类层次中匹配所供 {@code exception}，\n\t * 该值为所供 {@code exception} 与本规则直接匹配的异常之间类层次的层数。</li>\n\t * </ul>\n\t * <p>比较针对同一异常匹配的回滚规则时，匹配深度较小的规则胜出。\n\t * 例如直接匹配（{@code depth == 0}）优于超类层次中的匹配（{@code depth > 0}）。\n\t * <p>通过 {@link #RollbackRuleAttribute(String)} 以异常模式构造时，\n\t * 对嵌套异常类型或名称相似的异常类型的匹配将返回\n\t * 类层次中对应层级的深度，如同直接匹配一样。\n\t */",
        ),
        (
            "\t\t\t\t// Found it!",
            "\t\t\t\t// 找到了！",
        ),
        (
            "\t\t\t// Found it!",
            "\t\t\t// 找到了！",
        ),
        (
            "\t\t// If we've gone as far as we can go and haven't found it...",
            "\t\t// 若已到达可搜索的尽头仍未找到...",
        ),
    ],
    "RuleBasedTransactionAttribute.java": [
        (
            "/**\n * TransactionAttribute implementation that works out whether a given exception\n * should cause transaction rollback by applying a number of rollback rules,\n * both positive and negative. If no custom rollback rules apply, this attribute\n * behaves like DefaultTransactionAttribute (rolling back on runtime exceptions).\n *\n * <p>{@link TransactionAttributeEditor} creates objects of this class.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 09.04.2003\n * @see TransactionAttributeEditor\n */",
            "/**\n * 通过应用若干正负回滚规则判定给定异常是否应导致事务回滚的\n * {@link TransactionAttribute} 实现。若无自定义回滚规则适用，\n * 本属性行为类似 DefaultTransactionAttribute（对运行时异常回滚）。\n *\n * <p>{@link TransactionAttributeEditor} 创建本类的对象。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 09.04.2003\n * @see TransactionAttributeEditor\n */",
        ),
        (
            "\t/** Prefix for rollback-on-exception rules in description strings. */",
            "\t/** 描述字符串中“遇异常回滚”规则的前缀。 */",
        ),
        (
            "\t/** Prefix for commit-on-exception rules in description strings. */",
            "\t/** 描述字符串中“遇异常提交”规则的前缀。 */",
        ),
        (
            "\t/**\n\t * Create a new RuleBasedTransactionAttribute, with default settings.\n\t * Can be modified through bean property setters.\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t * @see #setRollbackRules\n\t */",
            "\t/**\n\t * 以默认设置创建新的 RuleBasedTransactionAttribute。\n\t * 可通过 Bean 属性 setter 修改。\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t * @see #setRollbackRules\n\t */",
        ),
        (
            "\t/**\n\t * Copy constructor. Definition can be modified through bean property setters.\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t * @see #setRollbackRules\n\t */",
            "\t/**\n\t * 拷贝构造函数。定义可通过 Bean 属性 setter 修改。\n\t * @see #setPropagationBehavior\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t * @see #setName\n\t * @see #setRollbackRules\n\t */",
        ),
        (
            "\t/**\n\t * Create a new DefaultTransactionAttribute with the given\n\t * propagation behavior. Can be modified through bean property setters.\n\t * @param propagationBehavior one of the propagation constants in the\n\t * TransactionDefinition interface\n\t * @param rollbackRules the list of RollbackRuleAttributes to apply\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t */",
            "\t/**\n\t * 以给定传播行为创建新的 DefaultTransactionAttribute。\n\t * 可通过 Bean 属性 setter 修改。\n\t * @param propagationBehavior TransactionDefinition 接口中的传播常量之一\n\t * @param rollbackRules 要应用的 RollbackRuleAttribute 列表\n\t * @see #setIsolationLevel\n\t * @see #setTimeout\n\t * @see #setReadOnly\n\t */",
        ),
        (
            "\t/**\n\t * Set the list of {@code RollbackRuleAttribute} objects\n\t * (and/or {@code NoRollbackRuleAttribute} objects) to apply.\n\t * @see RollbackRuleAttribute\n\t * @see NoRollbackRuleAttribute\n\t */",
            "\t/**\n\t * 设置要应用的 {@code RollbackRuleAttribute} 对象列表\n\t * （及/或 {@code NoRollbackRuleAttribute} 对象）。\n\t * @see RollbackRuleAttribute\n\t * @see NoRollbackRuleAttribute\n\t */",
        ),
        (
            "\t/**\n\t * Return the list of {@code RollbackRuleAttribute} objects\n\t * (never {@code null}).\n\t */",
            "\t/**\n\t * 返回 {@code RollbackRuleAttribute} 对象列表（永不为 {@code null}）。\n\t */",
        ),
        (
            "\t/**\n\t * Winning rule is the shallowest rule (that is, the closest in the\n\t * inheritance hierarchy to the exception). If no rule applies (-1),\n\t * return {@code false}.\n\t * @see TransactionAttribute#rollbackOn(java.lang.Throwable)\n\t */",
            "\t/**\n\t * 胜出的规则为最浅规则（即继承层次中最接近异常的规则）。\n\t * 若无规则适用（-1），返回 {@code false}。\n\t * @see TransactionAttribute#rollbackOn(java.lang.Throwable)\n\t */",
        ),
        (
            "\t\t// User superclass behavior (rollback on unchecked) if no rule matches.",
            "\t\t// 若无规则匹配，使用超类行为（对 unchecked 异常回滚）。",
        ),
    ],
    "TransactionAttribute.java": [
        (
            "/**\n * This interface adds a {@code rollbackOn} specification to {@link TransactionDefinition}.\n * As custom {@code rollbackOn} is only possible with AOP, it resides in the AOP-related\n * transaction subpackage.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Mark Paluch\n * @since 16.03.2003\n * @see DefaultTransactionAttribute\n * @see RuleBasedTransactionAttribute\n */",
            "/**\n * 本接口向 {@link TransactionDefinition} 添加 {@code rollbackOn} 规范。\n * 自定义 {@code rollbackOn} 仅能通过 AOP 实现，故位于 AOP 相关事务子包中。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Mark Paluch\n * @since 16.03.2003\n * @see DefaultTransactionAttribute\n * @see RuleBasedTransactionAttribute\n */",
        ),
        (
            "\t/**\n\t * Return a qualifier value associated with this transaction attribute.\n\t * <p>This may be used for choosing a corresponding transaction manager\n\t * to process this specific transaction.\n\t * @since 3.0\n\t */",
            "\t/**\n\t * 返回与本事务属性关联的限定符值。\n\t * <p>可用于选择相应的事务管理器处理该特定事务。\n\t * @since 3.0\n\t */",
        ),
        (
            "\t/**\n\t * Return labels associated with this transaction attribute.\n\t * <p>This may be used for applying specific transactional behavior\n\t * or follow a purely descriptive nature.\n\t * @since 5.3\n\t */",
            "\t/**\n\t * 返回与本事务属性关联的标签。\n\t * <p>可用于应用特定事务行为，或仅作描述性用途。\n\t * @since 5.3\n\t */",
        ),
        (
            "\t/**\n\t * Should we roll back on the given exception?\n\t * @param ex the exception to evaluate\n\t * @return whether to perform a rollback or not\n\t */",
            "\t/**\n\t * 遇到给定异常是否应回滚？\n\t * @param ex 要评估的异常\n\t * @return 是否执行回滚\n\t */",
        ),
    ],
    "TransactionAttributeEditor.java": [
        (
            "/**\n * PropertyEditor for {@link TransactionAttribute} objects. Accepts a String of form\n * <p>{@code PROPAGATION_NAME, ISOLATION_NAME, readOnly, timeout_NNNN,+Exception1,-Exception2}\n * <p>where only propagation code is required. For example:\n * <p>{@code PROPAGATION_MANDATORY, ISOLATION_DEFAULT}\n *\n * <p>The tokens can be in <strong>any</strong> order. Propagation and isolation codes\n * must use the names of the constants in the TransactionDefinition class. Timeout values\n * are in seconds. If no timeout is specified, the transaction manager will apply a default\n * timeout specific to the particular transaction manager.\n *\n * <p>A \"+\" before an exception name substring indicates that transactions should commit\n * even if this exception is thrown; a \"-\" that they should roll back.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 24.04.2003\n * @see org.springframework.transaction.TransactionDefinition\n */",
            "/**\n * {@link TransactionAttribute} 对象的 PropertyEditor。接受如下形式的字符串：\n * <p>{@code PROPAGATION_NAME, ISOLATION_NAME, readOnly, timeout_NNNN,+Exception1,-Exception2}\n * <p>其中仅传播代码为必填。例如：\n * <p>{@code PROPAGATION_MANDATORY, ISOLATION_DEFAULT}\n *\n * <p>标记可以<strong>任意</strong>顺序排列。传播与隔离代码必须使用\n * TransactionDefinition 类中常量的名称。超时值为秒。\n * 若未指定超时，事务管理器将应用其特定的默认超时。\n *\n * <p>异常名称子串前的 \"+\" 表示即使抛出该异常也应提交事务；\"-\" 表示应回滚。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 24.04.2003\n * @see org.springframework.transaction.TransactionDefinition\n */",
        ),
        (
            "\t/**\n\t * Format is PROPAGATION_NAME,ISOLATION_NAME,readOnly,timeout_NNNN,+Exception1,-Exception2.\n\t * Null or the empty string means that the method is non-transactional.\n\t */",
            "\t/**\n\t * 格式为 PROPAGATION_NAME,ISOLATION_NAME,readOnly,timeout_NNNN,+Exception1,-Exception2。\n\t * null 或空字符串表示方法非事务性。\n\t */",
        ),
        (
            "\t\t\t// tokenize it with \",\"",
            "\t\t\t// 以 \",\" 分词",
        ),
        (
            "\t\t\t\t// Trim leading and trailing whitespace.",
            "\t\t\t\t// 去除首尾空白。",
        ),
        (
            "\t\t\t\t// Check whether token contains illegal whitespace within text.",
            "\t\t\t\t// 检查标记内部是否含非法空白。",
        ),
        (
            "\t\t\t\t// Check token type.",
            "\t\t\t\t// 检查标记类型。",
        ),
        (
            "\t\t\tattr.resolveAttributeStrings(null);  // placeholders expected to be pre-resolved",
            "\t\t\tattr.resolveAttributeStrings(null);  // 占位符预期已预先解析",
        ),
    ],
}
