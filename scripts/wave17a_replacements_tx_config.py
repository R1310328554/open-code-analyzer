"""Chinese JavaDoc replacements for springframework wave17a tx config [2:8]."""

TX_CONFIG_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AnnotationDrivenBeanDefinitionParser.java": [
        (
            "/**\n * {@link org.springframework.beans.factory.xml.BeanDefinitionParser\n * BeanDefinitionParser} implementation that allows users to easily configure\n * all the infrastructure beans required to enable annotation-driven transaction\n * demarcation.\n *\n * <p>By default, all proxies are created as JDK proxies. This may cause some\n * problems if you are injecting objects as concrete classes rather than\n * interfaces. To overcome this restriction you can set the\n * '{@code proxy-target-class}' attribute to '{@code true}', which\n * will result in class-based proxies being created.\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 2.0\n */",
            "/**\n * {@link org.springframework.beans.factory.xml.BeanDefinitionParser\n * BeanDefinitionParser} 实现，使用户能够轻松配置\n * 启用注解驱动事务标记所需的全部基础设施 Bean。\n *\n * <p>默认情况下，所有代理均创建为 JDK 代理。若将对象\n * 以具体类而非接口注入，这可能引发一些问题。\n * 要克服此限制，可将 '{@code proxy-target-class}' 属性设为 '{@code true}'，\n * 从而创建基于类的代理。\n *\n * @author Juergen Hoeller\n * @author Rob Harrop\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Parses the {@code <tx:annotation-driven/>} tag. Will\n\t * {@link AopNamespaceUtils#registerAutoProxyCreatorIfNecessary register an AutoProxyCreator}\n\t * with the container as necessary.\n\t */",
            "\t/**\n\t * 解析 {@code <tx:annotation-driven/>} 标签。必要时\n\t * 向容器 {@link AopNamespaceUtils#registerAutoProxyCreatorIfNecessary 注册 AutoProxyCreator}。\n\t */",
        ),
        (
            "\t/**\n\t * Inner class to just introduce an AOP framework dependency when actually in proxy mode.\n\t */",
            "\t/**\n\t * 内部类，仅在代理模式下引入 AOP 框架依赖。\n\t */",
        ),
        (
            "\t\t\t\t// Create the TransactionAttributeSource definition.",
            "\t\t\t\t// 创建 TransactionAttributeSource 定义。",
        ),
        (
            "\t\t\t\t// Create the TransactionInterceptor definition.",
            "\t\t\t\t// 创建 TransactionInterceptor 定义。",
        ),
        (
            "\t\t\t\t// Create the TransactionAttributeSourceAdvisor definition.",
            "\t\t\t\t// 创建 TransactionAttributeSourceAdvisor 定义。",
        ),
    ],
    "JtaTransactionManagerBeanDefinitionParser.java": [
        (
            "/**\n * Parser for the &lt;tx:jta-transaction-manager/&gt; XML configuration element.\n *\n * @author Juergen Hoeller\n * @author Christian Dupuis\n * @since 2.5\n */",
            "/**\n * 解析 Spring 事务命名空间中 &lt;tx:jta-transaction-manager/&gt; XML 配置元素的解析器。\n * 将 {@link JtaTransactionManager} 注册为默认 Bean 名称 transactionManager。\n *\n * @author Juergen Hoeller\n * @author Christian Dupuis\n * @since 2.5\n */",
        ),
    ],
    "JtaTransactionManagerFactoryBean.java": [
        (
            "/**\n * A {@link FactoryBean} equivalent to the &lt;tx:jta-transaction-manager/&gt; XML element.\n *\n * @author Juergen Hoeller\n * @since 4.1.1\n * @deprecated as of 6.0, in favor of a straight {@link JtaTransactionManager} definition\n */",
            "/**\n * 等同于 &lt;tx:jta-transaction-manager/&gt; XML 元素的 {@link FactoryBean}。\n * 创建并初始化 {@link JtaTransactionManager} 单例实例。\n *\n * @author Juergen Hoeller\n * @since 4.1.1\n * @deprecated as of 6.0, in favor of a straight {@link JtaTransactionManager} definition\n */",
        ),
    ],
    "TransactionManagementConfigUtils.java": [
        (
            "/**\n * Configuration constants for internal sharing across subpackages.\n *\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 3.1\n */",
            "/**\n * 供子包内部共享的配置常量。\n *\n * @author Chris Beams\n * @author Stephane Nicoll\n * @since 3.1\n */",
        ),
        (
            "\t/**\n\t * The bean name of the internally managed transaction advisor (used when mode == PROXY).\n\t */",
            "\t/**\n\t * 内部管理的事务 Advisor 的 Bean 名称（mode == PROXY 时使用）。\n\t */",
        ),
        (
            "\t/**\n\t * The bean name of the internally managed transaction aspect (used when mode == ASPECTJ).\n\t */",
            "\t/**\n\t * 内部管理的事务 Aspect 的 Bean 名称（mode == ASPECTJ 时使用）。\n\t */",
        ),
        (
            "\t/**\n\t * The class name of the AspectJ transaction management aspect.\n\t */",
            "\t/**\n\t * AspectJ 事务管理切面的类名。\n\t */",
        ),
        (
            "\t/**\n\t * The name of the AspectJ transaction management @{@code Configuration} class.\n\t */",
            "\t/**\n\t * AspectJ 事务管理 @{@code Configuration} 类的名称。\n\t */",
        ),
        (
            "\t/**\n\t * The bean name of the internally managed JTA transaction aspect (used when mode == ASPECTJ).\n\t * @since 5.1\n\t */",
            "\t/**\n\t * 内部管理的 JTA 事务 Aspect 的 Bean 名称（mode == ASPECTJ 时使用）。\n\t * @since 5.1\n\t */",
        ),
        (
            "\t/**\n\t * The class name of the AspectJ transaction management aspect.\n\t * @since 5.1\n\t */",
            "\t/**\n\t * AspectJ 事务管理切面的类名。\n\t * @since 5.1\n\t */",
        ),
        (
            "\t/**\n\t * The name of the AspectJ transaction management @{@code Configuration} class for JTA.\n\t * @since 5.1\n\t */",
            "\t/**\n\t * JTA 的 AspectJ 事务管理 @{@code Configuration} 类名称。\n\t * @since 5.1\n\t */",
        ),
        (
            "\t/**\n\t * The bean name of the internally managed TransactionalEventListenerFactory.\n\t */",
            "\t/**\n\t * 内部管理的 TransactionalEventListenerFactory 的 Bean 名称。\n\t */",
        ),
    ],
    "TxAdviceBeanDefinitionParser.java": [
        (
            "/**\n * {@link org.springframework.beans.factory.xml.BeanDefinitionParser\n * BeanDefinitionParser} for the {@code <tx:advice/>} tag.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Adrian Colyer\n * @author Chris Beams\n * @since 2.0\n */",
            "/**\n * {@code <tx:advice/>} 标签的\n * {@link org.springframework.beans.factory.xml.BeanDefinitionParser\n * BeanDefinitionParser}。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Adrian Colyer\n * @author Chris Beams\n * @since 2.0\n */",
        ),
        (
            "\t\t\t// Using attributes source.",
            "\t\t\t// 使用 attributes 源。",
        ),
        (
            "\t\t\t// Assume annotations source.",
            "\t\t\t// 假定使用注解源。",
        ),
    ],
    "TxNamespaceHandler.java": [
        (
            "/**\n * {@code NamespaceHandler} allowing for the configuration of\n * declarative transaction management using either XML or using annotations.\n *\n * <p>This namespace handler is the central piece of functionality in the\n * Spring transaction management facilities and offers two approaches\n * to declaratively manage transactions.\n *\n * <p>One approach uses transaction semantics defined in XML using the\n * {@code <tx:advice>} elements, the other uses annotations\n * in combination with the {@code <tx:annotation-driven>} element.\n * Both approached are detailed to great extent in the Spring reference manual.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 允许使用 XML 或注解配置声明式事务管理的 {@code NamespaceHandler}。\n *\n * <p>此命名空间处理器是 Spring 事务管理设施的核心功能，\n * 提供两种声明式管理事务的方式。\n *\n * <p>一种方式使用 {@code <tx:advice>} 元素在 XML 中定义事务语义，\n * 另一种将注解与 {@code <tx:annotation-driven>} 元素结合使用。\n * 两种方式在 Spring 参考手册中均有详尽说明。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
    ],
}
