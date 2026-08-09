"""Chinese JavaDoc replacements for springframework wave17b interceptor [11:16]."""

TX_INTERCEPTOR_B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "TransactionAttributeSource.java": [
        (
            "/**\n * Strategy interface used by {@link TransactionInterceptor} for metadata retrieval.\n *\n * <p>Implementations know how to source transaction attributes, whether from configuration,\n * metadata attributes at source level (such as annotations), or anywhere else.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 15.04.2003\n * @see TransactionInterceptor#setTransactionAttributeSource\n * @see TransactionProxyFactoryBean#setTransactionAttributeSource\n * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource\n */",
            "/**\n * {@link TransactionInterceptor} 用于获取元数据的策略接口。\n *\n * <p>实现类知道如何获取事务属性，无论来自配置、\n * 源码级元数据属性（如注解）或其他来源。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 15.04.2003\n * @see TransactionInterceptor#setTransactionAttributeSource\n * @see TransactionProxyFactoryBean#setTransactionAttributeSource\n * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource\n */",
        ),
        (
            "\t/**\n\t * Determine whether the given class is a candidate for transaction attributes\n\t * in the metadata format of this {@code TransactionAttributeSource}.\n\t * <p>If this method returns {@code false}, the methods on the given class\n\t * will not get traversed for {@link #getTransactionAttribute} introspection.\n\t * Returning {@code false} is therefore an optimization for non-affected\n\t * classes, whereas {@code true} simply means that the class needs to get\n\t * fully introspected for each method on the given class individually.\n\t * @param targetClass the class to introspect\n\t * @return {@code false} if the class is known to have no transaction\n\t * attributes at class or method level; {@code true} otherwise. The default\n\t * implementation returns {@code true}, leading to regular introspection.\n\t * @since 5.2\n\t * @see #hasTransactionAttribute\n\t */",
            "\t/**\n\t * 判断给定类是否为本 {@code TransactionAttributeSource} 元数据格式下\n\t * 事务属性的候选类。\n\t * <p>若返回 {@code false}，给定类上的方法将不会为\n\t * {@link #getTransactionAttribute} 内省而遍历。\n\t * 因此对不受影响类返回 {@code false} 是一种优化，\n\t * 而 {@code true} 仅表示需要对该类每个方法逐一完整内省。\n\t * @param targetClass 要内省的类\n\t * @return 若已知类在类或方法级别无事务属性则为 {@code false}，否则为 {@code true}。\n\t * 默认实现返回 {@code true}，进行常规内省。\n\t * @since 5.2\n\t * @see #hasTransactionAttribute\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether there is a transaction attribute for the given method.\n\t * @param method the method to introspect\n\t * @param targetClass the target class (can be {@code null},\n\t * in which case the declaring class of the method must be used)\n\t * @since 6.2\n\t * @see #getTransactionAttribute\n\t */",
            "\t/**\n\t * 判断给定方法是否存在事务属性。\n\t * @param method 要内省的方法\n\t * @param targetClass 目标类（可为 {@code null}，此时须使用方法声明类）\n\t * @since 6.2\n\t * @see #getTransactionAttribute\n\t */",
        ),
        (
            "\t/**\n\t * Return the transaction attribute for the given method,\n\t * or {@code null} if the method is non-transactional.\n\t * @param method the method to introspect\n\t * @param targetClass the target class (can be {@code null},\n\t * in which case the declaring class of the method must be used)\n\t * @return the matching transaction attribute, or {@code null} if none found\n\t */",
            "\t/**\n\t * 返回给定方法的事务属性，若方法非事务性则为 {@code null}。\n\t * @param method 要内省的方法\n\t * @param targetClass 目标类（可为 {@code null}，此时须使用方法声明类）\n\t * @return 匹配的事务属性，未找到则为 {@code null}\n\t */",
        ),
    ],
    "TransactionAttributeSourceAdvisor.java": [
        (
            "/**\n * Advisor driven by a {@link TransactionAttributeSource}, used to include\n * a {@link TransactionInterceptor} only for methods that are transactional.\n *\n * <p>Because the AOP framework caches advice calculations, this is normally\n * faster than just letting the TransactionInterceptor run and find out\n * itself that it has no work to do.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #setTransactionInterceptor\n * @see TransactionProxyFactoryBean\n */",
            "/**\n * 由 {@link TransactionAttributeSource} 驱动的 Advisor，\n * 仅对事务性方法包含 {@link TransactionInterceptor}。\n *\n * <p>由于 AOP 框架缓存 advice 计算结果，\n * 这通常比让 TransactionInterceptor 自行运行并发现无事可做更快。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @see #setTransactionInterceptor\n * @see TransactionProxyFactoryBean\n */",
        ),
        (
            "\t/**\n\t * Create a new TransactionAttributeSourceAdvisor.\n\t */",
            "\t/**\n\t * 创建新的 TransactionAttributeSourceAdvisor。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new TransactionAttributeSourceAdvisor.\n\t * @param interceptor the transaction interceptor to use for this advisor\n\t */",
            "\t/**\n\t * 创建新的 TransactionAttributeSourceAdvisor。\n\t * @param interceptor 本 Advisor 使用的事务拦截器\n\t */",
        ),
        (
            "\t/**\n\t * Set the transaction interceptor to use for this advisor.\n\t */",
            "\t/**\n\t * 设置本 Advisor 使用的事务拦截器。\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link ClassFilter} to use for this pointcut.\n\t * Default is {@link ClassFilter#TRUE}.\n\t */",
            "\t/**\n\t * 设置本切点使用的 {@link ClassFilter}。\n\t * 默认为 {@link ClassFilter#TRUE}。\n\t */",
        ),
    ],
    "TransactionAttributeSourceEditor.java": [
        (
            "/**\n * Property editor that converts a String into a {@link TransactionAttributeSource}.\n * The transaction attribute string must be parseable by the\n * {@link TransactionAttributeEditor} in this package.\n *\n * <p>Strings are in property syntax, with the form:<br>\n * {@code <fully-qualified class name>.<method-name>=<transaction attribute string>}\n *\n * <p>For example:<br>\n * {@code com.mycompany.mycode.MyClass.myMethod=PROPAGATION_MANDATORY,ISOLATION_DEFAULT}\n *\n * <p><b>NOTE:</b> The specified class must be the one where the methods are\n * defined; in case of implementing an interface, the interface class name.\n *\n * <p>Note: Will register all overloaded methods for a given name.\n * Does not support explicit registration of certain overloaded methods.\n * Supports \"xxx*\" mappings &mdash; for example, \"notify*\" will match against\n * \"notify\" and \"notifyAll\".\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 26.04.2003\n * @see TransactionAttributeEditor\n */",
            "/**\n * 将 String 转换为 {@link TransactionAttributeSource} 的属性编辑器。\n * 事务属性字符串须可由本包中的 {@link TransactionAttributeEditor} 解析。\n *\n * <p>字符串为属性语法，形式为：<br>\n * {@code <全限定类名>.<方法名>=<事务属性字符串>}\n *\n * <p>例如：<br>\n * {@code com.mycompany.mycode.MyClass.myMethod=PROPAGATION_MANDATORY,ISOLATION_DEFAULT}\n *\n * <p><b>注意：</b>指定类必须是定义方法的类；\n * 若实现接口，则为接口类名。\n *\n * <p>注意：将为给定名称注册所有重载方法。\n * 不支持显式注册特定重载方法。\n * 支持 \"xxx*\" 映射 &mdash; 例如 \"notify*\" 将匹配 \"notify\" 和 \"notifyAll\"。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 26.04.2003\n * @see TransactionAttributeEditor\n */",
        ),
        (
            "\t\t\t// Use properties editor to tokenize the hold string.",
            "\t\t\t// 使用 Properties 编辑器对整串进行分词。",
        ),
        (
            "\t\t\t// Now we have properties, process each one individually.",
            "\t\t\t// 得到 Properties 后逐个处理。",
        ),
        (
            "\t\t\t\t// Convert value to a transaction attribute.",
            "\t\t\t\t// 将值转换为事务属性。",
        ),
        (
            "\t\t\t\t// Register name and attribute.",
            "\t\t\t\t// 注册名称与属性。",
        ),
    ],
    "TransactionAttributeSourcePointcut.java": [
        (
            "/**\n * Internal class that implements a {@code Pointcut} that matches if the underlying\n * {@link TransactionAttributeSource} has an attribute for a given method.\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.5.5\n */",
            "/**\n * 内部类，实现 {@code Pointcut}：当底层 {@link TransactionAttributeSource}\n * 对给定方法具有属性时匹配。\n *\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 2.5.5\n */",
        ),
        (
            "\t/**\n\t * {@link ClassFilter} that delegates to {@link TransactionAttributeSource#isCandidateClass}\n\t * for filtering classes whose methods are not worth searching to begin with.\n\t */",
            "\t/**\n\t * 委托 {@link TransactionAttributeSource#isCandidateClass} 的 {@link ClassFilter}，\n\t * 用于过滤其方法从一开始就不值得搜索的类。\n\t */",
        ),
    ],
    "TransactionInterceptor.java": [
        (
            "/**\n * AOP Alliance MethodInterceptor for declarative transaction\n * management using the common Spring transaction infrastructure\n * ({@link org.springframework.transaction.PlatformTransactionManager}/\n * {@link org.springframework.transaction.ReactiveTransactionManager}).\n *\n * <p>Derives from the {@link TransactionAspectSupport} class which\n * contains the integration with Spring's underlying transaction API.\n * TransactionInterceptor simply calls the relevant superclass methods\n * such as {@link #invokeWithinTransaction} in the correct order.\n *\n * <p>TransactionInterceptors are thread-safe.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Sebastien Deleuze\n * @see TransactionProxyFactoryBean\n * @see org.springframework.aop.framework.ProxyFactoryBean\n * @see org.springframework.aop.framework.ProxyFactory\n */",
            "/**\n * 使用 Spring 通用事务基础设施\n * （{@link org.springframework.transaction.PlatformTransactionManager}/\n * {@link org.springframework.transaction.ReactiveTransactionManager}）\n * 进行声明式事务管理的 AOP Alliance MethodInterceptor。\n *\n * <p>派生自 {@link TransactionAspectSupport}，其中包含与 Spring 底层事务 API 的集成。\n * TransactionInterceptor 仅按正确顺序调用 {@link #invokeWithinTransaction} 等超类方法。\n *\n * <p>TransactionInterceptor 是线程安全的。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Sebastien Deleuze\n * @see TransactionProxyFactoryBean\n * @see org.springframework.aop.framework.ProxyFactoryBean\n * @see org.springframework.aop.framework.ProxyFactory\n */",
        ),
        (
            "\t/**\n\t * Create a new TransactionInterceptor.\n\t * <p>Transaction manager and transaction attributes still need to be set.\n\t * @see #setTransactionManager\n\t * @see #setTransactionAttributes(java.util.Properties)\n\t * @see #setTransactionAttributeSource(TransactionAttributeSource)\n\t */",
            "\t/**\n\t * 创建新的 TransactionInterceptor。\n\t * <p>仍需设置事务管理器与事务属性。\n\t * @see #setTransactionManager\n\t * @see #setTransactionAttributes(java.util.Properties)\n\t * @see #setTransactionAttributeSource(TransactionAttributeSource)\n\t */",
        ),
        (
            "\t/**\n\t * Create a new TransactionInterceptor.\n\t * @param ptm the default transaction manager to perform the actual transaction management\n\t * @param tas the attribute source to be used to find transaction attributes\n\t * @since 5.2.5\n\t * @see #setTransactionManager\n\t * @see #setTransactionAttributeSource\n\t */",
            "\t/**\n\t * 创建新的 TransactionInterceptor。\n\t * @param ptm 执行实际事务管理的默认事务管理器\n\t * @param tas 用于查找事务属性的属性源\n\t * @since 5.2.5\n\t * @see #setTransactionManager\n\t * @see #setTransactionAttributeSource\n\t */",
        ),
        (
            "\t/**\n\t * Create a new TransactionInterceptor.\n\t * @param ptm the default transaction manager to perform the actual transaction management\n\t * @param tas the attribute source to be used to find transaction attributes\n\t * @see #setTransactionManager\n\t * @see #setTransactionAttributeSource\n\t * @deprecated in favor of\n\t * {@link #TransactionInterceptor(TransactionManager, TransactionAttributeSource)}\n\t */",
            "\t/**\n\t * 创建新的 TransactionInterceptor。\n\t * @param ptm 执行实际事务管理的默认事务管理器\n\t * @param tas 用于查找事务属性的属性源\n\t * @see #setTransactionManager\n\t * @see #setTransactionAttributeSource\n\t * @deprecated 请改用\n\t * {@link #TransactionInterceptor(TransactionManager, TransactionAttributeSource)}\n\t */",
        ),
        (
            "\t/**\n\t * Create a new TransactionInterceptor.\n\t * @param ptm the default transaction manager to perform the actual transaction management\n\t * @param attributes the transaction attributes in properties format\n\t * @see #setTransactionManager\n\t * @see #setTransactionAttributes(java.util.Properties)\n\t * @deprecated in favor of {@link #setTransactionAttributes(Properties)}\n\t */",
            "\t/**\n\t * 创建新的 TransactionInterceptor。\n\t * @param ptm 执行实际事务管理的默认事务管理器\n\t * @param attributes Properties 格式的事务属性\n\t * @see #setTransactionManager\n\t * @see #setTransactionAttributes(java.util.Properties)\n\t * @deprecated 请改用 {@link #setTransactionAttributes(Properties)}\n\t */",
        ),
        (
            "\t\t// Work out the target class: may be {@code null}.\n\t\t// The TransactionAttributeSource should be passed the target class\n\t\t// as well as the method, which may be from an interface.",
            "\t\t// 确定目标类：可能为 {@code null}。\n\t\t// TransactionAttributeSource 应同时传入目标类与方法，\n\t\t// 方法可能来自接口。",
        ),
        (
            "\t\t// Adapt to TransactionAspectSupport's invokeWithinTransaction...",
            "\t\t// 适配 TransactionAspectSupport 的 invokeWithinTransaction...",
        ),
        (
            "\t//---------------------------------------------------------------------\n\t// Serialization support\n\t//---------------------------------------------------------------------",
            "\t//---------------------------------------------------------------------\n\t// 序列化支持\n\t//---------------------------------------------------------------------",
        ),
        (
            "\tprivate void writeObject(ObjectOutputStream oos) throws IOException {\n\t\t// Rely on default serialization, although this class itself doesn't carry state anyway...\n\t\toos.defaultWriteObject();\n\n\t\t// Deserialize superclass fields.",
            "\tprivate void writeObject(ObjectOutputStream oos) throws IOException {\n\t\t// 依赖默认序列化，尽管本类本身并不携带状态...\n\t\toos.defaultWriteObject();\n\n\t\t// 反序列化超类字段。",
        ),
        (
            "\tprivate void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {\n\t\t// Rely on default serialization, although this class itself doesn't carry state anyway...\n\t\tois.defaultReadObject();\n\n\t\t// Serialize all relevant superclass fields.\n\t\t// Superclass can't implement Serializable because it also serves as base class\n\t\t// for AspectJ aspects (which are not allowed to implement Serializable)!",
            "\tprivate void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {\n\t\t// 依赖默认序列化，尽管本类本身并不携带状态...\n\t\tois.defaultReadObject();\n\n\t\t// 序列化所有相关超类字段。\n\t\t// 超类不能实现 Serializable，因为它也作为 AspectJ 切面的基类\n\t\t// （AspectJ 切面不允许实现 Serializable）！",
        ),
    ],
    "TransactionProxyFactoryBean.java": [
        (
            '/**\n * Proxy factory bean for simplified declarative transaction handling.\n * This is a convenient alternative to a standard AOP\n * {@link org.springframework.aop.framework.ProxyFactoryBean}\n * with a separate {@link TransactionInterceptor} definition.\n *\n * <p><strong>HISTORICAL NOTE:</strong> This class was originally designed to cover the\n * typical case of declarative transaction demarcation: namely, wrapping a singleton\n * target object with a transactional proxy, proxying all the interfaces that the target\n * implements. However, in Spring versions 2.0 and beyond, the functionality provided here\n * is superseded by the more convenient {@code tx:} XML namespace. See the\n * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#transaction-declarative">declarative transaction management</a>\n * section of the Spring reference documentation to understand modern options for managing\n * transactions in Spring applications. For these reasons, <strong>users should favor\n * the {@code tx:} XML namespace as well as\n * the @{@link org.springframework.transaction.annotation.Transactional Transactional}\n * and @{@link org.springframework.transaction.annotation.EnableTransactionManagement\n * EnableTransactionManagement} annotations.</strong>\n *\n * <p>There are three main properties that need to be specified:\n * <ul>\n * <li>"transactionManager": the {@link PlatformTransactionManager} implementation to use\n * (for example, a {@link org.springframework.transaction.jta.JtaTransactionManager} instance)\n * <li>"target": the target object that a transactional proxy should be created for\n * <li>"transactionAttributes": the transaction attributes (for example, propagation\n * behavior and "readOnly" flag) per target method name (or method name pattern)\n * </ul>\n *\n * <p>If the "transactionManager" property is not set explicitly and this {@link FactoryBean}\n * is running in a {@link ListableBeanFactory}, a single matching bean of type\n * {@link PlatformTransactionManager} will be fetched from the {@link BeanFactory}.\n *\n * <p>In contrast to {@link TransactionInterceptor}, the transaction attributes are\n * specified as properties, with method names as keys and transaction attribute\n * descriptors as values. Method names are always applied to the target class.\n *\n * <p>Internally, a {@link TransactionInterceptor} instance is used, but the user of this\n * class does not have to care. Optionally, a method pointcut can be specified\n * to cause conditional invocation of the underlying {@link TransactionInterceptor}.\n *\n * <p>The "preInterceptors" and "postInterceptors" properties can be set to add\n * additional interceptors to the mix, like\n * {@link org.springframework.aop.interceptor.PerformanceMonitorInterceptor}.\n *\n * <p><b>HINT:</b> This class is often used with parent / child bean definitions.\n * Typically, you will define the transaction manager and default transaction\n * attributes (for method name patterns) in an abstract parent bean definition,\n * deriving concrete child bean definitions for specific target objects.\n * This reduces the per-bean definition effort to a minimum.\n *\n * <pre class="code">\n * &lt;bean id="baseTransactionProxy" class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean"\n *     abstract="true"&gt;\n *   &lt;property name="transactionManager" ref="transactionManager"/&gt;\n *   &lt;property name="transactionAttributes"&gt;\n *     &lt;props&gt;\n *       &lt;prop key="insert*"&gt;PROPAGATION_REQUIRED&lt;/prop&gt;\n *       &lt;prop key="update*"&gt;PROPAGATION_REQUIRED&lt;/prop&gt;\n *       &lt;prop key="*"&gt;PROPAGATION_REQUIRED,readOnly&lt;/prop&gt;\n *     &lt;/props&gt;\n *   &lt;/property&gt;\n * &lt;/bean&gt;\n *\n * &lt;bean id="myProxy" parent="baseTransactionProxy"&gt;\n *   &lt;property name="target" ref="myTarget"/&gt;\n * &lt;/bean&gt;\n *\n * &lt;bean id="yourProxy" parent="baseTransactionProxy"&gt;\n *   &lt;property name="target" ref="yourTarget"/&gt;\n * &lt;/bean&gt;</pre>\n *\n * @author Juergen Hoeller\n * @author Dmitriy Kopylenko\n * @author Rod Johnson\n * @author Chris Beams\n * @since 21.08.2003\n * @see #setTransactionManager\n * @see #setTarget\n * @see #setTransactionAttributes\n * @see TransactionInterceptor\n * @see org.springframework.aop.framework.ProxyFactoryBean\n */',
            '/**\n * 用于简化声明式事务处理的代理工厂 Bean。\n * 这是标准 AOP {@link org.springframework.aop.framework.ProxyFactoryBean}\n * 配合独立 {@link TransactionInterceptor} 定义的便捷替代方案。\n *\n * <p><strong>历史说明：</strong>本类最初用于典型声明式事务划分场景：\n * 即用事务代理包装单例目标对象，代理目标实现的所有接口。\n * 但在 Spring 2.0 及之后，此处功能已被更便捷的 {@code tx:} XML 命名空间取代。\n * 请参阅 Spring 参考文档中的\n * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#transaction-declarative">声明式事务管理</a>\n * 章节以了解现代 Spring 应用中的事务管理选项。因此，<strong>用户应优先使用\n * {@code tx:} XML 命名空间以及\n * @{@link org.springframework.transaction.annotation.Transactional Transactional}\n * 与 @{@link org.springframework.transaction.annotation.EnableTransactionManagement\n * EnableTransactionManagement} 注解。</strong>\n *\n * <p>须指定三个主要属性：\n * <ul>\n * <li>"transactionManager"：要使用的 {@link PlatformTransactionManager} 实现\n * （例如 {@link org.springframework.transaction.jta.JtaTransactionManager} 实例）\n * <li>"target"：要为其创建事务代理的目标对象\n * <li>"transactionAttributes"：按目标方法名（或方法名模式）配置的事务属性\n * （例如传播行为与 "readOnly" 标志）\n * </ul>\n *\n * <p>若未显式设置 "transactionManager" 且本 {@link FactoryBean}\n * 运行于 {@link ListableBeanFactory} 中，将从 {@link BeanFactory} 获取\n * 唯一匹配的 {@link PlatformTransactionManager} Bean。\n *\n * <p>与 {@link TransactionInterceptor} 不同，事务属性以 Properties 形式指定，\n * 方法名为键、事务属性描述符为值。方法名始终应用于目标类。\n *\n * <p>内部使用 {@link TransactionInterceptor} 实例，但本类用户无需关心。\n * 可选指定方法切点以条件性调用底层 {@link TransactionInterceptor}。\n *\n * <p>可设置 "preInterceptors" 与 "postInterceptors" 属性以添加额外拦截器，\n * 例如 {@link org.springframework.aop.interceptor.PerformanceMonitorInterceptor}。\n *\n * <p><b>提示：</b>本类常与父子 Bean 定义配合使用。\n * 通常在抽象父 Bean 定义中配置事务管理器与默认事务属性（方法名模式），\n * 再为具体目标对象派生子 Bean 定义，从而将每个 Bean 的定义工作量降至最低。\n *\n * <pre class="code">\n * &lt;bean id="baseTransactionProxy" class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean"\n *     abstract="true"&gt;\n *   &lt;property name="transactionManager" ref="transactionManager"/&gt;\n *   &lt;property name="transactionAttributes"&gt;\n *     &lt;props&gt;\n *       &lt;prop key="insert*"&gt;PROPAGATION_REQUIRED&lt;/prop&gt;\n *       &lt;prop key="update*"&gt;PROPAGATION_REQUIRED&lt;/prop&gt;\n *       &lt;prop key="*"&gt;PROPAGATION_REQUIRED,readOnly&lt;/prop&gt;\n *     &lt;/props&gt;\n *   &lt;/property&gt;\n * &lt;/bean&gt;\n *\n * &lt;bean id="myProxy" parent="baseTransactionProxy"&gt;\n *   &lt;property name="target" ref="myTarget"/&gt;\n * &lt;/bean&gt;\n *\n * &lt;bean id="yourProxy" parent="baseTransactionProxy"&gt;\n *   &lt;property name="target" ref="yourTarget"/&gt;\n * &lt;/bean&gt;</pre>\n *\n * @author Juergen Hoeller\n * @author Dmitriy Kopylenko\n * @author Rod Johnson\n * @author Chris Beams\n * @since 21.08.2003\n * @see #setTransactionManager\n * @see #setTarget\n * @see #setTransactionAttributes\n * @see TransactionInterceptor\n * @see org.springframework.aop.framework.ProxyFactoryBean\n */',
        ),
        (
            '\t/**\n\t * Set the default transaction manager. This will perform actual\n\t * transaction management: This class is just a way of invoking it.\n\t * @see TransactionInterceptor#setTransactionManager\n\t */',
            '\t/**\n\t * 设置默认事务管理器。它将执行实际事务管理：\n\t * 本类只是调用它的方式。\n\t * @see TransactionInterceptor#setTransactionManager\n\t */',
        ),
        (
            '\t/**\n\t * Set properties with method names as keys and transaction attribute\n\t * descriptors (parsed via TransactionAttributeEditor) as values:\n\t * for example, key = "myMethod", value = "PROPAGATION_REQUIRED,readOnly".\n\t * <p>Note: Method names are always applied to the target class,\n\t * no matter if defined in an interface or the class itself.\n\t * <p>Internally, a NameMatchTransactionAttributeSource will be\n\t * created from the given properties.\n\t * @see #setTransactionAttributeSource\n\t * @see TransactionInterceptor#setTransactionAttributes\n\t * @see TransactionAttributeEditor\n\t * @see NameMatchTransactionAttributeSource\n\t */',
            '\t/**\n\t * 设置 Properties，以方法名为键、事务属性描述符\n\t * （通过 TransactionAttributeEditor 解析）为值：\n\t * 例如 key = "myMethod"，value = "PROPAGATION_REQUIRED,readOnly"。\n\t * <p>注意：方法名始终应用于目标类，无论定义在接口还是类本身。\n\t * <p>内部将根据给定 Properties 创建 NameMatchTransactionAttributeSource。\n\t * @see #setTransactionAttributeSource\n\t * @see TransactionInterceptor#setTransactionAttributes\n\t * @see TransactionAttributeEditor\n\t * @see NameMatchTransactionAttributeSource\n\t */',
        ),
        (
            '\t/**\n\t * Set the transaction attribute source which is used to find transaction\n\t * attributes. If specifying a String property value, a PropertyEditor\n\t * will create a MethodMapTransactionAttributeSource from the value.\n\t * @see #setTransactionAttributes\n\t * @see TransactionInterceptor#setTransactionAttributeSource\n\t * @see TransactionAttributeSourceEditor\n\t * @see MethodMapTransactionAttributeSource\n\t * @see NameMatchTransactionAttributeSource\n\t * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource\n\t */',
            '\t/**\n\t * 设置用于查找事务属性的事务属性源。\n\t * 若指定 String 属性值，PropertyEditor 将从该值创建 MethodMapTransactionAttributeSource。\n\t * @see #setTransactionAttributes\n\t * @see TransactionInterceptor#setTransactionAttributeSource\n\t * @see TransactionAttributeSourceEditor\n\t * @see MethodMapTransactionAttributeSource\n\t * @see NameMatchTransactionAttributeSource\n\t * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource\n\t */',
        ),
        (
            '\t/**\n\t * Set a pointcut, i.e a bean that can cause conditional invocation\n\t * of the TransactionInterceptor depending on method and attributes passed.\n\t * Note: Additional interceptors are always invoked.\n\t * @see #setPreInterceptors\n\t * @see #setPostInterceptors\n\t */',
            '\t/**\n\t * 设置切点，即可根据传入的方法与属性条件性调用 TransactionInterceptor 的 Bean。\n\t * 注意：额外拦截器始终会被调用。\n\t * @see #setPreInterceptors\n\t * @see #setPostInterceptors\n\t */',
        ),
        (
            '\t/**\n\t * This callback is optional: If running in a BeanFactory and no transaction\n\t * manager has been set explicitly, a single matching bean of type\n\t * {@link PlatformTransactionManager} will be fetched from the BeanFactory.\n\t * @see org.springframework.beans.factory.BeanFactory#getBean(Class)\n\t * @see org.springframework.transaction.PlatformTransactionManager\n\t */',
            '\t/**\n\t * 本回调可选：若在 BeanFactory 中运行且未显式设置事务管理器，\n\t * 将从 BeanFactory 获取唯一匹配的 {@link PlatformTransactionManager} Bean。\n\t * @see org.springframework.beans.factory.BeanFactory#getBean(Class)\n\t * @see org.springframework.transaction.PlatformTransactionManager\n\t */',
        ),
        (
            "\t/**\n\t * Creates an advisor for this FactoryBean's TransactionInterceptor.\n\t */",
            '\t/**\n\t * 为本 FactoryBean 的 TransactionInterceptor 创建 Advisor。\n\t */',
        ),
        (
            '\t\t\t// Rely on default pointcut.',
            '\t\t\t// 依赖默认切点。',
        ),
        (
            '\t/**\n\t * As of 4.2, this method adds {@link TransactionalProxy} to the set of\n\t * proxy interfaces in order to avoid re-processing of transaction metadata.\n\t */',
            '\t/**\n\t * 自 4.2 起，本方法将 {@link TransactionalProxy} 加入代理接口集合，\n\t * 以避免重复处理事务元数据。\n\t */',
        ),
    ],
    "TransactionalProxy.java": [
        (
            '/**\n * A marker interface for manually created transactional proxies.\n *\n * <p>{@link TransactionAttributeSourcePointcut} will ignore such existing\n * transactional proxies during AOP auto-proxying and therefore avoid\n * re-processing transaction metadata on them.\n *\n * @author Juergen Hoeller\n * @since 4.1.7\n */',
            '/**\n * 手动创建的事务代理的标记接口。\n *\n * <p>{@link TransactionAttributeSourcePointcut} 在 AOP 自动代理期间\n * 将忽略此类已有事务代理，从而避免对其重复处理事务元数据。\n *\n * @author Juergen Hoeller\n * @since 4.1.7\n */',
        ),
    ],
}
